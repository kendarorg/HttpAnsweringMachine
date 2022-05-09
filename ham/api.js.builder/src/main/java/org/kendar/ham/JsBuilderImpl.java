package org.kendar.ham;

import java.util.List;

class JsBuilderImpl implements JsBuilder {

    static {
        HamBuilder.register("js.builder", JsBuilderImpl::new);
    }

    private final HamInternalBuilder hamBuilder;

    private JsBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }

    @Override
    public List<String> filterIds() {
        return null;
    }

    @Override
    public FilterDescriptor filterById(String id) {
        return null;
    }

    @Override
    public String addFilter(FilterDescriptor descriptor) {
        return null;
    }

    @Override
    public void deleteFilter(String id) {

    }
}
