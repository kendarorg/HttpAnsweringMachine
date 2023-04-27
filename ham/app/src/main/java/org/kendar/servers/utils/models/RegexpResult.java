package org.kendar.servers.utils.models;

import java.util.List;

public class RegexpResult {
    private boolean matchFound;
    private String error;
    private boolean failed;
    private List<String> matches;

    public boolean getMatchFound() {
        return matchFound;
    }

    public void setMatchFound(boolean matchFound) {
        this.matchFound = matchFound;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean getFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public List<String> getMatches() {
        return matches;
    }

    public void setMatches(List<String> matches) {
        this.matches = matches;
    }
}
