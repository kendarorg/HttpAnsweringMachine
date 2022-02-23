package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.PactCompleted;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class PactDataset implements BaseDataset{
    private final Logger logger;
    private EventQueue eventQueue;
    private String name;
    private String replayerDataDir;
    private Thread thread;
    private String id;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean running = new AtomicBoolean(false);

    public PactDataset(LoggerBuilder loggerBuilder, EventQueue eventQueue){

        this.logger = loggerBuilder.build(PactDataset.class);
        this.eventQueue = eventQueue;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void load(String name, String replayerDataDir, String description) {
        this.name = name;
        this.replayerDataDir = replayerDataDir;
    }


    @Override
    public ReplayerState getType() {
        return ReplayerState.PLAYING_PACT;
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
        running.set(true);
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
            if(!running.get())break;
            var reqResp = maps.get(toCall.getReference());
            var jsToRun = jss.get(toCall.getReference());
            //Call request
            //Retrieve response
            //Run the js
            //Write to resultsFile
        }
        this.eventQueue.handle(new PactCompleted());
    }

    public void stop() {
        running.set(false);
    }
}
