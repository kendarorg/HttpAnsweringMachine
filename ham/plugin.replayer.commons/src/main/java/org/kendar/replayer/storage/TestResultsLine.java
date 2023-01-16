package org.kendar.replayer.storage;

import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component

@Entity
@Table(name="REPLAYER_RESULT_LINE")
public class TestResultsLine implements DbTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public boolean isStimulator() {
        return stimulator;
    }

    public void setStimulator(boolean stimulator) {
        this.stimulator = stimulator;
    }

    @Column(name = "stimulator")
    private boolean stimulator;

    public Long getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(Long recordingId) {
        this.recordingId = recordingId;
    }

    @Column(name="resultId")
    private Long resultId;
    @Column(name="recordingId")
    private Long recordingId;

    @Column(name = "expected",length = 64000)
    private String expectedResponse;

    @Column(name = "actual",length = 64000)
    private String actualResponse;

    public String getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(String expectedResponse) {
        this.expectedResponse = expectedResponse;
    }

    public String getActualResponse() {
        return actualResponse;
    }

    public void setActualResponse(String actualResponse) {
        this.actualResponse = actualResponse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Long getExecutedLine() {
        return executedLine;
    }

    public void setExecutedLine(Long executedLine) {
        this.executedLine = executedLine;
    }

    @Column(name="executedLine")
    private Long executedLine;
}
