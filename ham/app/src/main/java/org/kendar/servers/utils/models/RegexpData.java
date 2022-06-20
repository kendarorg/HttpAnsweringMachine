package org.kendar.servers.utils.models;

public class RegexpData {
    private String regexp;
    private String matcherString;
    private boolean caseInsensitive;
    private boolean literal;
    private boolean unicodeCase;
    private boolean multiline;

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public String getMatcherString() {
        return matcherString;
    }

    public void setMatcherString(String matcherString) {
        this.matcherString = matcherString;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public boolean isLiteral() {
        return literal;
    }

    public void setLiteral(boolean literal) {
        this.literal = literal;
    }

    public boolean isUnicodeCase() {
        return unicodeCase;
    }

    public void setUnicodeCase(boolean unicodeCase) {
        this.unicodeCase = unicodeCase;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }
}
