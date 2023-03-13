package org.kendar.servers.http.types.http;

import org.kendar.servers.http.JsUtils;
import org.mozilla.javascript.Script;

import java.util.ArrayList;
import java.util.List;

public class JsHttpFilterDescriptor {
    private JsHttpAction action;

    private String phase;
    private List<String> requires = new ArrayList<>();
    private int priority;
    private Script compiledScript;
    private boolean blocking;
    private String id;
    private JsUtils jsQueueHandler;


    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires = requires;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }


    public void setScript(Script compiledScript) {
        this.compiledScript = compiledScript;
    }

    public Script getScript() {
        return this.compiledScript;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void initializeQueue(JsUtils jsQueueHandler) {

        this.jsQueueHandler = jsQueueHandler;
    }

    public JsUtils retrieveQueue() {
        return jsQueueHandler;
    }

    public void setAction(JsHttpAction action) {
        this.action = action;
    }

    public JsHttpAction getAction() {
        return action;
    }
}
