package org.kendar.replayer.generator;

import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.utils.FileResourcesUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                .add("public class "+id+"Test {")
                .tab(a->{a
                        .add("@BeforeAll")
                        .add("void beforeAll(){")
                        .tab(b->{b
                                .add("//UPLOADTHEFILE");
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

    private void addRequest(SpecialStringBuilder a, String s) {

    }
}
