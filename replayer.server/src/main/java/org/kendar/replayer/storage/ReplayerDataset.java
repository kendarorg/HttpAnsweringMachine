package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.SerializableResponse;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayerDataset {
    private static final String MAIN_FILE ="runall.json";
    private final Logger logger;
    private DataReorganizer dataReorganizer;
    private ObjectMapper mapper = new ObjectMapper();
    private ConcurrentLinkedQueue<ReplayerRow> dynamicData = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,ReplayerRow> staticData = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    private AtomicInteger counter = new AtomicInteger(0);

    private String name;
    private String replayerDataDir;
    private String description;
    private ReplayerResult replayerResult;
    private ConcurrentHashMap<Integer,Object> states = new ConcurrentHashMap<>();

    public ReplayerDataset(String name, String replayerDataDir, String description, LoggerBuilder loggerBuilder,
                           DataReorganizer dataReorganizer){
        this.name = name;
        this.replayerDataDir = replayerDataDir;
        this.description = description;
        this.logger = loggerBuilder.build(ReplayerDataset.class);
        this.dataReorganizer = dataReorganizer;
    }


    public String getName() {
        return name;
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

            result.setDescription(description);
            dataReorganizer.reorganizeData(result,partialResult);
            var allDataString = mapper.writeValueAsString(result);
            var stringPath = rootPath + File.separator + name+".json";
            FileWriter myWriter = new FileWriter(stringPath);
            myWriter.write(allDataString);
            myWriter.close();
        }
    }

    public void add(Request req, Response res){
        var path = req.getHost()+req.getPath();
        try {


            MessageDigest md = MessageDigest.getInstance("MD5");
            String responseHash = null;

            if(req.isStaticRequest() && staticData.contains(path)){
                var alreadyPresent = staticData.get(path);
                responseHash=calculateMd5(res.getResponse(),md);
                if(!responseHash.equalsIgnoreCase(alreadyPresent.getResponseHash())){
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
            replayerRow.setRequestHash(calculateMd5(req.getRequest(),md));
            replayerRow.setResponseHash(responseHash);

            if(req.isStaticRequest()){
                staticData.put(path,replayerRow);
            }else{
                dynamicData.add(replayerRow);
            }
            //ADD the crap
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("Error recording request "+path,e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error recording request "+path,e);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error recording request "+path,e);
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



    public ReplayerResult load() throws IOException {
        var rootPath = Path.of(replayerDataDir);
        if(!Files.isDirectory(rootPath)){
            Files.createDirectory(rootPath);
        }
        var stringPath = Path.of(rootPath + File.separator + name+".json");
        replayerResult = mapper.readValue(stringPath.toFile(),ReplayerResult.class);
        return replayerResult;
    }

    private ReplayerRow findStaticMatch(Request sreq,String contentHash) {
        var matchingQuery = -1;
        ReplayerRow founded =null;
        for (var row :replayerResult.getStaticRequests()) {
            var rreq = row.getRequest();
            if(!sreq.getPath().equals(rreq.getPath()))continue;
            if(!sreq.getHost().equals(rreq.getHost()))continue;
            var matchedQuery = matchQuery(rreq.getQuery(), sreq.getQuery());
            if(rreq.isBinaryRequest()== sreq.isBinaryRequest()){
                if(row.getRequestHash().equalsIgnoreCase(contentHash)){
                    matchedQuery+=20;
                }
            }

            if(matchedQuery> matchingQuery){
                matchingQuery =matchedQuery;
                founded = row;
            }
        }
        return founded;
    }

    private ReplayerRow findDynamicMatch(Request sreq) {
        var matchingQuery = -1;
        ReplayerRow founded =null;
        for (var row :replayerResult.getDynamicRequests()) {
            var rreq = row.getRequest();
            //Avoid already running stuffs
            if(states.contains(row.getId()))continue;
            if(!sreq.getPath().equals(rreq.getPath()))continue;
            if(!sreq.getHost().equals(rreq.getHost()))continue;
            var matchedQuery = matchQuery(rreq.getQuery(), sreq.getQuery());
            if(rreq.isBinaryRequest()== sreq.isBinaryRequest()){
                matchedQuery+=1;
            }

            if(matchedQuery> matchingQuery){
                matchingQuery =matchedQuery;
                founded = row;
            }
        }
        if(founded!=null){
            states.put(founded.getId(),"");
        }
        return founded;
    }

    public SerializableResponse findResponse(Request req) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            var contentHash = calculateMd5(req.getRequest(),md);
            ReplayerRow founded = findStaticMatch(req,contentHash);
            if (founded != null) {
                return founded.getResponse();
            }
            founded = findDynamicMatch(req);
            if (founded != null) {
                return founded.getResponse();
            }
            return null;
        }catch (Exception ex){
            logger.error("ERror!",ex);
            return null;
        }
    }

    private int matchQuery(Map<String, String> left, Map<String, String> right) {
        var result = 0;
        for (var leftItem:left.entrySet()) {
            for (var rightItem:right.entrySet()) {
                if(leftItem.getKey().equalsIgnoreCase(rightItem.getKey())){
                    result++;
                    if(leftItem.getValue()==null){
                        if(rightItem.getValue()==null){
                            result++;
                        }
                    }else if(leftItem.getValue().equalsIgnoreCase(rightItem.getValue())){
                        result++;
                    }
                }
            }
        }
        return result;
    }

    public void delete(int line) {
        String staticIndex = null;
        for (var entry :
                staticData.entrySet()) {
            if(entry.getValue().getId()==line){
                staticIndex= entry.getKey();
            }
        }
        if(staticIndex == null){
            ReplayerRow toRemove = null;
            for(var entry: dynamicData){
                if(entry.getId()==line){
                    toRemove = entry;
                }
            }
            if(toRemove!=null){
                dynamicData.remove(toRemove);
            }
        }else{
            staticData.remove(staticIndex);
        }
    }

    public void add(ReplayerRow row) {

        var path = row.getRequest().getHost()+row.getRequest().getPath();
        if(row.getRequest().isStaticRequest()){
            staticData.put(path,row);
        }else{
            dynamicData.add(row);
        }
    }

    public void saveMods() throws IOException {
        save();
    }
}
