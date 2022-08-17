package org.kendar.servers.proxy.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.proxy.ProxyConfigChanged;
import org.kendar.servers.proxy.RemoteServerStatus;
import org.kendar.servers.proxy.SimpleProxyConfig;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ProxyHandlerApis implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;

    public ProxyHandlerApis(JsonConfiguration configuration, EventQueue eventQueue) {
        this.configuration = configuration;
        this.eventQueue = eventQueue;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/proxyes",
            method = "GET")
    @HamDoc(
            tags = {"base/proxy"},
            description = "Retrieve all configured proxies",
            responses = @HamResponse(
                    body = RemoteServerStatus[].class
            ))
    public void getProxies(Request req, Response res) throws JsonProcessingException {
        var proxyes = configuration.getConfiguration(SimpleProxyConfig.class).getProxies();
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(proxyes));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "GET")
    @HamDoc(
            tags = {"base/proxy"},
            path = @PathParameter(key = "id"),
            description = "Retrieve specific proxy data",
            responses = @HamResponse(
                    body = RemoteServerStatus.class
            ))
    public void getProxy(Request req, Response res) throws JsonProcessingException {
        var clone = configuration.getConfiguration(SimpleProxyConfig.class);
        var proxyes = clone.getProxies();
        var id = req.getPathParameter("id");
        for (var item : proxyes) {
            if (item.getId().equalsIgnoreCase(id)) {
                res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
                res.setResponseText(mapper.writeValueAsString(item));
                return;
            }
        }
        res.setStatusCode(404);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "DELETE")
    @HamDoc(
            tags = {"base/proxy"},
            path = @PathParameter(key = "id"),
            description = "Delete specific proxy"
    )
    public void removeProxy(Request req, Response res) {
        var clone = configuration.getConfiguration(SimpleProxyConfig.class).copy();
        var proxyes = clone.getProxies();
        var id = req.getPathParameter("id");
        var newList = new ArrayList<RemoteServerStatus>();
        for (var item : proxyes) {
            if (item.getId().equalsIgnoreCase(id)) {
                continue;
            }
            newList.add(item);
        }
        clone.setProxies(newList);
        configuration.setConfiguration(clone);
        eventQueue.handle(new ProxyConfigChanged());
        res.setStatusCode(200);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/proxyes/{id}",
            method = "PUT")
    @HamDoc(
            tags = {"base/proxy"},
            description = "Modify proxy",
            path = @PathParameter(key = "id"),
            requests = @HamRequest(
                    body = SimpleProxyConfig.class
            ))
    public void updateProxy(Request req, Response res) throws JsonProcessingException {
        var cloneConf = configuration.getConfiguration(SimpleProxyConfig.class).copy();
        var proxyes = cloneConf.getProxies();
        var id = req.getPathParameter("id");
        var newList = new ArrayList<RemoteServerStatus>();
        var newData = mapper.readValue(req.getRequestText(), RemoteServerStatus.class);

        for (var item : proxyes) {
            var clone = item.copy();
            if (!clone.getId().equalsIgnoreCase(id)) {
                newList.add(clone);
                continue;
            }
            clone.setTest(newData.getTest());
            clone.setWhen(newData.getWhen());
            clone.setWhere(newData.getWhere());
            newList.add(clone);
        }
        cloneConf.setProxies(newList);
        configuration.setConfiguration(cloneConf);
        eventQueue.handle(new ProxyConfigChanged());
        res.setStatusCode(200);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/proxyes",
            method = "POST")
    @HamDoc(
            tags = {"base/proxy"},
            description = "Add proxy",
            requests = @HamRequest(
                    body = SimpleProxyConfig.class
            ))
    public void addProxy(Request req, Response res) throws JsonProcessingException {
        var cloneConf = configuration.getConfiguration(SimpleProxyConfig.class).copy();
        var proxyes = cloneConf.getProxies();
        if (req.getRequestText() != null && !req.getRequestText().isEmpty()) {
            var newData = mapper.readValue(req.getRequestText(), RemoteServerStatus.class);
            proxyes.add(newData);
            configuration.setConfiguration(cloneConf);
        }
        eventQueue.handle(new ProxyConfigChanged());
        res.setStatusCode(200);
    }
}
