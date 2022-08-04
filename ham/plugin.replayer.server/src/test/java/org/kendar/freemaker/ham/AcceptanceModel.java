package org.kendar.freemaker.ham;

import java.util.ArrayList;
import java.util.List;

public class AcceptanceModel {
    private String packageName;
    private String profile;
    private String name;
    private List<ReqResponse> responses = new ArrayList<>();
    private List<ReqResponse> requests = new ArrayList<>();

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReqResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<ReqResponse> responses) {
        this.responses = responses;
    }

    public List<ReqResponse> getRequests() {
        return requests;
    }

    public void setRequests(List<ReqResponse> requests) {
        this.requests = requests;
    }
}
