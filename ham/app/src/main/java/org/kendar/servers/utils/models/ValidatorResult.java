package org.kendar.servers.utils.models;

import java.util.ArrayList;
import java.util.List;

public class ValidatorResult {
    private boolean error;
    private List<String> errors = new ArrayList<>();

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
