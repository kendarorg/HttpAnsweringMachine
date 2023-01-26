package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.utils.Sleeper;

import java.util.HashMap;

public class LocalRecording implements HamReplayerRecorderStop,HamReplayerWait {
    private HamReplayerRecorderBuilderImpl builder;
    private String lastUsedType;
    private long lastUsedId;

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    private String parameters;
    private HashMap<String,String> parametersMap = new HashMap<>();
    private long id;
    private ReplayerState state;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ReplayerState getState() {
        return state;
    }

    public void setState(ReplayerState state) {
        this.state = state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private static TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<HashMap<String, String>>() {};

    public LocalRecording withParameter(String name,Object value){

            parametersMap.put(name,value.toString());
        return this;
    }

    public LocalRecording withoutParameter(String name){
            parametersMap.remove(name);
        return this;
    }

    public LocalRecording withoutParameters(){
        parametersMap.clear();
        return this;
    }

    private static ObjectMapper mapper = new ObjectMapper();
    LocalRecording init(HamReplayerRecorderBuilderImpl builder) throws HamException {
        this.builder = builder;
        try {
            if(parameters==null || parameters.isEmpty())parameters="{}";
            parametersMap = mapper.readValue(parameters,typeRef);
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
        return this;
    }

    public void delete() throws HamException
    {
        var request = builder.hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/plugins/replayer/recording/"+id);
        builder.hamBuilder.call(request.build());
    }
    public HamReplayerRecorderStop startRecording() throws HamException
    {
        return (HamReplayerRecorderStop) executeAct("start","record");
    }
    public HamReplayerWait startReplaying() throws HamException
    {
        return (HamReplayerWait)executeAct("start","replay");
    }
    public HamReplayerWait startAutoTest() throws HamException
    {
        return (HamReplayerWait)executeAct("start","auto");
    }

    private LocalRecording executeAct(String action, String usedType) throws HamException {
        lastUsedType = usedType;
        lastUsedId = id;
        SHOULD SEND PARAMETERS
        var request = builder.hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording/"+ id +"/"+usedType+"/"+action);
        builder.hamBuilder.call(request.build());
        return this;
    }
    @Override
    public String stop() throws HamException {
        executeAct("stop",lastUsedType);
        Sleeper.sleep(1000);
        return null;
    }

    @Override
    public boolean isCompleted() throws HamException {
        Sleeper.sleep(500);
        var request = builder.hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/status");
        var status = builder.hamBuilder.callJson(request.build(), HamReplayerRecorderBuilderImpl.ReplayerStatus.class);
        return status.getStatus().equalsIgnoreCase("none");
    }
}
