package org.kendar.servers.utils.models;

import java.util.List;

public class RegexpResult {
    private boolean matchFound;
    private String error;
    private boolean failed;
    private List<String> matches;

    public void setMatchFound(boolean matchFound) {
        this.matchFound = matchFound;
    }

    public boolean getMatchFound() {
        return matchFound;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean getFailed() {
        return failed;
    }

    public void setMatches(List<String> matches) {
        this.matches = matches;
    }

    public List<String> getMatches() {
        return matches;
    }
}
