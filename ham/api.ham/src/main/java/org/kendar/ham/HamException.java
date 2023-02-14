package org.kendar.ham;

public class HamException extends Exception {
    public HamException(String message) {
        super(message);
    }

    public HamException(Exception e) {
        super(e);
    }
    public HamException(String message,Exception e) {
        super(message,e);
    }
}
