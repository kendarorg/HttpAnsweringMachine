package org.kendar.replayer.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;

@Component

@Entity
@Table(name="REPLAYER_ROW")
public class ReplayerRow {
    //FIXME SHOULD ADD A DECENT ID

    private static ObjectMapper mapper = new ObjectMapper();

    @Column(name="timestamp")
    private Timestamp timestamp = Timestamp.from(Calendar.getInstance().toInstant());

    public String getRequestSerialized() {
        return requestSerialized;
    }

    public void setRequestSerialized(String requestSerialized) {
        this.requestSerialized = requestSerialized;
    }

    @Column(name="requestSerialized")
    @Lob
    @JsonIgnore
    private String requestSerialized;

    public String getResponsSerialized() {
        return responsSerialized;
    }

    public void setResponsSerialized(String responsSerialized) {
        this.responsSerialized = responsSerialized;
    }

    @Column(name="responsSerialized")
    @Lob
    @JsonIgnore
    private String responsSerialized;


    @Column(name="requestHash")
    private String requestHash;

    public long getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(long recordingId) {
        this.recordingId = recordingId;
    }

    @Column(name="recordingId")
    private long recordingId;



    @Column(name="responseHash")
    private String responseHash;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

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

    @Column(name = "stimulatedTest")
    private boolean stimulatedTest;

    @Column(name = "staticRequest")
    private boolean staticRequest;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Request getRequest() {
        try {
            return mapper.readValue(requestSerialized,Request.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRequest(Request request) {
        try {
            this.requestSerialized = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Response getResponse() {
        try {
            return mapper.readValue(responsSerialized,Response.class);
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

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
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

    @Column(name="done")
    private boolean done =false;
    public void markAsDone() {
        done =true;
    }

    public boolean done(){
        return done;
    }

    public boolean isStimulatedTest() {
        return stimulatedTest;
    }

    public void setStimulatedTest(boolean stimulatedTest) {
        this.stimulatedTest = stimulatedTest;
    }

}
