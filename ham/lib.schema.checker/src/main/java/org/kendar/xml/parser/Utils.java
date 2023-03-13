package org.kendar.xml.parser;

public class Utils {
    public static boolean stringIsEmptyOrNull(String value) {
        return value == null || value.isEmpty() || value.trim().isEmpty();
    }

}
