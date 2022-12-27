package org.kendar.replayer.storage.db;

import org.kendar.janus.cmd.JdbcCommand;
import org.kendar.janus.cmd.connection.ConnectionConnect;
import org.kendar.janus.results.JdbcResult;
import org.kendar.janus.results.ObjectResult;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerEngine;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DbReplayer implements ReplayerEngine {
    public ReplayerEngine create(){
        return new DbReplayer(sessionFactory);
    }

    public DbReplayer(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String getId(){
        return "db";
    }



    private HibernateSessionFactory sessionFactory;
    private Map<String,DbTreeItem> databases= new HashMap<>();
    private JsonTypedSerializer serializer = new JsonTypedSerializer();



    private DbTreeItem getLastWithConnectionId(DbTreeItem dbParent, long connectionId) {
        for(var child:dbParent.getChildren()){
            for(var target:child.getTargets()){
                if(target.getConnectionId()==connectionId){
                    return getLastWithConnectionId(child,connectionId);
                }
            }
        }
        return dbParent;
    }
    public void loadDb(Long recordingId) throws Exception {


        ArrayList<CallIndex> indexes = new ArrayList<>();
        Map<String,Map<Long,List<DbRow>>> rows = new HashMap<>();

        loadIndexes(recordingId, indexes);
        loadRowsByConnection(recordingId, indexes, rows);

        for(var db:rows.entrySet()){
            if(!databases.containsKey(db.getKey())){
                databases.put(db.getKey(),new DbTreeItem());
            }
            for(var connection:db.getValue().entrySet()){
                for(var dbRow:connection.getValue()){
                    var dbParent = databases.get(db.getKey());
                    //var targetItemId = db.getKey()+"-"+dbRow.getConnectionId()+"-"+dbRow.getTraceId();
                    if(dbRow.getRequest() instanceof ConnectionConnect){
                        dbParent.addTarget(dbRow);
                        //cache.put(targetItemId,dbRow);
                    }else{
                        var realParent = getLastWithConnectionId(dbParent,dbRow.getConnectionId());

                        var addedAsTarget = false;
                        for (var target : realParent.getTargets()) {
                            //If the request content is the same
                            if (matchesContent(target,dbRow)) {
                                realParent.addTarget(dbRow);
                                addedAsTarget=true;
                                break;
                            }
                        }
                        if(!addedAsTarget){
                            //Is a nex step into the life of the connection
                            var dbTreeItem = new DbTreeItem();
                            dbTreeItem.setParent(realParent);
                            dbTreeItem.addTarget(dbRow);
                            realParent.addChild(dbTreeItem);
                        }
                    }
                }
            }
        }
    }

    private boolean matchesContent(DbRow target, DbRow dbRow) {
        return target.getRow().getRequest().getRequestText().equalsIgnoreCase(
                dbRow.getRow().getRequest().getRequestText());
    }

    private boolean matchesContent(DbRow target, Request request) {
        return target.getRow().getRequest().getRequestText().equalsIgnoreCase(
                request.getRequestText());
    }

    private void loadRowsByConnection(Long recordingId, ArrayList<CallIndex> indexes, Map<String, Map<Long, List<DbRow>>> rows) throws Exception {
        for(var index : indexes){
            sessionFactory.query(e -> {
                var row =(ReplayerRow) e.createQuery("SELECT e FROM ReplayerRow e " +
                        " WHERE " +
                        " e.id ="+index.getReference()+" " +
                        "AND e.recordingId=" + recordingId).getResultList().get(0);
                var reqDeser = serializer.newInstance();
                reqDeser.deserialize(row.getRequest().getRequestText());
                var resDeser = serializer.newInstance();
                resDeser.deserialize(row.getResponse().getResponseText());


                var dbRowName = row.getRequest().getPathParameter("dbName").toLowerCase(Locale.ROOT);
                var dbRow = new DbRow(row,
                        (JdbcCommand) reqDeser.read("command"),
                        (JdbcResult) resDeser.read("result"));
                var dbRowConnectionId = dbRow.getConnectionId();
                if(!rows.containsKey(dbRowName)){
                    rows.put(dbRowName,new HashMap<>());
                }

                if(!rows.get(dbRowName).containsKey(dbRowConnectionId)){
                    rows.get(dbRowName).put(dbRowConnectionId,new ArrayList<>());
                }
                rows.get(dbRowName).get(dbRowConnectionId).add(dbRow);
            });
        }
    }

    private void loadIndexes(Long recordingId, ArrayList<CallIndex> indexes) throws Exception {
        sessionFactory.query(e -> {
            indexes.addAll(e.createQuery("SELECT e FROM CallIndex e LEFT JOIN ReplayerRow f " +
                    " ON e.reference = f.id"+
                    " WHERE " +
                    " f.type='db' AND e.recordingId=" + recordingId +
                    " AND e.stimulatorTest=true ORDER BY e.id ASC").getResultList());
        });
    }

    private Map<Long,Long> connectionShadow = new HashMap<>();
    private Map<Long,List<DbTreeItem>> connectionRealPath = new HashMap<>();
    private AtomicLong atomicLong = new AtomicLong(Long.MAX_VALUE);

    @Override
    public Response findRequestMatch(Request req, String contentHash) throws Exception {
        if(req.getPathParameter("dbName")==null)return null;
        if(!req.getPath().startsWith("/api/db/"))return null;
        var reqDeser = serializer.newInstance();
        reqDeser.deserialize(req.getRequestText());
        var command = (JdbcCommand) reqDeser.read("command");
        var dbName = req.getPathParameter("dbName").toLowerCase(Locale.ROOT);
        var db = databases.get(dbName);
        if(command instanceof ConnectionConnect){
            var newConnectionId = atomicLong.decrementAndGet();
            if(db.getTargets().stream().anyMatch(t->!t.isVisited())){
                var firstNotVisited = db.getTargets().stream().filter(t->!t.isVisited())
                        .findFirst().get();
                firstNotVisited.setVisited(true);
                connectionRealPath.put(newConnectionId,new ArrayList<>());
                connectionRealPath.get(newConnectionId).add(db.getChildren().get(0));
                var result = new ObjectResult();
                result.setResult(newConnectionId);
                return serialize(result);
            }else{
                throw new Exception("No more recordings available or static connections");
            }
        }else{
            var connectionId = Long.parseLong(req.getHeader("x-connection-id"));
            var path = connectionRealPath.get(connectionId);
            var last = path.get(path.size()-1);
            for(var child:last.getChildren()){
                for(var target:child.getTargets()){
                    if(matchesContent(target,req) && !target.isVisited()){
                        var result = target.getResponse();
                        if(child.getChildren().size()==0){
                            if(target.getRow().isStaticRequest()){
                                //Clean the path for static requests
                                for(var i=0;i<path.size();i++){
                                    for(var j=0;j<path.get(i).getTargets().size();j++){
                                        var tth = path.get(i).getTargets().get(j);
                                        if(tth.isVisited()){
                                            tth.setVisited(false);
                                            break;
                                        }
                                    }
                                }
                            }else{
                                target.setVisited(true);
                            }
                        }
                        path.add(child);
                        return serialize(result);
                    }
                }
            }

            //No matches
        }
        return null;
    }

    private Response serialize(Object result) {
        var ser = serializer.newInstance();
        ser.write("result", result);
        var res = new Response();
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText((String) ser.getSerialized());
        return res;
    }
}
