package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;

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
