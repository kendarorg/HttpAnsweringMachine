package org.kendar.replayer.storage.db;

import org.kendar.janus.cmd.Exec;
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
    private Map<String,DbTreeItem> items= new HashMap<>();
    private JsonTypedSerializer serializer = new JsonTypedSerializer();

    public void loadDb(Long recordingId) throws Exception {

        ArrayList<CallIndex> indexes = new ArrayList<>();
        Map<Long,DbRow> rows = new HashMap<>();
        sessionFactory.query(e -> {
            indexes.addAll(e.createQuery("SELECT e FROM CallIndex e LEFT JOIN ReplayerRow f " +
                    " ON e.reference = f.id"+
                    " WHERE " +
                    " f.type='db' AND e.recordingId=" + recordingId +
                    " AND e.stimulatorTest=true ORDER BY e.id ASC").getResultList());
        });

        for(var index :indexes){
            sessionFactory.query(e -> {
                var row =(ReplayerRow) e.createQuery("SELECT e FROM ReplayerRow e " +
                        " WHERE " +
                        " e.id ="+index.getReference()+" " +
                        "AND e.recordingId=" + recordingId).getResultList().get(0);
                var reqDeser = serializer.newInstance();
                reqDeser.deserialize(row.getRequest().getRequestText());
                var resDeser = serializer.newInstance();
                resDeser.deserialize(row.getResponse().getResponseText());

                rows.put(row.getId(),
                        new DbRow(row,
                                (JdbcCommand) reqDeser.read("command"),
                                (JdbcResult) resDeser.read("result")));
            });
        }

        for(var index:indexes){
            var row = rows.get(index.getReference());
            var dbName = row.getRow().getRequest().getPathParameter("dbName").toLowerCase(Locale.ROOT);
            if(!databases.containsKey(dbName)){
                databases.put(dbName,new DbTreeItem());
            }




            var dbParent = databases.get(dbName);

            //If it is a connection request the parent is the databse
            //Else should find the parent
            if(!(row.getRequest() instanceof ConnectionConnect)){
                row.
                var targetItemId = dbName+"-"+row.getConnectionId()+"-"+row.getTraceId();
                var targetItem = items.get(targetItemId);
                var resultObject = newItem.getTarget().getResponse();
                var resultInitMethod = Arrays.stream(resultObject.getClass().getMethods()).filter(m->
                        m.getName().equalsIgnoreCase("getTraceId") &&
                                m.getParameterCount()==0).findFirst();
                if(resultInitMethod.isPresent()){
                    //If it's a tracked result add it to items
                    var resultItemId = dbName+"-"+row.getConnectionId()+"-"+resultInitMethod.get()
                            .invoke(resultObject,new Object[]{});
                    items.put(resultItemId,newItem);
                }
                //Anyway add it to parent
                targetItem.addChild(newItem);
            }else{
                dbParent.addTarget(row);
            }
        }
    }

    private Map<Long,Long> connectionShadow = new HashMap<>();
    private Map<Long,List<Long>> connectionRealPath = new HashMap<>();
    private AtomicLong atomicLong = new AtomicLong(Long.MAX_VALUE);

    @Override
    public ReplayerRow findRequestMatch(Request req,String contentHash) throws Exception {
        if(req.getPathParameter("dbName")==null)return null;
        if(!req.getPath().startsWith("/api/db/"))return null;
        var reqDeser = serializer.newInstance();
        reqDeser.deserialize(req.getRequestText());
        var command = (JdbcCommand) reqDeser.read("command");
        var dbName = req.getPathParameter("dbName").toLowerCase(Locale.ROOT);
        var db = databases.get(dbName);
        if(command instanceof ConnectionConnect){
            var newConnectionId = atomicLong.decrementAndGet();

        }
        return null;
    }
}
