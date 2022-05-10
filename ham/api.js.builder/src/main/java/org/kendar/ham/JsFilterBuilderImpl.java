package org.kendar.ham;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JsFilterBuilderImpl implements JsFilterBuilder,JsSourceBuilder{
    private ObjectMapper mapper = new ObjectMapper();

    private HamInternalBuilder hamBuilder;
    private String id;

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
    private List<String> source = new ArrayList<>();

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
        this.source = new ArrayList<>();
        return this;
    }

    @Override
    public JsSourceBuilder addLine(String line) {
        source.add(line);
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
        data.setMethod(this.method);
        data.setBlocking(this.blocking);
        data.setHostAddress(this.hostAddress);
        data.setHostRegexp(this.hostRegexp);
        data.setPathAddress(this.pathAddress);
        data.setPathRegexp(this.pathRegexp);
        data.setPhase(this.phase);
        data.setPriority(0);
        data.setRequires(new ArrayList<>());
        data.setSource(this.source);
        data.setId(this.id);
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/plugins/jsfilter/filters/"+id)
                .withJsonBody(data);
         hamBuilder.call(request.build());
        return this.id;
    }
}
