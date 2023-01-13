package org.kendar.replayer;

import org.kendar.janus.cmd.interfaces.JdbcCommand;
import org.kendar.janus.results.JdbcResult;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.HashMap;

public class ReqRespBuilder {
    public static ReqRespBuilder create(String db){
        return new ReqRespBuilder().withDb(db);
    }
    private static JsonTypedSerializer serializer = new  JsonTypedSerializer();
    private String db;
    private JdbcCommand command;
    private JdbcResult result;
    private long connection;
    private long traceId;
    private boolean isstatic;
    private boolean isstimulator;

    public ReqRespBuilder withDb(String db){
        this.db= db;
        return this;
    }
    public ReqRespBuilder withCommand(JdbcCommand command){
        this.command= command;
        return this;
    }
    public ReqRespBuilder withResult(JdbcResult result){
        this.result= result;
        return this;
    }
    public ReqRespBuilder asStatic() {
        this.isstatic = true;
        return this;
    }
    public ReqRespBuilder asStimulator(){
        this.isstimulator=true;
        return this;
    }
    public ReqRespBuilder withConnectionTrace(long connection,long traceId){
        this.connection= connection;
        this.traceId= traceId;
        return this;
    }

    public ReplayerRow buildRow(long id){
        var result = new ReplayerRow();
        result.setType("db");
        result.setIndex(id);
        result.setId(id);

        var req = new Request();
        req.setStaticRequest(isstatic);
        var commandPath = command.getPath().substring(1).split("/");
        var pathParameters = new HashMap<String,String>();
        pathParameters.put("dbName","test");
        pathParameters.put("targetType",commandPath[0]);
        pathParameters.put("command",commandPath[1]);
        var path = "/api/db/"+pathParameters.get("dbName")+ command.getPath();
        if(traceId>=0){
            path+="/"+traceId;
            pathParameters.put("traceId",traceId+"");
        }
        req.setPathParameters(pathParameters);
        req.addHeader("x-connection-id",""+id);
        var ser = serializer.newInstance();
        ser.write("command", command);
        req.setRequestText((String)ser.getSerialized());
        req.setPath(path);
        result.setRequest(req);


        var res= new Response();
        var dser = serializer.newInstance();
        dser.write("result", this.result);
        res.setResponseText((String)ser.getSerialized());
        result.setResponse(res);
        return result;
    }

    public CallIndex buildIndex(long id){
        var result = new CallIndex();
        result.setReference(id);
        result.setIndex(id);
        result.setId(id);
        result.setStimulatorTest(isstimulator);
        return result;
    }

    public void build(long id,FakeDbReplayer target) {
        target.callIndexMap.put(id,buildIndex(id));
        target.replayerRowMap.put(id,buildRow(id));
    }
}
