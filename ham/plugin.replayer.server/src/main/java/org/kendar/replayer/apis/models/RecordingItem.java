package org.kendar.replayer.apis.models;

import org.kendar.replayer.storage.TestResultsLine;

import java.util.List;

public class RecordingItem {
    private String date;
    private String testType;
    private String name;
    private Long fileId;
    private boolean successful=false;
    private String error;

    public boolean isSuccessful() {
        return successful;
    }

    public List<TestResultsLine> getResult() {
        return result;
    }

    public void setResult(List<TestResultsLine> result) {
        this.result = result;
    }

    private List<TestResultsLine> result;

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {

        this.error = error;
    }
}
