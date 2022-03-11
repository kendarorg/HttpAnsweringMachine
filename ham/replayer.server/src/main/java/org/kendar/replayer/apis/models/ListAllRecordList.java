package org.kendar.replayer.apis.models;

import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;

import java.util.ArrayList;
import java.util.List;

public class ListAllRecordList {
    private String id;
    private String description;
    private List<ReplayerRow> lines = new ArrayList<>();
    private List<CallIndex> indexes = new ArrayList<>();

    public ListAllRecordList(ReplayerResult datasetContent,String id,boolean cleanJs) {
        for (var staticLine :
                datasetContent.getStaticRequests()) {
            staticLine.getRequest().setRequestText(null);
            staticLine.getRequest().setRequestBytes(null);
            staticLine.getResponse().setResponseBytes(null);
            staticLine.getResponse().setResponseText(null);
            getLines().add((staticLine));
        }
        for (var dynamicLine :
                datasetContent.getDynamicRequests()) {
            dynamicLine.getRequest().setRequestText(null);
            dynamicLine.getRequest().setRequestBytes(null);
            dynamicLine.getResponse().setResponseBytes(null);
            dynamicLine.getResponse().setResponseText(null);
            getLines().add((dynamicLine));
        }
        for(var index: datasetContent.getIndexes()){
            getIndexes().add(index);
        }
        this.setId(id);
        this.setDescription(datasetContent.getDescription());
    }

    public List<ReplayerRow> getLines() {
        return lines;
    }

    public void setLines(List<ReplayerRow> lines) {
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

    public List<CallIndex> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<CallIndex> indexes) {
        this.indexes = indexes;
    }
}
