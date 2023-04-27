package org.kendar.be.data.entities;

import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Counter {
    @Id
    private String id;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    private String table;

    private long counter;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }
}
