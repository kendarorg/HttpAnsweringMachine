package org.tkendar.ham;

public class HamTestException extends Exception {
    public HamTestException(String message) {
        super(message);
    }

    public HamTestException(Exception e) {
        super(e);
    }
    public HamTestException(String message, Exception e) {
        super(message,e);
    }
}
