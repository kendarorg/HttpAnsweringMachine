package org.kendar.ham;

public interface JsFilterBuilder {
    JsFilterBuilder withMethod(Methods method);
    JsFilterBuilder inPhase(FilterPhase method);
    JsFilterBuilder withHost(String host);
    JsFilterBuilder withHostRegexp(String host);
    JsFilterBuilder withPath(String host);
    JsFilterBuilder withPathRegexp(String host);
    JsFilterBuilder setBlocking();
    JsSourceBuilder withSource();
    String create() throws HamException;
    JsFilterBuilder verifyHostRegexp(String host);
    JsFilterBuilder verifPathRegexp(String host);
}
