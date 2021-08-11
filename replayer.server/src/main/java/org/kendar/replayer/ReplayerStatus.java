package org.kendar.replayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ReplayerStatus {

    private ReplayerDataset dataset;
    private ReplayerState state;

    public void startRecording(String id) throws IOException {
        var rootPath = buildPath(id);
        if(!Files.isDirectory(rootPath)){
            Files.createDirectory(rootPath);
        }
        state = ReplayerState.RECORDING;
        dataset = new ReplayerDataset(id,rootPath.toString());
    }

    private static final String MAIN_FILE ="runall.json";
    private ObjectMapper mapper = new ObjectMapper();
    @Value("${replayer.data:replayerdata}")
    private String replayerData;


    public void addRequest(Request req, Response res) throws Exception {
        if(state!=ReplayerState.RECORDING) return;
        dataset.add(req,res);
    }


    private Path buildPath(String requestPart){
        try {
            if(!requestPart.startsWith("/")){
                requestPart = "/"+requestPart;
            }
            var fp = new URI(requestPart);

            if(!fp.isAbsolute()){
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();

                return Path.of(s+ replayerData+requestPart);
            }else {
                return Path.of(replayerData+requestPart);
            }
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public boolean replay(Request req, Response res) {
        return false;
    }

    public ReplayerState getStatus() {
        return state;
    }

    public void restartRecording() throws IOException {
        state = ReplayerState.RECORDING;
        dataset.save();
    }

    public void pauseRecording() {
        state = ReplayerState.PAUSED_RECORDING;
    }

    public void stopAndSave() {
        state = ReplayerState.NONE;

    }
}
