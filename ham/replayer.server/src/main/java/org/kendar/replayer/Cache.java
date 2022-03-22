package org.kendar.replayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> values = new ConcurrentHashMap<>();

    public String get(String runId,String key){
        runId = runId.toLowerCase(Locale.ROOT);
        key = key.toLowerCase(Locale.ROOT);
        if(!values.containsKey(runId))return null;
        if(!values.get(runId).containsKey(key))return null;
        return values.get(runId).get(key);
    }

    public List<String> getKeys(String runId){
        var result = new ArrayList<String>();
        if(values.containsKey(runId)){
            result.addAll(values.get(runId).values());
        }
        return result;
    }

    public String replaceAll(String runId,String src){
        if(src==null) return src;
        if(!values.containsKey(runId))return src;
        var keys = this.getKeys(runId);
        for(var vv:values.get(runId).entrySet()){
            src = src.replaceAll("##"+vv.getKey()+"##",get(runId,vv.getValue()));
        }
        return src;
    }
    public void set(String runId,String key, String value){
        runId = runId.toLowerCase(Locale.ROOT);
        key = key.toLowerCase(Locale.ROOT);
        if(!values.containsKey(runId)){
            values.put(runId,new ConcurrentHashMap<>());
        }
        values.get(runId).put(key,value);
    }
    public void remove(String runId,String key){
        runId = runId.toLowerCase(Locale.ROOT);
        key = key.toLowerCase(Locale.ROOT);
        if(!values.containsKey(runId))return;

        values.get(runId).remove(key);
    }
    public void remove(String runId){
        runId = runId.toLowerCase(Locale.ROOT);
        values.remove(runId);
    }
}
