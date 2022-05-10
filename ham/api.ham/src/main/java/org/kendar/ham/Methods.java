package org.kendar.ham;

public enum Methods {
    GET("GET"),
    POST("POST"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    PUT("PUT");
    private final String text;

    /**
     * Filter phase
     *
     * @param text for phase
     */
    Methods(final String text) {
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
