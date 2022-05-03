package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;

public class HamException extends Throwable {
    public HamException(String message) {
        super(message);
    }

    public HamException(Exception e) {
        super(e);
    }
}
