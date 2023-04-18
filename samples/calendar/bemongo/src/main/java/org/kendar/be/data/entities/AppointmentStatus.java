package org.kendar.be.data.entities;

public enum AppointmentStatus {
    CREATED("CREATED"),
    DRAFT("DRAFT"),
    CONFIRMED("CONFIRMED"),
    NONE("NONE")
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

    public static AppointmentStatus toEnum(String val){
        if(val.equalsIgnoreCase("CREATED"))return  AppointmentStatus.CREATED;
        if(val.equalsIgnoreCase("DRAFT"))return  AppointmentStatus.DRAFT;
        if(val.equalsIgnoreCase("CONFIRMED"))return  AppointmentStatus.CONFIRMED;
        return AppointmentStatus.NONE;
    }
}
