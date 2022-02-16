package org.kendar.replayer.generator;

import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.utils.FileResourcesUtils;

import java.util.HashMap;
import java.util.Map;

public class SingleRequestGenerator {
    private FileResourcesUtils fileResourcesUtils;


    public SingleRequestGenerator(FileResourcesUtils fileResourcesUtils){

        this.fileResourcesUtils = fileResourcesUtils;
    }

    public Map<String,String> generateRequestResponse(String id,ReplayerResult data){
        var result = new HashMap<String, String>();
        result.put(id+"Test.java",buildTestCode(id,data));

        return result;
    }

    private String buildTestCode(String id, ReplayerResult data) {
        var allRows = new HashMap<Integer,ReplayerRow>();
        for (var row :data.getStaticRequests()) {
            allRows.put(row.getId(),row);
        }
        for (var row :data.getDynamicRequests()) {
            allRows.put(row.getId(),row);
        }

        return new SpecialStringBuilder()
                .add("import org.apache.http.HttpResponse'")
                .add("import org.apache.http.StatusLine;" )
                .add("import org.apache.http.client.methods.HttpGet;" )
                .add("import org.apache.http.impl.client.CloseableHttpClient;" )
                .add("import org.apache.http.impl.client.HttpClients;" )
                .add("import org.junit.jupiter.api.BeforeAll;" )
                .add("import org.junit.jupiter.api.BeforeEach;" )
                .add("import org.junit.jupiter.api.Test;" )
                .add("import java.io.IOException;" )
                .add("import java.util.Scanner;")
                .add()
                .add("public class "+id+"Test {")
                .tab(a->{
                    a
                        .add("@BeforeAll")
                        .add("void beforeAll(){")
                        .tab(b->{b
                                .add("//UPLOADTHEREPLAYERRESULT")
                                .add("//STARTREPLAYING");
                        })
                        .add("}")
                        .add();
                    for (var line :data.getIndexes()) {
                        var row = allRows.get(line.getReference());
                        addRequest(a,"d_"+line.getId(),line,row);
                    }

                })
                .build();
    }

    private void addRequest(SpecialStringBuilder a, String methodName, CallIndex line, ReplayerRow row) {
        a
                //.add("@Test")
                .add("private void "+methodName+"(){")
                .tab(b->{b
                        .add(c->makeTheCall(c,line,row))
                        .add(c->retrieveTheData(c,line,row))
                        .add(c->checkTheReturn(c,line,row));
                })
                .add("}")
                .add();
    }

    private void checkTheReturn(SpecialStringBuilder c, CallIndex line, ReplayerRow row) {
        
    }

    private void retrieveTheData(SpecialStringBuilder c, CallIndex line, ReplayerRow row) {

    }

    private void makeTheCall(SpecialStringBuilder a, CallIndex line, ReplayerRow row) {

    }
}
