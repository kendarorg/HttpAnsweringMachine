package org.kendar.replayer.engine.http;

import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.engine.ReplayerEngine;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class HttpReplayer implements ReplayerEngine {

    protected final ConcurrentHashMap<Long, Object> states = new ConcurrentHashMap<>();

    private final HibernateSessionFactory sessionFactory;
    private final Logger logger;
    private long name;

    public ReplayerEngine create(LoggerBuilder loggerBuilder){
        return new HttpReplayer(sessionFactory,loggerBuilder);
    }

    @Override
    public boolean isValidPath(String path) {
        return path.startsWith("/int/");
    }

    @Override
    public boolean isValidRoundTrip(Request req, Response res, Map<String, String> specialParams) {
        return true;
    }

    public HttpReplayer(HibernateSessionFactory sessionFactory, LoggerBuilder loggerBuilder) {
        this.sessionFactory = sessionFactory;
        this.logger = loggerBuilder.build(HttpReplayer.class);
    }

    @Override
    public String getId() {
        return "http";
    }

    @Override
    public void loadDb(Long recordingId) throws Exception {
        this.name = recordingId;
        if(!hasHttpRows(recordingId))return;
    }

    private boolean hasRows = false;

    private boolean hasHttpRows(Long recordingId) throws Exception {
        hasRows = (Long)sessionFactory.queryResult(e -> {
            return (Long)e.createQuery("SELECT count(*) FROM ReplayerRow e " +
                            " WHERE " +
                            " e.type='http'" +
                            "AND e.recordingId=" + recordingId)
                    .getResultList().get(0);
        })>0;
        return hasRows;
    }

    @Override
    public Response findRequestMatch(Request req,String contentHash) throws Exception {
        if(!hasRows) return null;
        Response founded = findRequestMatch(req, contentHash,true);
        if(founded==null){
            founded = findRequestMatch(req, contentHash,false);
        }
        return founded;
    }


    private Response findRequestMatch(Request sreq, String contentHash, boolean staticRequest) throws Exception {
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
            staticRequests.addAll(query.getResultList());
        });

        var indexesIds = staticRequests.stream().map(r->r.getIndex().toString()).collect(Collectors.toList());
        var indexes = " e.reference="+String.join(" OR e.reference=",indexesIds);
        var callIndexes = new ArrayList<CallIndex>();
        var queryString = "SELECT e FROM CallIndex  e WHERE " +
                "  (" + indexes + ")" +
                " AND e.recordingId=:recordingId" +
                " ORDER BY e.reference ASC";
        if(indexesIds.size()>0) {
            sessionFactory.query(em -> {
                var query = em.createQuery(queryString);
                query.setParameter("recordingId", name);
                //query.setParameter("reqs",indexes);
                callIndexes.addAll(query.getResultList());
            });
        }

        for (var row : staticRequests) {
            if(!staticRequest){
                var st = states.get(row.getId());
                if(null!=st){
                    continue;
                }
            }
            var rreq = row.getRequest();
            var callIndex = callIndexes.stream().filter(
                    ci->ci.getReference()==row.getId()
            ).findFirst();
            var matchedQuery=0;
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
        if(founded!=null){
            states.put(founded.getId(),"");
        }else{
            return null;
        }
        return founded.getResponse();
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
