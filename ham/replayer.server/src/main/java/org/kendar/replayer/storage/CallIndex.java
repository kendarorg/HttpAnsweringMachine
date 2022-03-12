package org.kendar.replayer.storage;

public class CallIndex {
    private int id;
    private int reference;
    private String description;
    private boolean pactTest;
    private boolean stimulatorTest;

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
}
