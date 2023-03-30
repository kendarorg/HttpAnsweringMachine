package org.kendar.mongo;

import org.bson.*;

public class BsonDocumentHelper {

    public static BsonValue fromString(String value) {
        return new BsonString(value);
    }

    public static BsonValue fromInt32(int value) {
        return new BsonInt32(value);
    }

    public static BsonValue fromBoolean(boolean value) {
        return BsonBoolean.valueOf(value);
    }

    public static BsonValue fromDateTime(long value) {
        return new BsonDateTime(value);
    }
}
