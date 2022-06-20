package org.kendar.servers.http;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.HttpResponse;

import java.io.IOException;

public interface RequestResponseBuilder {
    Request fromExchange(HttpExchange exchange, String protocol) throws IOException, FileUploadException;
    boolean isMultipart(Request request);
    boolean hasBody(Request request);
    boolean hasBody(Response request);

    void fromHttpResponse(HttpResponse httpResponse, Response response) throws IOException;
}
