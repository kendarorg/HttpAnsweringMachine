package org.kendar.ham;

public class ScriptMatcherBuilder {


    private String hostAddress;
    private String script;
    private String pathAddress;

    public ScriptMatcherBuilder withHost(String host) {
        this.hostAddress = host;
        return this;
    }

    public ScriptMatcherBuilder withScript(String jsScript) {
        this.script = jsScript;
        return this;
    }

    public ScriptMatcherBuilder withPath(String host) {
        this.pathAddress = host;
        return this;
    }

    public Matcher build() {
        var matcher = new JsBuilder.ScriptMatcher();
        matcher.setScript(this.script);
        matcher.setHostAddress(this.hostAddress);
        matcher.setPathAddress(this.pathAddress);
        return matcher;
    }
}
