package org.kendar.servers.logging;

import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@Entity
@Table(name="LOGS")
public class LoggingTable implements DbTable {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isRequestBody() {
        return requestBody;
    }

    public void setRequestBody(boolean requestBody) {
        this.requestBody = requestBody;
    }

    public boolean isResponseBody() {
        return responseBody;
    }

    public void setResponseBody(boolean responseBody) {
        this.responseBody = responseBody;
    }

    public long getRequestData() {
        return requestData;
    }

    public void setRequestData(long requestData) {
        this.requestData = requestData;
    }

    public long getResponseData() {
        return responseData;
    }

    public void setResponseData(long responseData) {
        this.responseData = responseData;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "protocol")
    private String protocol;
    @Column(name = "address")
    private String address;
    @Column(name = "path")
    private String path;
    @Column(name = "query")
    private String query;
    @Column(name = "contentType")
    private String contentType;
    @Column(name = "requestBody")
    private boolean requestBody;
    @Column(name = "responseBody")
    private boolean responseBody;
    @Column(name = "requestData")
    private long requestData;
    @Column(name = "responseData")
    private long responseData;
}
