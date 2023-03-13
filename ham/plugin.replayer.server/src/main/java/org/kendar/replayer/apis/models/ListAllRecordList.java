package org.kendar.replayer.apis.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.db.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListAllRecordList {
    private List<CallIndex> callIndex;
    private List<ReplayerRow> replayerRows;
    private Long id;
    private String description;
    private List<ReplayerRow> lines = new ArrayList<>();
    private List<CallIndex> indexes = new ArrayList<>();
    private HashMap<String, String> filters;

    private DbRecording recording;

    private static TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<>() {
    };

    private static ObjectMapper mapper = new ObjectMapper();

    public ListAllRecordList(HibernateSessionFactory sessionFactory, Long id, boolean cleanJs) throws Exception {

        sessionFactory.query(em -> {
            recording = (DbRecording) em.createQuery("SELECT e FROM DbRecording e").getResultList().get(0);
            replayerRows = (List<ReplayerRow>) em.createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId=" + id).getResultList();
            callIndex = (List<CallIndex>) em.createQuery("SELECT e FROM CallIndex e WHERE e.recordingId=" + id).getResultList();
        });

        for (var row : replayerRows) {
            var req = row.getRequest();
            req.setRequestText(null);
            req.setRequestBytes(null);
            row.setRequest(req);

            var res = row.getResponse();
            res.setResponseText(null);
            res.setResponseBytes(null);
            row.setResponse(res);
        }
        if (recording.getFilter() != null && !recording.getFilter().isEmpty()) {
            filters = mapper.readValue(recording.getFilter(), typeRef);
        } else {
            filters = new HashMap<>();
        }

        for (var index : callIndex) {
            getIndexes().add(index);
        }
        this.setId(id);
        this.setDescription(recording.getDescription());
    }

    public List<ReplayerRow> getLines() {
        return lines;
    }

    public void setLines(List<ReplayerRow> lines) {
        this.lines = lines;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public HashMap<String, String> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, String> filters) {
        this.filters = filters;
    }
}
