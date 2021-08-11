package org.kendar.servers.logger;

public enum LogPartFlag {
    DEFAULT("DEFAULT"),
    CURL("CURL"),
    STATIC("STATIC"),
    BODY("BODY"),
    DYNAMIC("DYNAMIC"),
    APPS("APPS"),
    HOOKS("HOOKS"),
    HEADERS("HEADERS"),
    ALL("ALL")
    ;

    private final String text;

    /**
     * @param text
     */
    LogPartFlag(final String text) {
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
