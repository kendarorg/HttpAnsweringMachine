package org.kendar.xml.model;

import java.util.ArrayList;
import java.util.List;

public class DiffPath {
    private final List<String> position = new ArrayList<>();

    public void push(String s) {
        position.add(s);
    }

    public void pop() {
        position.remove(position.size() - 1);
    }


    public String getPath() {
        return String.join(".", position);
    }
}
