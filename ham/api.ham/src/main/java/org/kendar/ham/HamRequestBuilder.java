package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.kendar.servers.http.Request;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HamRequestBuilder {


    final ObjectMapper mapper = new ObjectMapper();
    private Request request;

    private HamRequestBuilder() {
    }

    public static HamRequestBuilder newRequest(String protocol, String host) {
        var result = new HamRequestBuilder();
        result.request = new Request();
        result.request.setProtocol(protocol);
        result.request.setHost(host);
        result.request.setMethod("get");
        return result;
    }

    public String getHost() {
        return this.request.getHost();
    }

    public HamRequestBuilder withHost(String host) {
        this.request.setHost(host);
        return this;
    }

    public HamRequestBuilder withProtocol(String host) {
        this.request.setProtocol(host);
        return this;
    }

    public HamRequestBuilder withPort(int port) {
        request.setPort(port);
        return this;
    }

    public HamRequestBuilder withMethod(String method) {
        request.setMethod(method);
        return this;
    }

    public HamRequestBuilder withJsonBody(Object body) {
        String text = null;
        try {
            text = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.withText(text);
        this.withContentType(ConstantsMime.JSON);
        return this;
    }

    public HamRequestBuilder withPost() {
        request.setMethod("POST");
        return this;
    }

    public HamRequestBuilder withHamFile(String fileName, byte[] data, String mime) {

        var fileReq = new FileRequest();
        fileReq.name = fileName;
        fileReq.type = mime;
        fileReq.data = Base64.encodeBase64String(data);
        return this.withPost().withJsonBody(fileReq);
    }

    public HamRequestBuilder withHamFile(String fileName, String data, String mime) {

        return withHamFile(fileName, data.getBytes(StandardCharsets.UTF_8), mime);
    }

    public HamRequestBuilder withPut() {
        request.setMethod("PUT");
        return this;
    }

    public HamRequestBuilder withDelete() {
        request.setMethod("DELETE");
        return this;
    }

    public HamRequestBuilder withPath(String path) {
        request.setPath(path);
        return this;
    }

    public HamRequestBuilder withHeader(String id, String value) {
        request.addHeader(id, value);
        return this;
    }

    public HamRequestBuilder withBasicAuth(String user, String pass) {
        request.setBasicPassword(pass);
        request.setBasicUsername(user);
        return this;
    }

    public HamRequestBuilder withQuery(String id, String value) {
        request.addQuery(id, value);
        return this;
    }

    public HamRequestBuilder withContentType(String value) {
        return withHeader(ConstantsHeader.CONTENT_TYPE, value);
    }

    public HamRequestBuilder withText(String text) {
        request.setRequestText(text);
        request.setBinaryRequest(false);
        return this;
    }

    public HamRequestBuilder withText(InputStream text) throws IOException {
        request.setRequestText(new String(text.readAllBytes()));
        request.setBinaryRequest(false);
        return this;
    }

    public HamRequestBuilder withBytes(byte[] data) {
        request.setRequestBytes(data);
        request.setBinaryRequest(true);
        return this;
    }

    public HamRequestBuilder withBytes(InputStream data) throws IOException {
        request.setRequestBytes(data.readAllBytes());
        request.setBinaryRequest(true);
        return this;
    }

    public Request build() {
        return request;
    }

    public static class FileRequest {
        private String name;
        private String data;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

}
