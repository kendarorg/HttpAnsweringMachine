package org.kendar.replayer.engine.http;

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
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class HttpReplayer implements ReplayerEngine {

    protected final ConcurrentHashMap<Long, Object> states = new ConcurrentHashMap<>();

    private final HibernateSessionFactory sessionFactory;
    private final Logger logger;
    private String localAddress;
    private JsonConfiguration configuration;
    private long name;

    public ReplayerEngine create(LoggerBuilder loggerBuilder) {
        var es = new HttpReplayer(sessionFactory, loggerBuilder, configuration);
        return es;
    }

    @Override
    public boolean isValidPath(Request req) {
        var isValid = (!isLocalhost(req)) ||
                (req.getPath().startsWith("/int/") && isLocalhost(req));
        return isValid;
    }

    private boolean isLocalhost(Request req) {
        return req.getHost().equalsIgnoreCase(localAddress) || req.getHost().equalsIgnoreCase("127.0.0.1") || req.getHost().equalsIgnoreCase("localhost");
    }

    @Override
    public boolean isValidRoundTrip(Request req, Response res, Map<String, String> specialParams) {
        var hosts = specialParams.get("hosts") == null ? new String[]{"*"} :
                specialParams.get("hosts").trim().split(",");
        var hostsAllowed = false;
        var reqHost = req.getHost();
        for (var host : hosts) {
            if (host.equalsIgnoreCase("*")) {
                hostsAllowed = true;
                break;
            } else if (host.equalsIgnoreCase(reqHost)) {
                hostsAllowed = true;
                break;
            } else if (host.contains("*")) {
                var splHost = host.split("\\*");
                var lastIndex = 0;
                hostsAllowed = true;
                for (var spl : splHost) {
                    var foundIndex = reqHost.indexOf(spl, lastIndex);
                    if (foundIndex < 0) {
                        hostsAllowed = false;
                        break;
                    }
                }
                if (hostsAllowed) break;
            }
        }
        if (!hostsAllowed) return false;
        return true;
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

    public HttpReplayer(HibernateSessionFactory sessionFactory, LoggerBuilder loggerBuilder, JsonConfiguration configuration) {
        this.sessionFactory = sessionFactory;
        this.logger = loggerBuilder.build(HttpReplayer.class);
        this.localAddress = configuration.getConfiguration(GlobalConfig.class).getLocalAddress();
        this.configuration = configuration;
    }

    @Override
    public String getId() {
        return "http";
    }

    @Override
    public void loadDb(Long recordingId) throws Exception {
        this.name = recordingId;
        if (!hasHttpRows(recordingId)) return;
    }

    private boolean hasRows = false;

    private boolean hasHttpRows(Long recordingId) throws Exception {
        hasRows = (Long) sessionFactory.queryResult(e -> {
            return (Long) e.createQuery("SELECT count(*) FROM ReplayerRow e " +
                            " WHERE " +
                            " e.type='http'" +
                            "AND e.recordingId=" + recordingId)
                    .getResultList().get(0);
        }) > 0;
        return hasRows;
    }

    @Override
    public RequestMatch findRequestMatch(Request req, String contentHash, Map<String, String> params) throws Exception {

        if (!hasRows) return null;
        RequestMatch founded = findRequestMatch(req, contentHash, true);
        if (founded == null) {
            founded = findRequestMatch(req, contentHash, false);
        }
        return founded;
    }


    private RequestMatch findRequestMatch(Request sreq, String contentHash, boolean staticRequest) throws Exception {
        var matchingQuery = -1;
        ReplayerRow founded = null;
        var staticRequests = new ArrayList<ReplayerRow>();

        sessionFactory.query(em -> {
            var query = em.createQuery("SELECT e FROM ReplayerRow  e,CallIndex c WHERE " +
                    " e.id=c.reference " +
                    " AND c.stimulatorTest=false" +
                    " AND e.staticRequest=:sr " +
                    " AND e.type='http' " +
                    " AND e.recordingId=:recordingId" +
                    " AND e.path=:path" +
                    " AND e.host=:host" +
                    " ORDER BY e.id ASC");
            query.setParameter("sr", staticRequest);
            query.setParameter("recordingId", name);
            query.setParameter("path", sreq.getPath());
            query.setParameter("host", sreq.getHost());
            var res = query.getResultList();
            for (var rr : res) {
                em.detach(rr);
            }
            staticRequests.addAll(res);
        });


        var indexesIds = staticRequests.stream().map(r -> String.valueOf(r.getIndex())).collect(Collectors.toList());
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
                }
            }

            matchedQuery += matchQuery(rreq.getQuery(), sreq.getQuery());
            if (matchedQuery > matchingQuery) {
                matchingQuery = matchedQuery;
                founded = row;
            }
        }

        if (founded != null) {
            states.put(founded.getId(), "");
        } else {
            return null;
        }
        return new RequestMatch(sreq, founded.getRequest(),founded.getResponse());
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

}
