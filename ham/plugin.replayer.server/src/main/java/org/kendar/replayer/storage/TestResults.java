package org.kendar.replayer.storage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TestResults {
    private String isoDate;
    private String recordingId;
    private Calendar timestamp;
    private long duration;
    private String errors;
    private List<Integer> executed = new ArrayList<>();
    private String type;

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = recordingId;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
        if(timestamp!=null){
            Date date = timestamp.getTime();

// Conversion
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            isoDate = sdf.format(date);
        }
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getError() {
        return errors;
    }

    public void setError(String errors) {
        this.errors = errors;
    }

    public List<Integer> getExecuted() {
        return executed;
    }

    public void setExecuted(List<Integer> executed) {
        this.executed = executed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsoDate() {
        return isoDate;
    }

    public void setIsoDate(String isoDate) {
        this.isoDate = isoDate;
    }
}
