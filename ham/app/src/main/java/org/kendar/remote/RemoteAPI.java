package org.kendar.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.cert.ocsp.Req;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.config.SSLDomain;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class RemoteAPI implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    private EventQueue eventQueue;

    public RemoteAPI(EventQueue eventQueue){

        this.eventQueue = eventQueue;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/remote/execute",
            method = "POST",
            id = "1002a4b4-ffff-11ec-9621-0242ac130002")
    @HamDoc(
            tags = {"base/utils"},
            description = "Execute remote request",
            requests = @HamRequest(
                    body = Request.class
            ),
            responses = @HamResponse(
                    body = Response.class
            ))
    public void executeOnHam(Request req, Response res) throws Exception {
        var realRequest = mapper.readValue(req.getRequestText(), Request.class);
        var event = new ExecuteRemoteRequest();
        event.setRequest(realRequest);
        var result = eventQueue.execute(event,Response.class);
        res.setResponseText(mapper.writeValueAsString(result));
        res.setStatusCode(200);
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/remote/mirror",
            method = "*",
            id = "1002a4b4-ffff-11ec-99866359242ac130002")
    @HamDoc(
            tags = {"base/utils"},
            description = "Returns the data sent by the caller",
            requests = @HamRequest(
                    body = Request.class
            ),
            responses = @HamResponse(
                    body = Response.class
            ))
    public void mirrorRequest(Request req, Response res) throws Exception {
        res.setResponseText(mapper.writeValueAsString(req));
        res.setStatusCode(200);
    }
}
