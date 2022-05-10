package org.kendar.ham;

public interface JsSourceBuilder {
    JsSourceBuilder addLine(String line);
    JsFilterBuilder closeBlocking();
    JsFilterBuilder closeNonBlocking();
}
