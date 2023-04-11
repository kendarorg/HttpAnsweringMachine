package org.kendar.ham;

import org.kendar.servers.dbproxy.DbDescriptor;
import org.kendar.servers.dbproxy.DbProxy;

import java.util.List;
import java.util.UUID;

import static org.kendar.ham.HamBuilder.pathId;
import static org.kendar.ham.HamBuilder.updateMethod;

class ProxyBuilderImpl implements ProxyBuilder, DbProxyBuilder {
    private HamBuilder hamBuilder;
    private DbProxy dbProxy;

    public ProxyBuilderImpl(HamBuilder hamBuilder) {

        this.hamBuilder = hamBuilder;
    }

    @Override
    public String addProxy(String when, String where, String test) throws HamException {
        var proxy = new Proxy();
        var alreadyExisting = retrieveProxies()
                .stream().filter(d -> d.getWhen().equalsIgnoreCase(when)).findAny();
        proxy.setId(alreadyExisting.isPresent() ? alreadyExisting.get().getId() : UUID.randomUUID().toString());
        proxy.setTest(test);
        proxy.setWhen(when);
        proxy.setWhere(where);
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/proxies",
                        alreadyExisting,
                        () -> alreadyExisting.get().getId()))
                .withJsonBody(proxy);

        hamBuilder.call(request.build());
        var inserted = retrieveProxies()
                .stream().filter(d -> d.getWhen().equalsIgnoreCase(when)).findAny();
        if (inserted.isPresent()) {
            return inserted.get().getId();
        }
        inserted = retrieveProxies()
                .stream().filter(d -> d.getWhere().equalsIgnoreCase(where)).findAny();
        if (inserted.isPresent()) {

            return inserted.get().getId();
        }
        throw new HamException("Missing id");
    }

    @Override
    public String addForcedProxy(String when, String where, String test) throws HamException {
        var proxy = new Proxy();
        var alreadyExisting = retrieveProxies()
                .stream().filter(d -> d.getWhen().equalsIgnoreCase(when)).findAny();
        proxy.setId(alreadyExisting.isPresent() ? alreadyExisting.get().getId() : UUID.randomUUID().toString());
        proxy.setTest(test);
        proxy.setWhen(when);
        proxy.setWhere(where);
        proxy.setForce(true);
        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/proxies",
                        alreadyExisting,
                        () -> alreadyExisting.get().getId()))
                .withJsonBody(proxy);

        hamBuilder.call(request.build());
        var inserted = retrieveProxies()
                .stream().filter(d -> d.getWhen().equalsIgnoreCase(when)).findAny();
        if (inserted.isPresent()) {
            return inserted.get().getId();
        }
        inserted = retrieveProxies()
                .stream().filter(d -> d.getWhere().equalsIgnoreCase(where)).findAny();
        if (inserted.isPresent()) {

            return inserted.get().getId();
        }
        throw new HamException("Missing id");
    }



    @Override
    public DbProxyBuilder addRemoteDbProxy(String dbName, String login, String password, String dbDriver) throws HamException {

        var proxy = new DbProxy();
        var dbd = new DbDescriptor();
        dbd.setConnectionString(dbName);
        dbd.setPassword(password);
        dbd.setLogin(login);
        proxy.setDriver(dbDriver);
        proxy.setActive(true);
        proxy.setRemote(dbd);
        dbProxy = proxy;
        return this;
    }

    @Override
    public void removeProxy(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/proxies/" + id);
        hamBuilder.call(request.build());
    }

    @Override
    public List<Proxy> retrieveProxies() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/proxies");
        return hamBuilder.callJsonList(request.build(), Proxy.class);
    }

    @Override
    public Proxy retrieveProxy(String id) throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/proxies/"+id);
        return hamBuilder.callJson(request.build(), Proxy.class);
    }

    @Override
    public void refresh() throws HamException {
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/proxies");
        hamBuilder.call(request.build());
    }

    @Override
    public String asLocal(String dbName, String login, String password) throws HamException {
        var alreadyExisting = retrieveDbProxies()
                .stream().filter(d -> d.getExposed().getConnectionString().equalsIgnoreCase(dbName)).findAny();
        dbProxy.setId(alreadyExisting.isPresent() ? alreadyExisting.get().getId() : UUID.randomUUID().toString());
        var dbd = new DbDescriptor();
        dbd.setConnectionString(dbName);
        dbd.setPassword(password);
        dbd.setLogin(login);
        dbProxy.setExposed(dbd);

        var request = hamBuilder.newRequest()
                .withMethod(updateMethod(alreadyExisting))
                .withPath(pathId(
                        "/api/jdbcproxies/proxies",
                        alreadyExisting,
                        () -> alreadyExisting.get().getId()))
                .withJsonBody(dbProxy);

        hamBuilder.call(request.build());
        var inserted = retrieveDbProxies()
                .stream().filter(d -> d.getExposed().getConnectionString().equalsIgnoreCase(dbName)).findAny();
        if (inserted.isPresent()) {
            return String.format("jdbc:janus:http://%s/api/db/%s", request.getHost(), dbName);
        }
        throw new HamException("Missing id");
    }

    @Override
    public DbProxyBuilder asInactive() {
        this.dbProxy.setActive(false);
        return this;
    }

    @Override
    public void removeDbProxy(String dbId) throws HamException {
        var request = hamBuilder.newRequest()
                .withDelete()
                .withPath("/api/jdbcproxies/proxies/" + dbId);
        hamBuilder.call(request.build());
    }


    @Override
    public List<DbProxy> retrieveDbProxies() throws HamException {
        var request = hamBuilder.newRequest()
                .withPath("/api/jdbcproxies/proxies");
        return hamBuilder.callJsonList(request.build(), DbProxy.class);
    }
}
