package org.kendar.remote;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.util.encoders.Base64Encoder;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class RestClientApi implements FilteringClass {
    private EventQueue eventQueue;

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    public RestClientApi(EventQueue eventQueue) {

        this.eventQueue = eventQueue;
    }


    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/remote/restclient",
            method = "POST")
    @HamDoc(
            description = "Execute",

            requests = @HamRequest(
                    body = String.class,
                    accept = "text/plain",
                    examples = {
                            @Example(
                                    description = "POST",
                                    example = "POST http://www.google.com?q=test\n" +
                                            "Content-Type:application/json\n" +
                                            "\n" +
                                            "{'key':'value'}"
                            ),
                            @Example(
                                    description = "GET",
                                    example = "GET http://www.google.com?q=test\n" +
                                            "Accept:application/json"
                            )}
            ),
            responses = @HamResponse(
                    body = String.class,
                    content = "text/plain",
                    examples = {@Example(
                            description = "Binary responese (data is Base64 encoded)",
                            example = "200\n" +
                                    "Content-type: application/octect-stream\n" +
                                    "\n" +
                                    "B64ASERTSDGSDY45645sgd45s34stfdgsd"
                    ), @Example(
                            description = "Simple text",
                            example = "200\n" +
                                    "Content-type: text/plain\n" +
                                    "\n" +
                                    "Some Text"
                    )
                    }

            ), tags = {"base/utils"}
    )
    public void restClientRequest(Request request, Response response) throws Exception {
        var parser = new RestClientParser();
        Request call = parser.parse(request.getRequestText());
        var event = new ExecuteLocalRequest();
        event.setRequest(call);
        var result = eventQueue.execute(event, Response.class);
        var stringResult = new StringBuffer();
        stringResult.append(result.getStatusCode() + "\n");
        for (var head : result.getHeaders().entrySet()) {
            stringResult.append(head.getKey() + ":" + head.getValue() + "\n");
        }
        stringResult.append("\n");
        if (result.isBinaryResponse()) {
            stringResult.append(Base64.encodeBase64String(result.getResponseBytes()));
        } else {
            stringResult.append(result.getResponseText());
        }
        response.setResponseText(stringResult.toString());

    }
}
