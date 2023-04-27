package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.utils.Sleeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class JsFilterBuilderImpl implements JsFilterBuilder, JsSourceBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    private final HamInternalBuilder hamBuilder;
    private final String name;
    private String type = "body";
    private Matcher matcher;
    private String matcherType;

    JsFilterBuilderImpl(HamInternalBuilder hamBuilder, String name) {

        this.hamBuilder = hamBuilder;
        this.name = name;
    }


    private FilterPhase phase;
    private boolean blocking = false;
    private String source;



    @Override
    public JsFilterBuilder inPhase(FilterPhase method) {
        this.phase = method;
        return this;
    }





    @Override
    public JsFilterBuilder withType(ScriptType type) {
        this.type = type.toString();
        return this;
    }



    @Override
    public JsFilterBuilder withApiMatcher(Consumer<ApiMatcherBuilder> matcher) {
        var apiMatcherBuilder = new ApiMatcherBuilder();
        matcher.accept(apiMatcherBuilder);
        this.matcher = apiMatcherBuilder.build();
        this.matcherType = "apimatcher";
        return this;
    }

    @Override
    public JsFilterBuilder withScriptMatcher(Consumer<ScriptMatcherBuilder> matcher) {
        var scriptMatcherBuilder = new ScriptMatcherBuilder();
        matcher.accept(scriptMatcherBuilder);
        this.matcher = scriptMatcherBuilder.build();
        this.matcherType = "scriptmatcher";
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

        try {
            matchers.put(matcherType, mapper.writeValueAsString(matcher));
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
