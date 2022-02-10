package org.kendar.http.annotations;

public enum MatcherType {
    ADDRESS("ADDRESS"),
    PATH("PATH"),
    QUERY_STRING("PARAM"),
    BODY("BODY"),
    HEADER("HEADER"),
    JSONPATH("JSONPATH"),
    XPATH("XPATH");

    private final String text;

    MatcherType(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
