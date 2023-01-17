package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.utils.Sleeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class JsFilterBuilderImpl implements JsFilterBuilder,JsSourceBuilder{
    private ObjectMapper mapper = new ObjectMapper();

    private HamInternalBuilder hamBuilder;
    private String id;
    private String type;

    JsFilterBuilderImpl(HamInternalBuilder hamBuilder, String name){

        this.hamBuilder = hamBuilder;
        id = name;
    }
    private Methods method;
    private String hostAddress;
    private String hostRegexp;
    private String pathAddress;
    private String pathRegexp;
    private FilterPhase phase;
    private boolean blocking = false;
    private String source;

    @Override
    public JsFilterBuilder withMethod(Methods method) {
        this.method = method;
        return this;
    }

    @Override
    public JsFilterBuilder inPhase(FilterPhase method) {
        this.phase = method;
        return this;
    }

    @Override
    public JsFilterBuilder withHost(String host) {
        this.hostAddress = host;
        return this;
    }

    @Override
    public JsFilterBuilder withHostRegexp(String host) {
        this.hostRegexp = host;
        return this;
    }
    @Override
    public JsFilterBuilder verifyHostRegexp(String host) {
        var pattern = Pattern.compile(hostRegexp);
        var matcher = pattern.matcher(host);
        if(!matcher.matches()){
            throw new RuntimeException(host+ " does not match "+hostRegexp);
        }
        return this;
    }

    @Override
    public JsFilterBuilder withPath(String host) {
        this.pathAddress = host;
        return this;
    }

    @Override
    public JsFilterBuilder withType(ScriptType type) {
        this.type = type.toString();
        return this;
    }

    @Override
    public JsFilterBuilder withPathRegexp(String host) {
        this.pathRegexp = host;
        return this;
    }
    @Override
    public JsFilterBuilder verifPathRegexp(String host) {
        var pattern = Pattern.compile(pathRegexp);
        var matcher = pattern.matcher(host);
        if(!matcher.matches()){
            throw new RuntimeException(host+ " does not match "+hostRegexp);
        }
        return this;
    }

    @Override
    public JsFilterBuilder setBlocking() {
        this.blocking = true;
        return this;
    }

    @Override
    public JsSourceBuilder withSource() {
        return this;
    }

    @Override
    public JsSourceBuilder addLine(String line) {
        source+=line+"\n";
        return this;
    }

    @Override
    public JsFilterBuilder closeBlocking() {

        return (JsFilterBuilder)addLine("return false;");
    }

    @Override
    public JsFilterBuilder closeNonBlocking() {
        return (JsFilterBuilder)addLine("return true;");
    }

    @Override
    public String create() throws HamException {
        var data = new JsBuilder.FilterDescriptor();
        var matchers = new HashMap<String,String>();
        var matcher = new JsBuilder.ApiMatcher();
        matcher.setMethod(this.method);
        matcher.setHostAddress(this.hostAddress);
        matcher.setHostRegexp(this.hostRegexp);
        matcher.setPathAddress(this.pathAddress);
        matcher.setPathRegexp(this.pathRegexp);
        try {
            matchers.put("apimatcher",mapper.writeValueAsString(matcher));
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
        data.setBlocking(this.blocking);
        data.setPhase(this.phase);
        data.setType(this.type);
        data.setPriority(0);
        data.setRequires(new ArrayList<>());
        data.setSource(this.source);
        data.setId(this.id);
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/plugins/jsfilter/filters/"+id)
                .withJsonBody(data);
         hamBuilder.call(request.build());
        Sleeper.sleep(500);
        return this.id;
    }
}
