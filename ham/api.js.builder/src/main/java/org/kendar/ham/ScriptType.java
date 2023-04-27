package org.kendar.ham;

public enum ScriptType {
    SCRIPT("script"),
    BODY("body");

    private final String text;

    ScriptType(String text) {

        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
