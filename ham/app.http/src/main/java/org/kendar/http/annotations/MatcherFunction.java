package org.kendar.http.annotations;

public enum MatcherFunction {
    EXACT("EXACT"),
    REGEXP("REGEXP"),
    STARTS("STARTS"),
    CONTAINS("CONTAINS"),
    END("END");

    private final String text;

    MatcherFunction(final String text) {
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
