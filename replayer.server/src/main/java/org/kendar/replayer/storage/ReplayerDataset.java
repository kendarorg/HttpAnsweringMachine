package org.kendar.replayer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.SerializableResponse;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplayerDataset {
    private static final String MAIN_FILE ="runall.json";
    private final Logger logger;
    private ObjectMapper mapper = new ObjectMapper();
    private ConcurrentLinkedQueue<ReplayerRow> dynamicData = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String,ReplayerRow> staticData = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    private AtomicInteger counter = new AtomicInteger(0);

    private String name;
    private String replayerDataDir;
    private String description;
    private ReplayerResult replayerResult;

    public ReplayerDataset(String name, String replayerDataDir, String description, LoggerBuilder loggerBuilder){
        this.name = name;
        this.replayerDataDir = replayerDataDir;
        this.description = description;
        this.logger = loggerBuilder.build(ReplayerDataset.class);
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
            reorganizeData(result,partialResult);
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



    public void load() throws IOException {
        var rootPath = Path.of(replayerDataDir);
        if(!Files.isDirectory(rootPath)){
            Files.createDirectory(rootPath);
        }
        var stringPath = Path.of(rootPath + File.separator + name+".json");
        replayerResult = mapper.readValue(stringPath.toFile(),ReplayerResult.class);
    }

    private void groupMultipleRequests(ArrayList<ReplayerRow> source, HashMap<String, List<ReplayerRow>> byRequest, HashMap<String, List<ReplayerRow>> byRequestResponse) {
        for (var row : source) {
            var byRequestDs = calculateDataResult(row,true);
            var byRequestResponseDs = calculateDataResult(row,true);
            if(!byRequest.containsKey(byRequestDs)){
                byRequest.put(byRequestDs,new ArrayList<>());
            }
            byRequest.get(byRequestDs).add(row);

            if(!byRequestResponse.containsKey(byRequestResponseDs)){
                byRequestResponse.put(byRequestResponseDs,new ArrayList<>());
            }
            byRequest.get(byRequestResponseDs).add(row);
        }
    }

    private void reorganizeData(ReplayerResult destination, ArrayList<ReplayerRow> source) {
        var requestGroups = new HashMap<String, List<ReplayerRow>>();
        var requestResponseGroups = new HashMap<String,List<ReplayerRow>>();
        groupMultipleRequests(source, requestGroups, requestResponseGroups);

        var staticIndexes = setupStaticIndexes(requestGroups, requestResponseGroups);

        var toStaticizeIndexes=new ArrayList<ReplayerRow>();
        //This will be removed
        var toDeduplicateIndexes=new ArrayList<ReplayerRow>();
        var singleStatefulIndex=new ArrayList<ReplayerRow>();
        organizeByNewDiscoveredType(requestGroups, staticIndexes, toStaticizeIndexes, toDeduplicateIndexes, singleStatefulIndex);

        for(var item: singleStatefulIndex){
            item.getRequest().setStaticRequest(false);
            destination.getDynamicRequests().add(item);
        }
        for(var item: toStaticizeIndexes){
            item.getRequest().setStaticRequest(true);
            destination.getStaticRequests().add(item);
        }
    }

    private void organizeByNewDiscoveredType(HashMap<String, List<ReplayerRow>> requestGroups, ArrayList<List<ReplayerRow>> staticIndexes, ArrayList<ReplayerRow> toStaticizeIndexes, ArrayList<ReplayerRow> toDeduplicateIndexes, ArrayList<ReplayerRow> singleStatefulIndex) {
        for(var request : requestGroups.entrySet()){
            var requestIndexes = request.getValue();
            boolean isMatching = false;
            for(var staticIndex: staticIndexes){
                //Find matching static requests
                if(containsTheSameIndexes(requestIndexes,staticIndex)){
                    isMatching = true;
                    //The first one should become static
                    toStaticizeIndexes.add(staticIndex.get(0));
                    if(staticIndex.size()>0){
                        //The other will be removed (as duplicates)
                        for(int i=1;i<staticIndex.size();i++){
                            toDeduplicateIndexes.add(staticIndex.get(i));
                        }
                    }
                    break;
                }
            }
            if(!isMatching){
                //They will become stateful
                for(int i=1;i<requestIndexes.size();i++){
                    singleStatefulIndex.add(requestIndexes.get(i));
                }
            }
        }
    }

    private ArrayList<List<ReplayerRow>> setupStaticIndexes(HashMap<String, List<ReplayerRow>> requestGroups, HashMap<String, List<ReplayerRow>> requestResponseGroups) {
        var staticIndexes = new ArrayList<List<ReplayerRow>>();
        for(var request: requestGroups.entrySet()){
            var requestIndexes = request.getValue();
            for(var requestResponse: requestResponseGroups.entrySet()){
                var requestResponseIndexes = requestResponse.getValue();
                //If the request only is able to discern the API (request ids=request/response ids
                if(containsTheSameIndexes(requestIndexes,requestResponseIndexes)){
                    staticIndexes.add(requestIndexes);
                    break;
                }
            }
        }
        return staticIndexes;
    }

    private boolean containsTheSameIndexes(List<ReplayerRow> left, List<ReplayerRow> right) {
        for(var leftItem :left){
            var found = false;
            for(var rightItem :right){
                if(rightItem.getId()==leftItem.getId()){
                    found=true;
                    break;
                }
            }
            if(!found){
                return false;
            }
        }
        for(var rightItem :right){
            var found = false;
            for(var leftItem :left){
                if(rightItem.getId()==leftItem.getId()){
                    found=true;
                    break;
                }
            }
            if(!found){
                return false;
            }
        }
        return true;
    }

    public  String calculateDataResult(ReplayerRow row, boolean byRequestOnly) {
        var req= row.getRequest();
        var result = req.getHost()+"|"+req.getPath();
        if(req.getQuery()!=null) {
            SortedSet<String> keys = new TreeSet<>(req.getQuery().keySet());
            for (var q : keys) {
                result += "|" + q + "=" + req.getQuery().get(q);
            }
        }
        result+="|"+row.getRequestHash();
        if(!byRequestOnly){
            result+="|"+row.getResponseHash();
        }
        return result;
    }

    public SerializableResponse findResponse(Request req) {
        return null;
    }
}
