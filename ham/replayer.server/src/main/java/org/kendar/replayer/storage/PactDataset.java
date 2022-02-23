package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class PactDataset implements BaseDataset{
    private final Logger logger;
    private String name;
    private String replayerDataDir;
    private Thread thread;
    private String id;
    private ObjectMapper mapper = new ObjectMapper();

    public PactDataset(String name, String replayerDataDir, LoggerBuilder loggerBuilder){
        this.name = name;
        this.replayerDataDir = replayerDataDir;
        this.logger = loggerBuilder.build(PactDataset.class);
    }
    @Override
    public String getName() {
        return name;
    }

    public String start() {
        id = UUID.randomUUID().toString();
        thread = new Thread(()-> {
            try {
                runPactDataset(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return id;
    }

    private void runPactDataset(String id) throws IOException {
        var rootPath = Path.of(replayerDataDir);
        if (!Files.isDirectory(rootPath)) {
            Files.createDirectory(rootPath);
        }
        var stringPath = Path.of(rootPath + File.separator + name + ".json");
        var pactsDir = Path.of(rootPath + File.separator + "pacts" + File.separator);
        var resultsFile =  Path.of(rootPath + File.separator + "pacts" + File.separator+ id+".json");
        if(!Files.exists(pactsDir)){
            Files.createDirectory(pactsDir);
        }
        var maps = new HashMap<Integer,ReplayerRow>();
        var jss = new HashMap<Integer,String>();
        var replayerResult = mapper.readValue(stringPath.toFile(), ReplayerResult.class);
        for (var call : replayerResult.getStaticRequests()) {
            maps.put(call.getId(),call);
        }
        for (var call : replayerResult.getDynamicRequests()) {
            maps.put(call.getId(),call);
        }
        var indexes = replayerResult.getIndexes().stream()
                .filter(a->a.isPactTest())
                .sorted(Comparator.comparingInt(CallIndex::getId))
                .collect(Collectors.toList());
        for (var toCall : indexes) {
            var reqResp = maps.get(toCall.getReference());
            var jsToRun = jss.get(toCall.getReference());
            //Call request
            //Retrieve response
            //Run the js
            //Write to resultsFile
        }

    }
}
