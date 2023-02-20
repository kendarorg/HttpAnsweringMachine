package org.kendar.ham;

public interface JsFilterBuilder {
    JsFilterBuilder withMethod(Methods method);
    JsFilterBuilder inPhase(FilterPhase method);
    JsFilterBuilder withHost(String host);
    JsFilterBuilder wihtHostPattern(String host);
    JsFilterBuilder withPath(String host);
    JsFilterBuilder withType(ScriptType type);
    JsFilterBuilder withPathPattern(String host);
    JsFilterBuilder setBlocking();
    JsSourceBuilder withSource();
    Long create() throws HamException;
    JsFilterBuilder verifyHostPattern(String host);
    JsFilterBuilder verifyPathPattern(String host);
}
