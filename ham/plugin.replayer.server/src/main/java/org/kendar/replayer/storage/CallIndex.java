package org.kendar.replayer.storage;

import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component

@Entity
@Table(name="REPLAYER_CALL_INDEX")
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

    @Column(name="recordingId")
    private long recordingId;

    @Column(name = "reference")
    private long reference;

    @Column(name = "description",length = 1024)
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

    @Column(name = "pactTest")
    private boolean pactTest;


    @Column(name = "stimulatorTest")
    private boolean stimulatorTest;

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

    public boolean isPactTest() {
        return pactTest;
    }

    public void setPactTest(boolean pactTest) {
        this.pactTest = pactTest;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }
}
