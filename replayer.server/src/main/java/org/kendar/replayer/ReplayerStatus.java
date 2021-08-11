package org.kendar.replayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.storage.ReplayerFileData;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ReplayerStatus {
    private static final String MAIN_FILE ="runall.json";
    private ObjectMapper mapper = new ObjectMapper();
    @Value("${replayer.data:replayerdata}")
    private String replayerData;
    public static final String API = "/api";
    public static final String RECORDINGS = API+"/recording";
    private String operationId;
    private ReplayerState replayerState;
    private ConcurrentLinkedQueue<ReplayerRow> dynamicData;
    private ConcurrentHashMap<String,ReplayerRow> staticData;
    private ConcurrentLinkedQueue<String> errors;
    private AtomicInteger counter = new AtomicInteger(0);
    private ReplayerResult replayerSource;
    private HashSet<String> statesExecuted = new HashSet<>();

    public void setOperation(String operationId, ReplayerState replayerState){
        synchronized (this) {
            if(replayerState==ReplayerState.RECORDING && !operationId.equalsIgnoreCase(operationId)){
                replayerSource = null;
                statesExecuted = new HashSet<>();
                counter.set(0);
                dynamicData = new ConcurrentLinkedQueue<>();
                staticData = new ConcurrentHashMap<>();
                errors = new ConcurrentLinkedQueue<>();
            }else if(replayerState==ReplayerState.REPLAYING && !operationId.equalsIgnoreCase(operationId)){
                try {
                    statesExecuted = new HashSet<>();
                    var rootPath = buildPath(operationId);
                    var mainData  = rootPath+ File.separator+MAIN_FILE;
                    if(!Files.isDirectory(rootPath)){
                        replayerSource = mapper.readValue(new File(mainData),ReplayerResult.class);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.operationId = operationId;
            this.replayerState = replayerState;
        }
    }

    public void setOperation(ReplayerState replayerState) {

        this.replayerState = replayerState;
    }

    public String getOperationId() {
        return operationId;
    }

    public ReplayerState getReplayerState() {
        return replayerState;
    }

    public void addRequest(Request req, Response res) throws Exception {
        try {
            var rootPath = buildPath(operationId);
            if(!Files.isDirectory(rootPath)){
                Files.createDirectory(rootPath);
            }

            MessageDigest md = MessageDigest.getInstance("MD5");
            String responseHash = null;
            var path = req.getHost()+req.getPath();
            if(req.isStaticRequest() && staticData.contains(path)){
                var alreadyPresent = staticData.get(path);
                responseHash=calculateMd5(res.getResponse(),md);
                if(!responseHash.equalsIgnoreCase(alreadyPresent.getResponseFile().getMd5())){
                    errors.add("Static request was dynamic "+path);
                    throw new Exception("Static request was dynamic "+path);
                }
                return;
            }
            var replayerRow = new ReplayerRow();
            if(responseHash==null){
                responseHash = calculateMd5(res.getResponse(),md);
            }
            replayerRow.setId(counter.getAndIncrement());
            replayerRow.setRequest(Request.toSerializable(req));
            replayerRow.setResponse(Response.toSerializable(res));
            replayerRow.setRequestFile(new ReplayerFileData("PATH",calculateMd5(req.getRequest(),md)));
            replayerRow.setResponseFile(new ReplayerFileData("PATH",responseHash));
            writeDataFiles(rootPath, replayerRow);

            if(req.isStaticRequest()){
                staticData.put(path,replayerRow);
            }else{
                dynamicData.add(replayerRow);
            }
            //ADD the crap
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String calculateMd5(Object data, MessageDigest md) {
        if(data==null){
            return "0";
        }
        if(data instanceof String){
            if(((String)data).length()==0) return "0";
            md.update(((String)data).getBytes(StandardCharsets.UTF_8));
        }else{
            if(((byte[])data).length==0) return "0";
            md.update((byte[])data);
        }
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        return bigInt.toString(16);
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

    public void save(String id) throws IOException {
        synchronized (this) {
            var result = new ReplayerResult();
            var partialResult = new ArrayList<ReplayerRow>();
            var rootPath = buildPath(id);
            if(!Files.isDirectory(rootPath)){
                Files.createDirectory(rootPath);
            }
            for (var staticRow :this.staticData.entrySet()) {
                var rowValue = staticRow.getValue();
                partialResult.add(rowValue);
            }
            staticData.clear();
            while (!dynamicData.isEmpty()) {
                // consume element
                var rowValue = dynamicData.poll();
                partialResult.add(rowValue);
            }
            dynamicData.clear();
            while (!errors.isEmpty()) {
                // consume element
                result.addError(errors.poll());
            }
            errors.clear();
            reorganizeData(result,partialResult);
            var allDataString = mapper.writeValueAsString(result);
            var stringPath = rootPath + File.separator + MAIN_FILE;
            FileWriter myWriter = new FileWriter(stringPath);
            myWriter.write(allDataString);
            myWriter.close();
        }
    }

    private void writeDataFiles(Path rootPath, ReplayerRow rowValue) throws IOException {
        var rowId = rowValue.getId();
        if(!rowValue.getRequestFile().getMd5().equalsIgnoreCase("0")){
            var stringPath = rootPath + File.separator + rowId + ".request";
            var requestPath = Path.of(stringPath);
            if(rowValue.getRequest().isBinaryRequest()) {
                Files.write(requestPath, rowValue.getRequest().getRequestBytes());
            }else{
                FileWriter myWriter = new FileWriter(stringPath);
                myWriter.write(rowValue.getRequest().getRequestText());
                myWriter.close();
            }
            rowValue.getRequest().setRequestBytes(null);
            rowValue.getRequest().setRequestText(null);
        }
        if(!rowValue.getResponseFile().getMd5().equalsIgnoreCase("0")){
            var stringPath = rootPath + File.separator + rowId + ".response";
            var requestPath = Path.of(stringPath);
            if(rowValue.getResponse().isBinaryResponse()) {
                Files.write(requestPath, rowValue.getResponse().getResponseBytes());
            }else{
                FileWriter myWriter = new FileWriter(stringPath);
                myWriter.write(rowValue.getResponse().getResponseText());
                myWriter.close();
            }
            rowValue.getResponse().setResponseBytes(null);
            rowValue.getResponse().setResponseText(null);
        }
    }

    private void reorganizeData(ReplayerResult destination, ArrayList<ReplayerRow> source) {
        //group by r_method,r_path,r_query,r_request_text,r_request_bin,r_response_text,r_response_bin order by r_path desc
        //group by r_method,r_path,r_query,r_request_text,r_request_bin                                order by r_path desc
    }

    public boolean replay(Request req, Response res) {
        return false;
    }
}
