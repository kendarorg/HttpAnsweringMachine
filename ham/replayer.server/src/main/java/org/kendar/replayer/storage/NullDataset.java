package org.kendar.replayer.storage;

import org.kendar.events.EventQueue;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.events.NullCompleted;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.utils.LoggerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class NullDataset extends ReplayerDataset{
    private EventQueue eventQueue;
    private Thread thread;
    private String id;
    private AtomicBoolean running = new AtomicBoolean(false);

    public NullDataset(
            LoggerBuilder loggerBuilder,
            DataReorganizer dataReorganizer,
            Md5Tester md5Tester, EventQueue eventQueue) {
        super(loggerBuilder,dataReorganizer,md5Tester);
        this.eventQueue = eventQueue;
    }


    @Override
    public ReplayerState getType() {
        return ReplayerState.PLAYING_NULL_INFRASTRUCTURE;
    }

    @Override
    public String getName() {
        return name;
    }

    public String start() {
        id = UUID.randomUUID().toString();
        thread = new Thread(()-> {
            try {
                runNullDataset(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return id;
    }

    @Override
    protected boolean superMatch(ReplayerRow row) {
        return row.isStimulatedTest();
    }

    private void runNullDataset(String id) throws IOException {
        running.set(true);
        var rootPath = Path.of(replayerDataDir);
        if (!Files.isDirectory(rootPath)) {
            Files.createDirectory(rootPath);
        }
        var stringPath = Path.of(rootPath + File.separator + name + ".json");
        var nullDir = Path.of(rootPath + File.separator + "null" + File.separator);
        var resultsFile =  Path.of(rootPath + File.separator + "null" + File.separator+ id+".json");
        if(!Files.exists(nullDir)){
            Files.createDirectory(nullDir);
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
                .filter(a->a.isStimulatorTest())
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
        this.eventQueue.handle(new NullCompleted());
    }

    public void stop() {
        running.set(false);
    }
}
