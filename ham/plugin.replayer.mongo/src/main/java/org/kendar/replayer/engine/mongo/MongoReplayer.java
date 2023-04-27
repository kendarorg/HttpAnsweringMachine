package org.kendar.replayer.engine.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ClassUtils;
import org.kendar.mongo.model.MongoReqResPacket;
import org.kendar.replayer.engine.ReplayerEngine;
import org.kendar.replayer.engine.RequestMatch;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.typed.serializer.JsonTypedSerializer;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class MongoReplayer implements ReplayerEngine {

    protected final ConcurrentHashMap<Long, Object> states = new ConcurrentHashMap<>();

    private final HibernateSessionFactory sessionFactory;
    private final Logger logger;
    private final String localAddress;
    private final JsonConfiguration configuration;
    private final AtomicInteger responseRequestId = new AtomicInteger(1);
    private final JsonTypedSerializer serializer = new JsonTypedSerializer();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<String, String> connectionIds = new ConcurrentHashMap<>();
    private long name;
    private boolean hasRows = false;

    public MongoReplayer(HibernateSessionFactory sessionFactory, LoggerBuilder loggerBuilder, JsonConfiguration configuration) {
        this.sessionFactory = sessionFactory;
        this.logger = loggerBuilder.build(MongoReplayer.class);
        this.localAddress = configuration.getConfiguration(GlobalConfig.class).getLocalAddress();
        this.configuration = configuration;
    }

    public ReplayerEngine create(LoggerBuilder loggerBuilder) {
        var es = new MongoReplayer(sessionFactory, loggerBuilder, configuration);
        return es;
    }

    @Override
    public boolean isValidPath(Request req) {
        return
                req.getPath().contains("/api/mongo/") && req.getMethod().equalsIgnoreCase("POST");
    }

    private boolean isLocalhost(Request req) {
        return req.getHost().equalsIgnoreCase(localAddress) ||
                req.getHost().equalsIgnoreCase("127.0.0.1") ||
                req.getHost().equalsIgnoreCase("localhost");
    }

    @Override
    public boolean isValidRoundTrip(Request req, Response res, Map<String, String> specialParams) {
        var ports = specialParams.get("ports") == null ? new String[]{"*"} :
                specialParams.get("ports").trim().split(",");
        var port = req.getPathParameter("port");
        var portAllowed = false;
        for (var allowedPort : ports) {
            if (allowedPort.equalsIgnoreCase("*")) {
                portAllowed = true;
                break;
            } else if (allowedPort.equalsIgnoreCase(port)) {
                portAllowed = true;
                break;
            }
        }
        return portAllowed;

    }

    @Override
    public void setParams(Map<String, String> params) {

    }

    protected void loadIndexes(Long recordingId, ArrayList<CallIndex> indexes) throws Exception {
        sessionFactory.query(e -> {
            addAllIndexes(recordingId, indexes, e);
        });
    }

    protected void addAllIndexes(Long recordingId, ArrayList<CallIndex> indexes, EntityManager e) {
        var rs = e.createQuery("SELECT e FROM CallIndex e LEFT JOIN ReplayerRow f " +
                " ON e.reference = f.id" +
                " WHERE " +
                " f.type='" + this.getId() + "' AND e.recordingId=" + recordingId +
                " AND e.stimulatorTest=false ORDER BY e.id ASC").getResultList();
        var founded = new HashSet<Long>();
        for (var rss : rs) {
            e.detach(rss);
            var index = (CallIndex) rss;
            if (founded.contains(index.getIndex())) continue;
            indexes.add(index);
            founded.add(index.getIndex());
        }
    }

    @Override
    public void setupStaticCalls(DbRecording recording) throws Exception {
        ArrayList<CallIndex> indexes = new ArrayList<>();
        HashMap<String, List<Long>> mappingIndexes = new HashMap<>();
        HashMap<String, Set<String>> mappingResponses = new HashMap<>();
        loadIndexes(recording.getId(), indexes);
        for (var index : indexes) {
            sessionFactory.query(e -> {
                Object[] crcPath = (Object[]) e.createQuery("SELECT " +
                        " c.requestHash,c.path,c.responseHash " +
                        " FROM ReplayerRow c WHERE c.recordingId=" + recording.getId() + " AND " +
                        " c.id=" + index.getReference()).getResultList().get(0);

                var requestHash = (String) crcPath[0];
                var path = (String) crcPath[1];
                var responseHash = (String) crcPath[2];

                //var crc = row.getRequestHash()+":"+row.getResponseHash();
                if (!mappingIndexes.containsKey(requestHash + path)) {
                    mappingIndexes.put(requestHash + path, new ArrayList<>());
                    mappingResponses.put(requestHash + path, new HashSet<>());
                }
                mappingIndexes.get(requestHash + path).add(index.getId());
                mappingResponses.get(requestHash + path).add(responseHash);
            });
        }
        for (var mappingIndex : mappingIndexes.entrySet()) {
            if (mappingIndex.getValue().size() > 1 && mappingResponses.get(mappingIndex.getKey()).size() == 1) {

                sessionFactory.transactional(e -> {
                    var first = mappingIndex.getValue().get(0);

                    var callIndexesToRemove = mappingIndex.getValue().stream().skip(1)
                            .map(a -> a.toString())
                            .collect(Collectors.toList());
                    var callIndex = (CallIndex) e.createQuery("SELECT e FROM CallIndex e " +
                            " WHERE " +
                            " e.recordingId=" + recording.getId() +
                            " AND e.id=" + first).getResultList().get(0);
                    callIndex.setCalls(callIndexesToRemove.size() + 1);
                    var row = (ReplayerRow) e.createQuery("SELECT e FROM ReplayerRow e " +
                            " WHERE " +
                            " e.recordingId=" + recording.getId() +
                            " AND e.id=" + callIndex.getReference()).getResultList().get(0);
                    row.setStaticRequest(true);
                    e.merge(row);
                    e.merge(callIndex);


                    var referencesToRemove = ((List<Long>) e.createQuery("SELECT e.reference FROM CallIndex e " +
                            " WHERE " +
                            " e.recordingId=" + recording.getId() +
                            " AND e.id IN (" + String.join(",", callIndexesToRemove) + ")").getResultList())
                            .stream().map(a -> a.toString()).collect(Collectors.toList());
                    e.createQuery("DELETE FROM  ReplayerRow e " +
                            " WHERE " +
                            " e.recordingId=" + recording.getId() +
                            " AND e.id IN (" + String.join(",", referencesToRemove) + ")").executeUpdate();
                    e.createQuery("DELETE FROM  CallIndex e " +
                            " WHERE " +
                            " e.recordingId=" + recording.getId() +
                            " AND e.id IN (" + String.join(",", callIndexesToRemove) + ")").executeUpdate();


                });
            }
        }

    }

    @Override
    public void updateReqRes(Request req, Response res, Map<String, String> specialParams) {

    }

    @Override
    public String getId() {
        return "mongo";
    }

    @Override
    public void loadDb(Long recordingId) throws Exception {
        this.name = recordingId;
        if (!hasHttpRows(recordingId)) return;
    }

    private boolean hasHttpRows(Long recordingId) throws Exception {
        hasRows = (Long) sessionFactory.queryResult(e -> {
            return (Long) e.createQuery("SELECT count(*) FROM ReplayerRow e " +
                            " WHERE " +
                            " e.type='mongo'" +
                            "AND e.recordingId=" + recordingId)
                    .getResultList().get(0);
        }) > 0;
        return hasRows;
    }

    @Override
    public RequestMatch findRequestMatch(Request req, String contentHash, Map<String, String> params) throws Exception {

        if (!hasRows) return null;
        var foundedRes = findRequestMatch(req, contentHash, false);

        if (foundedRes != null) {
            var founded = foundedRes.getFoundedRes();
            var receivedDeserializer = serializer.newInstance();
            receivedDeserializer.deserialize(req.getRequestText());
            var receivedDeserialized = receivedDeserializer.read("data");

            var skipPrint = "ismaster".equalsIgnoreCase(req.getPathParameter("operation")) ||
                    "hello".equalsIgnoreCase(req.getPathParameter("operation"));
            if (!skipPrint) {
                logger.debug(mapper.writeValueAsString(receivedDeserialized));
            }
            if (ClassUtils.isAssignable(receivedDeserialized.getClass(), MongoReqResPacket.class)) {
                var toSendSerializer = serializer.newInstance();
                toSendSerializer.deserialize(founded.getResponseText());
                var toSendDeserialized = toSendSerializer.read("data");
                if (ClassUtils.isAssignable(toSendDeserialized.getClass(), MongoReqResPacket.class)) {
                    var tss = (MongoReqResPacket) toSendDeserialized;
                    var rcv = (MongoReqResPacket) receivedDeserialized;
                    tss.setResponseTo(rcv.getRequestId());
                    tss.setRequestId(responseRequestId.incrementAndGet());
                    toSendSerializer = serializer.newInstance();
                    toSendSerializer.write("data", tss);
                    if (!skipPrint) {
                        logger.debug(mapper.writeValueAsString(tss));
                    }
                    var responseReal = founded.copy();
                    responseReal.setResponseText((String) toSendSerializer.getSerialized());
                    foundedRes.setFoundedRes(responseReal);
                }
            }

        }
        return foundedRes;
    }

    private RequestMatch findRequestMatch4(Request sreq, String contentHash, boolean staticRequest) throws Exception {
        var matchingQuery = -10000;
        ReplayerRow founded = null;
        var staticRequests = new ArrayList<ReplayerRow>();
        var sreqPath = sreq.getPath();
        if (!sreqPath.startsWith("http://127.0.0.1/api/mongo")) return null;

        var connectionId = sreq.getHeader("X-CONNECTION-ID");
        String internalFlowId = null;
        if (connectionId != null && connectionIds.containsKey(connectionId) &&
                !sreqPath.toLowerCase().endsWith("/ismaster")) {
            internalFlowId = connectionIds.get(connectionId);
        }


        sessionFactory.query(em -> {
            var query = em.createQuery("SELECT e FROM ReplayerRow  e,CallIndex c WHERE " +
                    " e.id=c.reference " +
                    " AND c.stimulatorTest=false" +
                    " AND e.staticRequest=:sr " +
                    " AND e.type='mongo' " +
                    " AND e.recordingId=:recordingId" +
                    " AND e.path=:path" +
                    " AND e.host=:host" +
                    " ORDER BY e.id ASC ");
            if (sreqPath.toLowerCase().endsWith("/ismaster")) {
                query.setMaxResults(1);
            }
            query.setParameter("sr", staticRequest);
            query.setParameter("recordingId", name);
            query.setParameter("path", sreq.getPath());
            query.setParameter("host", sreq.getHost());
            var res = (List<ReplayerRow>) query.getResultList().stream().collect(Collectors.toList());
            for (var rr : res) {
                em.detach(rr);
                if (states.containsKey(rr.getId())) continue;
                staticRequests.add(rr);
            }
        });


        var indexesIds = staticRequests.stream().map(r -> String.valueOf(r.getId())).collect(Collectors.toList());
        var indexes = " e.reference=" + String.join(" OR e.reference=", indexesIds);
        var callIndexes = new ArrayList<CallIndex>();
        var baseQueryString = "SELECT e FROM CallIndex  e WHERE " +
                "  (" + indexes + ")" +
                " AND e.recordingId=:recordingId" +
                " ORDER BY e.reference ASC";
        if (indexesIds.size() > 0) {
            sessionFactory.query(em -> {
                var query = em.createQuery(baseQueryString);
                query.setParameter("recordingId", name);
                //query.setParameter("reqs",indexes);
                var ci = query.getResultList();
                for (var c : ci) {
                    em.detach(c);
                    callIndexes.add((CallIndex) c);
                }
            });
        }
        var uuid = UUID.randomUUID().toString();
        try {

            for (var row : staticRequests) {
                if (!staticRequest) {
                    var st = states.get(row.getId());
                    if (null != st) {
                        continue;
                    }
                }
                var rreq = row.getRequest();
                var callIndex = callIndexes.stream().filter(
                        ci -> ci.getReference() == row.getId()
                ).findFirst();
                var matchedQuery = 0;
                if (rreq.isBinaryRequest() == sreq.isBinaryRequest()) {
                    if (row.getRequestHash().equalsIgnoreCase(contentHash)) {
                        matchedQuery += 20;
                    } else {
                        if (!rreq.isBinaryRequest()) {
                            if (rreq.getRequestText().length() == sreq.getRequestText().length()) {
                                matchedQuery += Math.abs(Levenshtein.normalized(
                                        rreq.getRequestText(),
                                        sreq.getRequestText(), 20));
                            }
                        }
                    }
                }

                var rreqConnectionId = rreq.getHeader("X-CONNECTION-ID");
                if (internalFlowId != null && rreqConnectionId != null && rreqConnectionId.equalsIgnoreCase(internalFlowId)) {
                    matchedQuery += 20;
                }
                //matchedQuery += matchQuery(rreq.getQuery(), sreq.getQuery());
                if (matchedQuery > matchingQuery) {
                    matchingQuery = matchedQuery;
                    founded = row;
                }
            }
        } catch (Exception ex) {
            logger.error("error", ex);
        }


        if (founded != null) {
            if (!sreqPath.toLowerCase().endsWith("/ismaster")) {
                states.put(founded.getId(), "");
            }
        } else {
            return null;
        }

        if (connectionId != null && internalFlowId == null && !sreqPath.toLowerCase().endsWith("/ismaster")) {
            connectionIds.put(connectionId, founded.getRequest().getHeader("X-CONNECTION-ID"));
        }

        return new RequestMatch(sreq, founded.getRequest(), founded.getResponse());
    }

    private int matchQuery(Map<String, String> left, Map<String, String> right) {
        var result = 0;
        for (var leftItem : left.entrySet()) {
            for (var rightItem : right.entrySet()) {
                if (leftItem.getKey().equalsIgnoreCase(rightItem.getKey())) {
                    result++;
                    if (leftItem.getValue() == null) {
                        if (rightItem.getValue() == null) {
                            result++;
                        }
                    } else if (leftItem.getValue().equalsIgnoreCase(rightItem.getValue())) {
                        result++;
                    }
                }
            }
        }
        return result;
    }

    private RequestMatch findRequestMatch(Request sreq, String contentHash, boolean staticRequest) throws Exception {
        var matchingQuery = -10000;
        ReplayerRow founded = null;
        var staticRequests = new ArrayList<ReplayerRow>();
        var sreqPath = sreq.getPath();
        if (!sreqPath.startsWith("/api/mongo")) return null;

        var connectionId = sreq.getHeader("X-CONNECTION-ID");

        sessionFactory.query(em -> {
            var query = em.createQuery("SELECT e FROM ReplayerRow  e,CallIndex c WHERE " +
                    " e.id=c.reference " +
                    " AND c.stimulatorTest=false" +
                    " AND e.type='mongo' " +
                    " AND e.recordingId=:recordingId" +
                    " AND e.path=:path" +
                    " ORDER BY e.id ASC ");
            if (sreqPath.toLowerCase().endsWith("/ismaster")) {
                query.setMaxResults(1);
            }
            query.setParameter("recordingId", name);
            query.setParameter("path", sreq.getPath());
            var res = (List<ReplayerRow>) query.getResultList().stream().collect(Collectors.toList());
            for (var rr : res) {
                em.detach(rr);
                if (states.containsKey(rr.getId())) continue;
                staticRequests.add(rr);
            }
        });


        var indexesIds = staticRequests.stream().map(r -> String.valueOf(r.getId())).collect(Collectors.toList());
        var indexes = " e.reference=" + String.join(" OR e.reference=", indexesIds);
        var callIndexes = new ArrayList<CallIndex>();
        var baseQueryString = "SELECT e FROM CallIndex  e WHERE " +
                "  (" + indexes + ")" +
                " AND e.recordingId=:recordingId" +
                " ORDER BY e.reference ASC";
        if (indexesIds.size() > 0) {
            sessionFactory.query(em -> {
                var query = em.createQuery(baseQueryString);
                query.setParameter("recordingId", name);
                //query.setParameter("reqs",indexes);
                var ci = query.getResultList();
                for (var c : ci) {
                    em.detach(c);
                    callIndexes.add((CallIndex) c);
                }
            });
        }

        try {

            for (var row : staticRequests) {
                if (!staticRequest) {
                    var st = states.get(row.getId());
                    if (null != st) {
                        continue;
                    }
                }
                var rreq = row.getRequest();
                var callIndex = callIndexes.stream().filter(
                        ci -> ci.getReference() == row.getId()
                ).findFirst();
                var matchedQuery = 0;
                if (rreq.isBinaryRequest() == sreq.isBinaryRequest()) {
                    if (row.getRequestHash().equalsIgnoreCase(contentHash)) {
                        matchedQuery += 20;
                    } else {
                        if (!rreq.isBinaryRequest()) {
                            if (rreq.getRequestText().length() == sreq.getRequestText().length()) {
                                matchedQuery += Math.abs(Levenshtein.normalized(
                                        rreq.getRequestText(),
                                        sreq.getRequestText(), 20));
                            }
                        }
                    }
                }

                var rreqConnectionId = rreq.getHeader("X-CONNECTION-ID");
                //if(internalFlowId!=null && rreqConnectionId !=null && rreqConnectionId.equalsIgnoreCase(internalFlowId)){
                //   matchedQuery+=20;
                //}
                //matchedQuery += matchQuery(rreq.getQuery(), sreq.getQuery());
                if (matchedQuery > matchingQuery) {
                    matchingQuery = matchedQuery;
                    founded = row;
                }
            }
        } catch (Exception ex) {
            logger.error("error:", ex);
        }

        if (founded != null) {
            if (!sreqPath.toLowerCase().endsWith("/ismaster")) {
                states.put(founded.getId(), "");
            }
        } else {
            return null;
        }


        return new RequestMatch(sreq, founded.getRequest(), founded.getResponse());
    }

}
