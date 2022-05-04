package org.kendar.ham;

import java.util.List;

import static org.kendar.ham.HamBuilder.pathId;
import static org.kendar.ham.HamBuilder.updateMethod;

class ProxyBuilderImpl implements ProxyBuilder{
    private HamBuilder hamBuilder;

    public ProxyBuilderImpl(HamBuilder hamBuilder) {

        this.hamBuilder = hamBuilder;
    }

    @Override
    public String addProxy(String when, String where, String test) throws HamException {
        var alreadyExisting = retrieveProxies()
                .stream().filter(d-> d.getWhen().equalsIgnoreCase(when)).findAny();
        var proxy = new Proxy();
        proxy.setTest(test);
        proxy.setWhen(when);
        proxy.setWhere(where);
        proxy.setId(alreadyExisting.isPresent()? alreadyExisting.get().getId() :null);
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/proxyes",
                        alreadyExisting,
                        ()-> alreadyExisting.get().getId()))
                .withJsonBody(proxy);

        hamBuilder.call(request.build());
        var inserted = retrieveProxies()
                .stream().filter(d-> d.getWhen().equalsIgnoreCase(when)).findAny();
        if(inserted.isPresent()){
            return inserted.get().getId();
        }
        throw new HamException("Missing id");
    }

    @Override
    public void removeProxy(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/proxyes/"+id);
        hamBuilder.call(request.build());
    }

    @Override
    public List<Proxy> retrieveProxies() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/proxyes");
        return hamBuilder.callJsonList(request.build(), Proxy.class);
    }

    @Override
    public void refresh() throws HamException {
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/proxyes");
        hamBuilder.call(request.build());
    }
}
