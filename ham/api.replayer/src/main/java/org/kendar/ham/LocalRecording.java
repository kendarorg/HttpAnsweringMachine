package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.utils.Sleeper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalRecording implements HamReplayerRecorderStop, HamReplayerWait {
    private static final TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<>() {
    };
    private static final ObjectMapper mapper = new ObjectMapper();
    private HamReplayerRecorderBuilderImpl builder;
    private String lastUsedType;
    private long lastUsedId;
    private String parameters;
    private HashMap<String, String> parametersMap = new HashMap<>();
    private long id;
    private ReplayerState state;
    private String name;

    private static String toQuery(Map.Entry<String, String> entry) {
        return entry.getKey() + "=" + entry.getValue();
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalRecording withParameter(ExtraParam name, Object value) {
        parametersMap.put(name.toString(), value.toString());
        return this;
    }

    public LocalRecording withoutParameter(ExtraParam name) {
        parametersMap.remove(name.toString());
        return this;
    }

    public LocalRecording withoutParameters() {
        parametersMap.clear();
        return this;
    }

    LocalRecording init(HamReplayerRecorderBuilderImpl builder) throws HamException {
        this.builder = builder;
        try {
            if (parameters == null || parameters.isEmpty()) parameters = "{}";
            parametersMap = mapper.readValue(parameters, typeRef);
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
        return this;
    }

    public void delete() throws HamException {
        var request = builder.hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/plugins/replayer/recording/" + id);
        builder.hamBuilder.call(request.build());
    }

    public HamReplayerRecorderStop startRecording() throws HamException {
        if (!parametersMap.containsKey(ExtraParam.DB_NAMES.toString())) {
            parametersMap.put(ExtraParam.DB_NAMES.toString(), "*");
        }
        if (!parametersMap.containsKey(ExtraParam.HTTP_HOSTS.toString())) {
            parametersMap.put(ExtraParam.HTTP_HOSTS.toString(), "*");
        }
        return executeAct("start", "record");
    }

    public HamReplayerWait startReplaying() throws HamException {
        return executeAct("start", "replay");
    }

    public HamReplayerWait startAutoTest() throws HamException {
        return executeAct("start", "auto");
    }

    private LocalRecording executeAct(String action, String usedType) throws HamException {
        lastUsedType = usedType;
        lastUsedId = id;

        var extra = String.join("&", parametersMap.entrySet()
                .stream().map(LocalRecording::toQuery).collect(Collectors.toList()));
        if (extra.length() > 0) {
            extra = "?" + extra;
        }

        var request = builder.hamBuilder.newRequest()
                .withPath("/api/plugins/replayer/recording/" + id + "/" + usedType + "/" + action + extra);
        builder.hamBuilder.call(request.build());
        return this;
    }

    @Override
    public String stop() throws HamException {
        executeAct("stop", lastUsedType);
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
