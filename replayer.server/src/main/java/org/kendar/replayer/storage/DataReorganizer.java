package org.kendar.replayer.storage;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataReorganizer {
    public void reorganizeData(ReplayerResult destination, ArrayList<ReplayerRow> source) {
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
}
