package org.kendar.ham;

import java.util.function.Consumer;

public interface JsFilterBuilder {


    JsFilterBuilder inPhase(FilterPhase method);


    JsFilterBuilder withType(ScriptType type);

    JsFilterBuilder setBlocking();

    JsSourceBuilder withSource();

    Long create() throws HamException;


    JsFilterBuilder withApiMatcher(Consumer<ApiMatcherBuilder> matcher);

    JsFilterBuilder withScriptMatcher(Consumer<ScriptMatcherBuilder> matcher);
}
