package org.kendar.replayer.apis.models;

import org.kendar.replayer.storage.ReplayerResult;

import java.util.ArrayList;
import java.util.List;

public class ListAllRecordList {
    private String id;
    private String description;
    private List<ListAllRecordLine> lines = new ArrayList<>();

    public ListAllRecordList(ReplayerResult datasetContent,String id) {
        for (var staticLine :
                datasetContent.getStaticRequests()) {
            lines.add(new ListAllRecordLine(staticLine));
        }
        for (var dynamicLine :
                datasetContent.getDynamicRequests()) {
            lines.add(new ListAllRecordLine(dynamicLine));
        }
        this.id = id;
        this.description = datasetContent.getDescription();
    }

    public List<ListAllRecordLine> getLines() {
        return lines;
    }

    public void setLines(List<ListAllRecordLine> lines) {
        this.lines = lines;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
