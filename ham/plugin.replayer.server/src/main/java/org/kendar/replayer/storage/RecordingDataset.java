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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class RecordingDataset implements BaseDataset{
    private final DataReorganizer dataReorganizer;
    private final ConcurrentLinkedQueue<ReplayerRow> dynamicData = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, List<ReplayerRow>> staticData = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<CallIndex> indexes = new ConcurrentLinkedQueue<>();
    private final Md5Tester md5Tester;
    private final Logger logger;
    private HibernateSessionFactory sessionFactory;
    private Long name;
    private String replayerDataDir;
    private String description;
    private final ObjectMapper mapper = new ObjectMapper();

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

    public RecordingDataset(
            LoggerBuilder loggerBuilder, DataReorganizer dataReorganizer,
            Md5Tester md5Tester, HibernateSessionFactory sessionFactory) {
        this.dataReorganizer = dataReorganizer;
        this.md5Tester = md5Tester;
        this.logger = loggerBuilder.build(RecordingDataset.class);
        this.sessionFactory = sessionFactory;
    }

    public void save() throws IOException {
        //synchronized (this) {
            /*var result = new ReplayerResult();
            var partialResult = new ArrayList<ReplayerRow>();
            var rootPath = Path.of(replayerDataDir);
            if (!Files.isDirectory(rootPath)) {
                Files.createDirectory(rootPath);
            }
            for (var staticRow : this.staticData.entrySet()) {
                var rowValue = staticRow.getValue();
                if(rowValue.size()==1) {
                    partialResult.add(rowValue.get(0));
                }else{
                    rowValue.forEach(a->a.getRequest().setStaticRequest(false));
                    partialResult.addAll(rowValue);
                }
            }

            while (!dynamicData.isEmpty()) {
                // consume element
                var rowValue = dynamicData.poll();
                partialResult.add(rowValue);
            }

            while (!errors.isEmpty()) {
                // consume element
                result.addError(errors.poll());
            }

            result.setDescription(description);
            result.setIndexes(new ArrayList<>(indexes));
            dataReorganizer.reorganizeData(result, partialResult);

            var allDataString = mapper.writeValueAsString(result);
            var stringPath = rootPath + File.separator + name + ".json";
            FileWriter myWriter = new FileWriter(stringPath);
            myWriter.write(allDataString);
            myWriter.close();*/
            staticRequests.clear();
            recording =  null;
        //}
    }

    private static Map<String,Long> staticRequests = new HashMap<>();
    private static DbRecording recording;

    public void add(Request req, Response res) throws Exception {
        if(recording==null){
            sessionFactory.transactional((em)->{
                recording = new DbRecording();
                recording.setDescripton(description);
                em.persist(recording);
            });
        }
        var path = req.getHost() + req.getPath();
        try {

            String responseHash;

            if (req.isStaticRequest() && staticData.containsKey(path)) {
                var alreadyPresent = staticData.get(path);
                if (res.isBinaryResponse()) {
                    responseHash = md5Tester.calculateMd5(res.getResponseBytes());
                } else {
                    responseHash = md5Tester.calculateMd5(res.getResponseText());
                }

                final var lambdaHash = responseHash;
                var isAlreadyPresent = alreadyPresent.stream().filter(present->
                        lambdaHash.equalsIgnoreCase(present.getResponseHash())).collect(Collectors.toList());
                if (isAlreadyPresent.size()>0) {
                    var newId = counter.getAndIncrement();
                    var callIndex = new CallIndex();
                    callIndex.setId(newId);
                    callIndex.setReference(isAlreadyPresent.get(0).getId());
                    this.indexes.add(callIndex);
                    return;
                }
            }
            var replayerRow = new ReplayerRow();
            if (res.isBinaryResponse()) {
                responseHash = md5Tester.calculateMd5(res.getResponseBytes());
            } else {
                responseHash = md5Tester.calculateMd5(res.getResponseText());
            }
            replayerRow.setId(counter.getAndIncrement());
            replayerRow.setRequest(req);
            replayerRow.setResponse(res);
            if (req.isBinaryRequest()) {
                replayerRow.setRequestHash(md5Tester.calculateMd5(req.getRequestBytes()));
            } else {
                replayerRow.setRequestHash(md5Tester.calculateMd5(req.getRequestText()));
            }
            replayerRow.setResponseHash(responseHash);

            var callIndex = new CallIndex();
            callIndex.setRecordingId(recording.getId());

            sessionFactory.transactional(em-> {
                var isRowStatic = MimeChecker.isStatic(res.getHeader(ConstantsHeader.CONTENT_TYPE), req.getPath());

                replayerRow.setRecordingId(recording.getId());
                var saveRow = true;
                if (isRowStatic && req.getMethod().equalsIgnoreCase("GET")) {
                    replayerRow.setStaticRequest(true);
                    if (staticRequests.containsKey(replayerRow.getResponseHash())) {
                        saveRow = false;
                    }
                } else {
                    replayerRow.setStaticRequest(false);
                }
                /*if (req.isStaticRequest()) {

                    if(!staticData.containsKey(path)){
                        staticData.put(path, new ArrayList<>());
                    }
                    staticData.get(path).add(replayerRow);
                    callIndex.setReference(replayerRow.getId());
                } else {
                    dynamicData.add(replayerRow);
                }*/
                em.persist(replayerRow);

                callIndex.setId(replayerRow.getId());
                if (!saveRow) {
                    callIndex.setReference(replayerRow.getId());
                } else {
                    staticRequests.put(replayerRow.getResponseHash(),replayerRow.getId());
                    callIndex.setReference(staticRequests.get(replayerRow.getResponseHash()));
                }
                em.persist(callIndex);
                if (!saveRow) {
                    em.remove(replayerRow);
                }
            });

            /*this.indexes.add(callIndex);
            if (req.isStaticRequest()) {
                if(!staticData.containsKey(path)){
                    staticData.put(path, new ArrayList<>());
                }
                staticData.get(path).add(replayerRow);
                callIndex.setReference(replayerRow.getId());
            } else {
                dynamicData.add(replayerRow);
            }*/
            // ADD the crap
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error recording request " + path, e);
        }
    }
}
