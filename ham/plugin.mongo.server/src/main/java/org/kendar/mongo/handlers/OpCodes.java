package org.kendar.mongo.handlers;

import java.util.HashMap;
import java.util.Map;

public enum OpCodes {
    OP_NONE(0),
    OP_QUERY(2004),
    OP_COMPRESSED(2012),
    OP_MSG(2013),
    OP_REPLY(1),
    OP_UPDATE(2001),
    OP_INSERT(2002),
    OP_DELETE(2006);
    private final int value;
    private static final Map<Integer, OpCodes> BY_INT = new HashMap<>();
    static {
        for (OpCodes e: values()) {
            BY_INT.put(e.value, e);
        }
    }
    public int getValue() {
        return value;
    }

    OpCodes(int value) {

        this.value = value;
    }

    public static OpCodes of(int value){
        return BY_INT.get(value);
    }
}
