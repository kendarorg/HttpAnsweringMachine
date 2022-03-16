package org.kendar.replayer;

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
