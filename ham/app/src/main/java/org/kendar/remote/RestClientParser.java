package org.kendar.remote;

import org.apache.http.client.utils.URLEncodedUtils;
import org.kendar.servers.http.Request;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class RestClientParser {
    public Request parse(String requestText) throws URISyntaxException {
        var call = new Request();
        var lines = requestText.trim().split("\\r?\\n");
        var requestLine = lines[0];
        addRequest(call, requestLine);
        var body = new StringBuffer();
        var headersCompleted = false;
        for (var i = 1; i < lines.length; i++) {
            var current = lines[i];

            if (headersCompleted) {
                body.append(current);
                body.append("\n");
            } else {
                var trimmed = current.trim();
                if (trimmed.length() == 0) {
                    headersCompleted = true;
                    continue;
                }
                addHeader(call, current);
            }
        }
        call.setRequestText(body.toString());
        call.setBinaryRequest(false);
        return call;
    }


    private void addRequest(Request call, String requestLine) throws URISyntaxException {
        var space = requestLine.indexOf(' ');
        call.setMethod(requestLine.substring(0, space).trim().toLowerCase(Locale.ROOT));
        var url = new URI(requestLine.substring(space).trim());
        call.setProtocol(url.getScheme().toLowerCase(Locale.ROOT));
        if (url.getPort() > 0) call.setPort(url.getPort());
        call.setHost(url.getHost());
        call.setPath(url.getPath());
        var parsedQuery = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);
        for (var item : parsedQuery) {
            call.addQuery(item.getName(), item.getValue());
        }
    }

    private void addHeader(Request call, String requestLine) {
        var cols = requestLine.indexOf(':');
        var key = requestLine.substring(0, cols);
        var value = requestLine.substring(cols + 1);
        call.addHeader(key, value);
    }
}
