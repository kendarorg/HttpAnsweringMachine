package org.kendar.replayer.storage;

import org.kendar.servers.db.DbTable;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component

@Entity
@Table(name="REPLAYER_RESULT")
public class TestResults implements DbTable {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "isoDate")
    private String isoDate;

    @Column(name = "recordingId")
    private Long recordingId;

    @Column(name = "timestamp")
    private Timestamp timestamp;

    @Column(name = "duration")
    private long duration;

    @Column(name = "errors",length = 64000)
    private String errors;

    @Column(name = "type")
    private String type;

    public Long getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(Long recordingId) {
        this.recordingId = recordingId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        if(timestamp!=null){
            Date date=new Date(timestamp.getTime());

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
