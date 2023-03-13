package org.kendar.servers.logging;


import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;


@Component
@Entity
@Table(name = "LOGS_DATA")
public class LoggingDataTable implements DbTable {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String content) {
        this.response = content;
    }

    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "response")
    @Lob
    private String response;
    @Column(name = "request")
    @Lob
    private String request;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
