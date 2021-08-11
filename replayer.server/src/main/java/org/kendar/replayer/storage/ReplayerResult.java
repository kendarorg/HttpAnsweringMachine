package org.kendar.replayer.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ReplayerResult {
    private List<ReplayerRow> rows = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    public void add(ReplayerRow row) {
        getRows().add(row);
    }

    public void addError(String error) {
        getErrors().add(error);
    }

    public List<ReplayerRow> getRows() {
        return rows;
    }

    public void setRows(List<ReplayerRow> rows) {
        this.rows = rows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
