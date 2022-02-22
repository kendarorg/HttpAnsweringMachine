package org.kendar.replayer.storage;

public class CallIndex {
    private int id;
    private int reference;
    private String description;
    private String preRequestJs;
    private String middleJs;
    private String postResponseJs;
    private String testType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreRequestJs() {
        return preRequestJs;
    }

    public void setPreRequestJs(String preRequestJs) {
        this.preRequestJs = preRequestJs;
    }

    public String getMiddleJs() {
        return middleJs;
    }

    public void setMiddleJs(String middleJs) {
        this.middleJs = middleJs;
    }

    public String getPostResponseJs() {
        return postResponseJs;
    }

    public void setPostResponseJs(String postResponseJs) {
        this.postResponseJs = postResponseJs;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }
}
