package org.kendar.replayer.apis.models;

public class SingleScriptLine {
    private Long id;
    private boolean stimulatorTest;
    private String queryCalc;
    private boolean script;
    private boolean preScript;
    private boolean responseHashCalc;
    private boolean requestHashCalc;
    private String requestMethod;
    private String requestHost;
    private String requestPath;
    private int responseStatusCode;
    private long reference;
    private String type;
    private long calls;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public boolean isStimulatorTest() {
        return stimulatorTest;
    }

    public void setStimulatorTest(boolean stimulatorTest) {
        this.stimulatorTest = stimulatorTest;
    }

    public String getQueryCalc() {
        return queryCalc;
    }

    public void setQueryCalc(String queryCalc) {
        this.queryCalc = queryCalc;
    }

    public boolean isScript() {
        return script;
    }

    public void setScript(boolean script) {
        this.script = script;
    }

    public boolean isPreScript() {
        return preScript;
    }

    public void setPreScript(boolean preScript) {
        this.preScript = preScript;
    }

    public boolean isResponseHashCalc() {
        return responseHashCalc;
    }

    public void setResponseHashCalc(boolean responseHashCalc) {
        this.responseHashCalc = responseHashCalc;
    }

    public boolean isRequestHashCalc() {
        return requestHashCalc;
    }

    public void setRequestHashCalc(boolean requestHashCalc) {
        this.requestHashCalc = requestHashCalc;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestHost() {
        return requestHost;
    }

    public void setRequestHost(String requestHost) {
        this.requestHost = requestHost;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public void setReference(long reference) {
        this.reference = reference;
    }

    public long getReference() {
        return reference;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setCalls(long calls) {
        this.calls = calls;
    }

    public long getCalls() {
        return calls;
    }
}
