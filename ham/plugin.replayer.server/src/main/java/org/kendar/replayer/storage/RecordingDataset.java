package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.MimeChecker;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class RecordingDataset implements BaseDataset{
    private final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    private final AtomicLong counter = new AtomicLong(0L);
    private final Md5Tester md5Tester;
    private final Logger logger;
    private HibernateSessionFactory sessionFactory;
    private Long name;
    private String replayerDataDir;
    private String description;
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean recordDbCalls;
    private boolean recordVoidDbCalls;

    public Long getName(){
        return this.name;
    }

    @Override
    public void load(Long name, String replayerDataDir, String description) {
        this.description = description;
        this.name = name;
        this.replayerDataDir = replayerDataDir;
    }

    @Override
    public ReplayerState getType() {
        return ReplayerState.RECORDING;
    }

    @Override
    public void setRecordDbCalls(boolean recordDbCalls) {
        this.recordDbCalls = recordDbCalls;
    }

    @Override
    public void setRecordVoidDbCalls(boolean recordVoidDbCalls) {
        this.recordVoidDbCalls = recordVoidDbCalls;
    }

    public RecordingDataset(
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester, HibernateSessionFactory sessionFactory) {
        this.md5Tester = md5Tester;
        this.logger = loggerBuilder.build(RecordingDataset.class);
        this.sessionFactory = sessionFactory;
    }

    public void save() throws IOException {

        staticRequests.clear();
        recording =  null;
    }

    private static Map<String,Long> staticRequests = new HashMap<>();
    private static DbRecording recording;

    public void add(Request req, Response res) throws Exception {
        if(name==null){
            sessionFactory.transactional((em)->{
                recording = new DbRecording();
                recording.setDescription(description);
                em.persist(recording);
                name = recording.getId();
            });
        }
        if(recording == null){
            recording = sessionFactory.queryResult((em)->{
                return em.createQuery("SELECT e FROM DbRecording e WHERE e.id="+name).getResultList().get(0);
            });
        }
        var path = req.getHost() + req.getPath();
        try {

            String responseHash;
            var newId = req.getId();
            var replayerRow = new ReplayerRow();
            replayerRow.setType("http");
            if(req.getPath().startsWith("/api/db/")){
                replayerRow.setType("db");
                if(!recordDbCalls)return;
                if(!recordVoidDbCalls){
                    if(res.getResponseText()==null)return;
                    if(res.getResponseText().contains("VoidResult"))return;
                }
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
            callIndex.setRecordingId(recording.getId());

            sessionFactory.transactional(em-> {
                var isRowStatic = MimeChecker.isStatic(res.getHeader(ConstantsHeader.CONTENT_TYPE), req.getPath());
                if(replayerRow.getType().equalsIgnoreCase("db")){
                    isRowStatic=false;
                }
                var saveRow = true;
                if (isRowStatic && req.getMethod().equalsIgnoreCase("GET")) {
                    replayerRow.setStaticRequest(true);
                    if (staticRequests.containsKey(replayerRow.getResponseHash())) {
                        saveRow = false;
                    }
                } else {
                    replayerRow.setStaticRequest(false);
                }
                if (!saveRow) {
                    //Overwrite when duplicate
                    //No save row only callIndex
                    callIndex.setReference(staticRequests.get(replayerRow.getResponseHash()));
                } else {
                    if (isRowStatic && req.getMethod().equalsIgnoreCase("GET")) {
                        staticRequests.put(replayerRow.getResponseHash(), replayerRow.getId());
                    }
                    em.persist(replayerRow);
                }
                em.persist(callIndex);
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error recording request " + path, e);
        }
    }
}
