package org.kendar.replayer.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.db.DbTable;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Calendar;

@Component

@Entity
@Table(name = "REPLAYER_ROW")
public class ReplayerRow implements DbTable {

    private static ObjectMapper mapper = new ObjectMapper();
    @Column(name = "type")
    private String type;
    @Column(name = "binaryRequest")
    private boolean binaryRequest;
    @Column(name = "path", length = 6400)
    private String path;
    //FIXME SHOULD ADD A DECENT ID
    @Column(name = "host", length = 6400)
    private String host;
    @Column(name = "query", length = 6400)
    private String query;
    @Column(name = "timestamp")
    private long timestamp = Calendar.getInstance().getTimeInMillis();
    @Column(name = "requestSerialized", columnDefinition = "CLOB")
    @JsonIgnore
    private String requestSerialized;
    @Column(name = "responsSerialized", columnDefinition = "CLOB")
    @Lob
    @JsonIgnore
    private String responsSerialized;
    @Column(name = "requestHash")
    private String requestHash;
    @Column(name = "recordingId")
    private long recordingId;
    @Column(name = "responseHash")
    private String responseHash;
    @Column(name = "id")
    private long id;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "index")
    private Long index;
    @Column(name = "staticRequest")
    private boolean staticRequest;
    @Column(name = "done")
    private boolean done = false;

    public boolean isBinaryRequest() {
        return binaryRequest;
    }

    public void setBinaryRequest(boolean binaryRequest) {
        this.binaryRequest = binaryRequest;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getRequestSerialized() {
        return requestSerialized;
    }

    public void setRequestSerialized(String requestSerialized) {
        this.requestSerialized = requestSerialized;
    }

    public String getResponsSerialized() {
        return responsSerialized;
    }

    public void setResponsSerialized(String responsSerialized) {
        this.responsSerialized = responsSerialized;
    }

    public long getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(long recordingId) {
        this.recordingId = recordingId;
    }

    public boolean isStaticRequest() {
        return staticRequest;
    }

    public void setStaticRequest(boolean staticRequest) {
        this.staticRequest = staticRequest;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Request getRequest() {
        try {
            return mapper.readValue(requestSerialized, Request.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRequest(Request request) {
        try {
            this.host = request.getHost();
            this.path = request.getPath();
            this.binaryRequest = request.isBinaryRequest();
            this.requestSerialized = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Response getResponse() {
        try {
            return mapper.readValue(responsSerialized, Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setResponse(Response response) {
        try {
            this.responsSerialized = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public String getResponseHash() {
        return responseHash;
    }

    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }

    public void markAsDone() {
        done = true;
    }

    public boolean done() {
        return done;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
