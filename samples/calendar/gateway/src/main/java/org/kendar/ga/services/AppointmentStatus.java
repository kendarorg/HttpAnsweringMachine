package org.kendar.ga.services;

public enum AppointmentStatus {
    CREATED("CREATED"),
    DRAFT("DRAFT"),
    CONFIRMED("CONFIRMED")
    ;

    private final String text;

    /**
     * @param text
     */
    AppointmentStatus(final String text) {
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
