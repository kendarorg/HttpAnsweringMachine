package org.kendar.be.data.exceptions;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String id) {
        super("Could not find item " + id);
    }
}
