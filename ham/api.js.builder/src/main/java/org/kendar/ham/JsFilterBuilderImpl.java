package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.utils.Sleeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class JsFilterBuilderImpl implements JsFilterBuilder, JsSourceBuilder {
    private ObjectMapper mapper = new ObjectMapper();

    private HamInternalBuilder hamBuilder;
    private String name;
    private String type = "body";

    JsFilterBuilderImpl(HamInternalBuilder hamBuilder, String name) {

        this.hamBuilder = hamBuilder;
        this.name = name;
    }

    private Methods method;
    private String hostAddress;
    private String hostPattern;
    private String pathAddress;
    private String pathPattern;
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
    public JsFilterBuilder wihtHostPattern(String host) {
        this.hostPattern = host;
        return this;
    }

    @Override
    public JsFilterBuilder verifyHostPattern(String host) {
        var pattern = Pattern.compile(hostPattern);
        var matcher = pattern.matcher(host);
        if (!matcher.matches()) {
            throw new RuntimeException(host + " does not match " + hostPattern);
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
    public JsFilterBuilder withPathPattern(String host) {
        this.pathPattern = host;
        return this;
    }

    @Override
    public JsFilterBuilder verifyPathPattern(String host) {
        var pattern = Pattern.compile(pathPattern);
        var matcher = pattern.matcher(host);
        if (!matcher.matches()) {
            throw new RuntimeException(host + " does not match " + hostPattern);
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
        source = "";
        return this;
    }

    @Override
    public JsSourceBuilder addLine(String line) {
        source += line + "\n";
        return this;
    }

    @Override
    public JsFilterBuilder closeBlocking() {

        return (JsFilterBuilder) addLine("return false;");
    }

    @Override
    public JsFilterBuilder closeNonBlocking() {
        return (JsFilterBuilder) addLine("return true;");
    }

    @Override
    public Long create() throws HamException {
        var data = new JsBuilder.FilterDescriptor();
        var matchers = new HashMap<String, String>();
        var matcher = new JsBuilder.ApiMatcher();
        matcher.setMethod(this.method);
        matcher.setHostAddress(this.hostAddress);
        matcher.setHostPattern(this.hostPattern);
        matcher.setPathAddress(this.pathAddress);
        matcher.setPathPattern(this.pathPattern);
        try {
            matchers.put("apimatcher", mapper.writeValueAsString(matcher));
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
        data.setMatchers(matchers);
        data.setBlocking(this.blocking);
        data.setPhase(this.phase);
        data.setType(this.type);
        data.setPriority(0);
        data.setRequire(new ArrayList<>());
        data.setSource(this.source);
        data.setName(this.name);
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/plugins/jsfilter/filters")
                .withJsonBody(data);
        var res = hamBuilder.call(request.build());
        Sleeper.sleep(500);
        return Long.parseLong(res.getResponseText());
    }
}
