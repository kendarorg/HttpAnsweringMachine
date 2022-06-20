package org.kendar.ham;

import org.kendar.utils.Sleeper;

import java.util.List;
import java.util.stream.Collectors;

class JsBuilderImpl implements JsBuilder {

    private final HamInternalBuilder hamBuilder;

    public JsBuilderImpl(HamInternalBuilder hamBuilder){
        this.hamBuilder = hamBuilder;
    }

    @Override
    public List<String> filterIds() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/jsfilter/filters");
        return hamBuilder.callJsonList(request.build(), String.class).stream().collect(Collectors.toList());
    }

    @Override
    public FilterDescriptor filterById(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/plugins/jsfilter/filters/"+id);
        return hamBuilder.callJson(request.build(), FilterDescriptor.class);
    }

    @Override
    public JsFilterBuilder addFilter(String name) {

        return new JsFilterBuilderImpl(hamBuilder,name);
    }

    @Override
    public void deleteFilter(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/plugins/jsfilter/filters/"+id);
        hamBuilder.call(request.build());
        Sleeper.sleep(500);
    }

    @Override
    public JsBuilderImpl init() {
        return null;
    }
}
