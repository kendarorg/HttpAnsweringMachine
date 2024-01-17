package org.kendar.replayer.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.replayer.apis.models.LocalRecording;
import org.kendar.replayer.engine.ReplayerResult;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.utils.FullDownloadUpload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RecorderDownloadUpload implements FullDownloadUpload {
    final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final HibernateSessionFactory sessionFactory;
    TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<>() {
    };

    public RecorderDownloadUpload(JsonConfiguration configuration,
                                  EventQueue eventQueue,
                                  HibernateSessionFactory sessionFactory) {

        this.configuration = configuration;
        this.eventQueue = eventQueue;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Map<String, byte[]> retrieveItems() throws Exception {

        Map<String, byte[]> resultData = new HashMap<>();


        var listOfItems = new ArrayList<LocalRecording>();
        sessionFactory.query((em -> {
            List<DbRecording> allRecs = em.createQuery("SELECT e FROM DbRecording e").getResultList();
            for (var rs : allRecs) {
                var lr = new LocalRecording();
                lr.setId(rs.getId());
                lr.setName(rs.getName());
                listOfItems.add(lr);
            }
        }));

        for (var singleIt : listOfItems) {
            var result = new ReplayerResult();


            sessionFactory.query(em -> {
                DbRecording recording = (DbRecording) em.createQuery("SELECT e FROM DbRecording e WHERE e.id=" + singleIt.getId()).getResultList().get(0);
                List<CallIndex> indexLines = em.createQuery("SELECT e FROM CallIndex e WHERE e.recordingId=" + singleIt.getId()).getResultList();
                List<ReplayerRow> rows = em.createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId=" + singleIt.getId()).getResultList();

                result.setName(recording.getName());
                result.setDescription(recording.getDescription());
                for (var row : rows) {
                    if (row.isStaticRequest()) {
                        result.getStaticRequests().add(row);
                    } else {
                        result.getDynamicRequests().add(row);
                    }
                }
                for (var indexLine : indexLines) {
                    result.getIndexes().add(indexLine);
                }
            });
            resultData.put("recording." + singleIt.getId() + ".json", mapper.writeValueAsBytes(result));
        }

        return resultData;
    }

    @Override
    public String getId() {
        return "recorder";
    }

    @Override
    public void uploadItems(HashMap<String, byte[]> data) throws Exception {
        sessionFactory.transactional(em -> {
            em.createQuery("DELETE FROM TestResultsLine").executeUpdate();
            em.createQuery("DELETE FROM TestResults").executeUpdate();
            em.createQuery("DELETE FROM ReplayerRow").executeUpdate();
            em.createQuery("DELETE FROM CallIndex").executeUpdate();
            em.createQuery("DELETE FROM DbRecording").executeUpdate();
        });
        for (var filter : data.entrySet()) {
            var json = new String(filter.getValue());
            var replayerResult = mapper.readValue(json, ReplayerResult.class);
            var recording = new DbRecording();
            recording.setDescription(replayerResult.getDescription());
            recording.setName(replayerResult.getName());
            recording.setFilter(mapper.writeValueAsString(replayerResult.getFilter()));

            sessionFactory.transactional(em -> em.persist(recording));
            for (var row : replayerResult.getDynamicRequests()) {
                row.setIndex(null);
                row.setRecordingId(recording.getId());
                sessionFactory.transactional(em -> em.persist(row));
            }
            for (var row : replayerResult.getStaticRequests()) {
                row.setIndex(null);
                row.setRecordingId(recording.getId());
                sessionFactory.transactional(em -> em.persist(row));
            }
            for (var row : replayerResult.getIndexes()) {
                row.setIndex(null);
                row.setRecordingId(recording.getId());
                sessionFactory.transactional(em -> em.persist(row));
            }
        }


    }
}
