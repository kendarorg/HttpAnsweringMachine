package org.kendar.replayer.generator;

import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.utils.FileResourcesUtils;

import java.util.ArrayList;
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
        var allRows = new ArrayList<ReplayerRow>();
        allRows.addAll(data.getDynamicRequests());
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
                .tab(a->{a
                        .add("@BeforeAll")
                        .add("void beforeAll(){")
                        .tab(b->{b
                                .add("//UPLOADTHEREPLAYERRESULT")
                                .add("//STARTREPLAYING");
                        })
                        .add("}")
                        .add();
                    for (var line :data.getDynamicRequests()) {
                        addRequest(a,"d_"+line.getId());
                    }
                    for (var line :data.getStaticRequests()) {
                        addRequest(a,"s_"+line.getId());
                    }

                })
                .build();
    }

    private void addRequest(SpecialStringBuilder a, String methodName) {
        a
            .add("@Test")
            .add("void "+methodName+"(){")
            .tab(b->{b
                    .add("//MAKETHECALL")
                    .add("//RETRIEVETHEDATA")
                    .add("//CHECKTHEDATA");
            })
            .add("}")
            .add();
    }
}
