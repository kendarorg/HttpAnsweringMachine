package org.kendar.replayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.SerializableResponse;
import org.kendar.utils.LoggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ReplayerStatus {

    private ReplayerDataset dataset;
    private ReplayerState state = ReplayerState.NONE;
    @Value("${replayer.data:replayerdata}")
    private String replayerData;
    private LoggerBuilder loggerBuilder;

    public ReplayerStatus(LoggerBuilder loggerBuilder){

        this.loggerBuilder = loggerBuilder;
    }

    public void startRecording( String id,String description) throws IOException {
        var rootPath = buildPath();
        if(!Files.isDirectory(rootPath)){
            Files.createDirectory(rootPath);
        }
        if(state!=ReplayerState.NONE)return;
        state = ReplayerState.RECORDING;
        dataset = new ReplayerDataset(id,rootPath.toString(),description,loggerBuilder);
    }

    private static final String MAIN_FILE ="runall.json";
    private ObjectMapper mapper = new ObjectMapper();


    public void addRequest(Request req, Response res) throws Exception {
        if(state!=ReplayerState.RECORDING) return;
        dataset.add(req,res);
    }


    private Path buildPath(){
        try {


            var fp = new URI(replayerData);

            if(!fp.isAbsolute()){
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();

                return Path.of(s+ File.separator+ replayerData);
            }else {
                return Path.of(replayerData);
            }
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public boolean replay(Request req, Response res) {
        if(state!=ReplayerState.REPLAYING)return false;
        SerializableResponse response = dataset.findResponse(req);
        if(response!=null){
            Response.fromSerializable(res,response);
            return true;
        }
        return false;
    }

    public ReplayerState getStatus() {
        if(state==null)return ReplayerState.NONE;
        return state;
    }

    public void restartRecording() throws IOException {
        if(state!=ReplayerState.PAUSED_RECORDING)return;
        state = ReplayerState.RECORDING;
    }

    public void pauseRecording() {
        if(state!=ReplayerState.RECORDING)return;
        state = ReplayerState.PAUSED_RECORDING;
    }

    public void stopAndSave() throws IOException {

        if(state!=ReplayerState.PAUSED_RECORDING && state!=ReplayerState.RECORDING)return;
        state = ReplayerState.NONE;
        dataset.save();
        dataset = null;

    }

    public void startReplaying(String id) throws IOException {
        var rootPath = buildPath();
        if(!Files.isDirectory(rootPath)){
            Files.createDirectory(rootPath);
        }
        if(state!=ReplayerState.NONE)return;
        state = ReplayerState.REPLAYING;
        dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder);
        dataset.load();
    }

    public void restartReplaying() {
        if(state!=ReplayerState.PAUSED_REPLAYING)return;
        state = ReplayerState.REPLAYING;

    }

    public void pauseReplaying() {
        if(state!=ReplayerState.REPLAYING)return;
        state = ReplayerState.PAUSED_REPLAYING;
    }

    public void stopReplaying() {
        state = ReplayerState.NONE;
        dataset = null;
    }
}
