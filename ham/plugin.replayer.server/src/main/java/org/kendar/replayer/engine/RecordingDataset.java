package org.kendar.replayer.engine;

import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RecordingDataset implements BaseDataset {
    private final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    private final AtomicLong counter = new AtomicLong(0L);
    private final Md5Tester md5Tester;
    private final Logger logger;
    private HibernateSessionFactory sessionFactory;
    private List<ReplayerEngine> replayerEngines;
    private Long name;
    private String description;
    private Map<String, String> specialParams;

    public Long getName() {
        return this.name;
    }

    @Override
    public void load(Long name, String description) {
        this.description = description;
        this.name = name;
    }

    @Override
    public ReplayerState getType() {
        return ReplayerState.RECORDING;
    }

    @Override
    public void setSpecialParams(Map<String, String> query) {
        this.specialParams = query;
    }

    @Override
    public void setParams(Map<String, String> query) {

    }

    public RecordingDataset(
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester, HibernateSessionFactory sessionFactory,
            List<ReplayerEngine> replayerEngines) {
        this.md5Tester = md5Tester;
        this.logger = loggerBuilder.build(RecordingDataset.class);
        this.sessionFactory = sessionFactory;
        this.replayerEngines = replayerEngines;
    }

    public void save() throws Exception {
//        for (int i = 0; i < replayerEngines.size(); i++) {
//            var engine = replayerEngines.get(i);
//            engine.setupStaticCalls(recording);
//        }
        staticRequests.clear();
        recording = null;
    }

    private static Map<String, Long> staticRequests = new HashMap<>();
    private static DbRecording recording;

    public boolean add(Request req, Response res) throws Exception {
        if (name == null) {
            sessionFactory.transactional((em) -> {
                recording = new DbRecording();
                recording.setDescription(description);
                em.persist(recording);
                name = recording.getId();
            });
        }
        if (recording == null) {
            recording = sessionFactory.queryResult((em) -> {
                return em.createQuery("SELECT e FROM DbRecording e WHERE e.id=" + name).getResultList().get(0);
            });
        }
        var path = req.getHost() + req.getPath();
        try {

            String responseHash;
            var newId = req.getId();
            var replayerRow = new ReplayerRow();

            ReplayerEngine engine = null;
            for (int i = 0; i < replayerEngines.size(); i++) {
                engine = replayerEngines.get(i);
                if (engine.isValidPath(req)) {
                    replayerRow.setType(engine.getId());
                    if (!engine.isValidRoundTrip(req, res, specialParams)) {
                        engine = null;
                        continue;
                    }
                    engine.updateReqRes(req, res, specialParams);
                    break;
                } else {
                    engine = null;
                }
            }

            if (engine == null) {
                return false;
            }

            if (res.isBinaryResponse()) {
                responseHash = md5Tester.calculateMd5(res.getResponseBytes());
            } else {
                responseHash = md5Tester.calculateMd5(res.getResponseText());
            }
            replayerRow.setTimestamp(req.getMs());
            replayerRow.setId(newId);
            replayerRow.setRequest(req);
            replayerRow.setResponse(res);
            replayerRow.setPath(req.getPath());
            replayerRow.setHost(req.getHost());
            if (req.isBinaryRequest()) {
                replayerRow.setRequestHash(md5Tester.calculateMd5(req.getRequestBytes()));
            } else {
                replayerRow.setRequestHash(md5Tester.calculateMd5(req.getRequestText()));
            }
            replayerRow.setResponseHash(responseHash);
            replayerRow.setRecordingId(recording.getId());

            var callIndex = new CallIndex();
            callIndex.setTimestamp(req.getMs());
            callIndex.setId(newId);
            callIndex.setReference(newId);
            callIndex.setCalls(1);
            callIndex.setRecordingId(recording.getId());

            final ReplayerEngine constEngine = engine;
            sessionFactory.transactional(em -> {
                replayerRow.setStaticRequest(false);
                em.persist(replayerRow);
                em.persist(callIndex);
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error recording request " + path, e);
            return false;
        }
    }
}
