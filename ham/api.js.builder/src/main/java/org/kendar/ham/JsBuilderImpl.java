package org.kendar.ham;

class JsBuilderImpl implements JsBuilder {

    static {
        HamBuilder.register("js.builder", (b)-> new JsBuilderImpl(b));
    }

    private final HamInternalBuilder hamBuilder;

    private JsBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }
}
