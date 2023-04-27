package org.kendar.ham;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ExtraParam {
    DB_RECORD_VOID_CALLS("recordVoidDbCalls"),
    DB_RECORD_CALLS("recordDbCalls"),
    DB_USE_SIMULATED_ENGINE("useSimEngine"),
    DB_NAMES("dbNames"),
    HTTP_HOSTS("hosts");

    private static final Map<String, ExtraParam> typesMap = new HashMap<>();

    static {
        for (ExtraParam type : ExtraParam.values()) {
            typesMap.put(type.text.toLowerCase(Locale.ROOT), type);
        }
    }

    private final String text;

    ExtraParam(final String text) {
        this.text = text;
    }

    public static ExtraParam fromString(String value) {
        var type = typesMap.get(value);
        if (type == null) {
            var founded = Arrays.stream(ExtraParam.values()).filter(a -> a.name().equalsIgnoreCase(value))
                    .findFirst();
            return founded.isEmpty() ? null : founded.get();
        }
        return type;
    }

    public boolean is(String text) {
        return text.equalsIgnoreCase(this.text);
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
