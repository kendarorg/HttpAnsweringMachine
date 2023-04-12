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

    private int counter;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
