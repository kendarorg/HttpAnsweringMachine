package org.kendar.replayer.storage;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Calendar;

@Component

@Entity
@Table(name = "REPLAYER_CALL_INDEX")
public class CallIndex implements DbTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "index")
    private Long index;

    @Column(name = "id")
    private Long id;

    public long getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(long recordingId) {
        this.recordingId = recordingId;
    }

    @Column(name = "recordingId")
    private long recordingId;

    @Column(name = "reference")
    private long reference;

    @Column(name = "calls")
    @JsonSetter(nulls = Nulls.SKIP)
    private long calls = 1;

    @Column(name = "description", length = 1024)
    private String description;


    @Column(name = "preScript")
    @Lob
    private String preScript;

    public String getPreScript() {
        return preScript;
    }

    public void setPreScript(String preScript) {
        this.preScript = preScript;
    }

    public String getPostScript() {
        return postScript;
    }

    public void setPostScript(String postScript) {
        this.postScript = postScript;
    }

    @Column(name = "postScript")
    @Lob
    private String postScript;


    @Column(name = "stimulatorTest")
    private boolean stimulatorTest;


    @Column(name = "timestamp")
    private long timestamp = Calendar.getInstance().getTimeInMillis();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getReference() {
        return reference;
    }

    public void setReference(long reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStimulatorTest() {
        return stimulatorTest;
    }

    public void setStimulatorTest(boolean stimulatorTest) {
        this.stimulatorTest = stimulatorTest;
    }


    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCalls() {
        return calls;
    }

    public void setCalls(long calls) {
        this.calls = calls;
    }
}
