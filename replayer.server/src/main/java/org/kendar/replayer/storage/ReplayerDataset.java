package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayerDataset {
    private static final String MAIN_FILE ="runall.json";
    private ObjectMapper mapper = new ObjectMapper();
    private ConcurrentLinkedQueue<ReplayerRow> dynamicData = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,ReplayerRow> staticData = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    private AtomicInteger counter = new AtomicInteger(0);

    private String name;
    private String replayerDataDir;

    public ReplayerDataset(String name,String replayerDataDir){
        this.name = name;
        this.replayerDataDir = replayerDataDir;
    }

    public void save() throws IOException {
        synchronized (this) {
            var result = new ReplayerResult();
            var partialResult = new ArrayList<ReplayerRow>();
            var rootPath = Path.of(replayerDataDir);
            if(!Files.isDirectory(rootPath)){
                Files.createDirectory(rootPath);
            }
            for (var staticRow :this.staticData.entrySet()) {
                var rowValue = staticRow.getValue();
                partialResult.add(rowValue);
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
            errors.clear();
            reorganizeData(result,partialResult);
            var allDataString = mapper.writeValueAsString(result);
            var stringPath = rootPath + File.separator + MAIN_FILE;
            FileWriter myWriter = new FileWriter(stringPath);
            myWriter.write(allDataString);
            myWriter.close();
        }
    }


    private void reorganizeData(ReplayerResult destination, ArrayList<ReplayerRow> source) {
        //group by r_method,r_path,r_query,r_request_text,r_request_bin,r_response_text,r_response_bin order by r_path desc
        //group by r_method,r_path,r_query,r_request_text,r_request_bin                                order by r_path desc
    }

    public void add(Request req, Response res){
        try {


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
            writeDataFiles(Path.of(replayerDataDir), replayerRow);

            if(req.isStaticRequest()){
                staticData.put(path,replayerRow);
            }else{
                dynamicData.add(replayerRow);
            }
            //ADD the crap
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
}
