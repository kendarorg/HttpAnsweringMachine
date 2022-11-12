package org.kendar.replayer.apis.models;

import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.storage.ReplayerResult;
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
    private HashMap<String,String> variables;
    private HashMap<String,String> preScript;
    private HashMap<String,String> postScript;

    private DbRecording recording;

    public ListAllRecordList(HibernateSessionFactory sessionFactory, Long id, boolean cleanJs) throws Exception {

        sessionFactory.query(em->{
            recording = (DbRecording) em.createQuery("SELECT e FROM DbRecording e").getResultList().get(0);
            replayerRows = (List<ReplayerRow>) em.createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId="+id).getResultList();
            callIndex =  (List<CallIndex>) em.createQuery("SELECT e FROM CallIndex e WHERE e.recordingId="+id).getResultList();
        });

        for(var row:replayerRows){
            var req = row.getRequest();
            req.setRequestText(null);
            req.setRequestBytes(null);
            row.setRequest(req);

            var res = row.getResponse();
            res.setResponseText(null);
            res.setResponseBytes(null);
            row.setResponse(res);
        }
  /*
  FIXME
        variables = recording.getVariables();
        preScript = recording.getPreScript();
        postScript= recording.getPostScript();
*/
        for(var index: callIndex){
            getIndexes().add(index);
        }
        this.setId(id);
        this.setDescription(recording.getDescripton());
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

    public HashMap<String, String> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, String> variables) {
        this.variables = variables;
    }

    public HashMap<String, String> getPreScript() {
        return preScript;
    }

    public void setPreScript(HashMap<String, String> preScript) {
        this.preScript = preScript;
    }

    public HashMap<String, String> getPostScript() {
        return postScript;
    }

    public void setPostScript(HashMap<String, String> postScript) {
        this.postScript = postScript;
    }
}
