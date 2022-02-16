package org.kendar.replayer.generator;

import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.RequestUtils;
import org.kendar.utils.FileResourcesUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SingleRequestGenerator {
    private FileResourcesUtils fileResourcesUtils;


    public SingleRequestGenerator(FileResourcesUtils fileResourcesUtils) {

        this.fileResourcesUtils = fileResourcesUtils;
    }

    public Map<String, byte[]> generateRequestResponse(String pack,String recordingId, ReplayerResult data) {
        var result = new HashMap<String, byte[]>();

        var allRows = new HashMap<Integer, ReplayerRow>();
        for (var row : data.getStaticRequests()) {
            allRows.put(row.getId(), row);
        }
        for (var row : data.getDynamicRequests()) {
            allRows.put(row.getId(), row);
        }

        //Build the code
        var srcDir = "src/test/java/"+pack.replaceAll(".","/")+"/"+recordingId;
        result.put(srcDir+"/"+recordingId+"Test.java", buildTestCode(pack,recordingId, data,allRows).getBytes(StandardCharsets.UTF_8));

        //Build the resources
        var rsrcDir = "src/test/resources/"+pack.replaceAll(".","/")+"/"+recordingId;

        for(var row:allRows.values()){
            var response = row.getResponse();
            var request = row.getRequest();
            var resourceFile = rsrcDir+"/row_"+row.getId()+"_";
            var resourceFileRes = resourceFile+"res";
            var res= writeData(resourceFileRes,response.isBinaryResponse(),response.getResponseBytes(),response.getResponseText());
            result.put(resourceFileRes,res);
            var resourceFileReq = resourceFile+"req";
            var req = writeData(resourceFileReq,request.isBinaryRequest(),request.getRequestBytes(),request.getRequestText());
            result.put(resourceFileReq,req);
        }

        return result;
    }

    private byte[] writeData(String resourceFileRes, boolean binaryResponse, byte[] responseBytes, String responseText) {
        if(binaryResponse && responseBytes!=null && responseBytes.length>0){
            return responseBytes;
        }else if(!binaryResponse && responseText!=null && !responseText.isEmpty()){
            return responseText.getBytes(StandardCharsets.UTF_8);
        }else{
            return new byte[]{};
        }
    }

    private String buildTestCode(String pack, String recordingId, ReplayerResult data, HashMap<Integer, ReplayerRow> allRows) {


        return new SpecialStringBuilder()
                .add("package "+pack)
                .add()
                .add("import org.apache.http.HttpResponse;")
                .add("import org.apache.http.StatusLine;")
                .add("import org.apache.http.client.methods.HttpGet;")
                .add("import org.apache.http.impl.client.CloseableHttpClient;")
                .add("import org.apache.http.impl.client.HttpClients;")
                .add("import org.junit.jupiter.api.BeforeAll;")
                .add("import org.junit.jupiter.api.BeforeEach;")
                .add("import org.junit.jupiter.api.Test;")
                .add("import java.io.IOException;")
                .add("import java.util.Scanner;")
                .add()
                .add("public class " + recordingId + "Test {")
                .tab(a -> {
                    a
                            .add("@BeforeAll")
                            .add("void beforeAll(){")
                            .tab(b -> {
                                b
                                        .add("//UPLOADTHEREPLAYERRESULT")
                                        .add("//STARTREPLAYING");
                            })
                            .add("}")
                            .add();
                    for (var line : data.getIndexes()) {
                        var row = allRows.get(line.getReference());
                        addRequest(a,recordingId, "d_" + line.getId(), line, row,pack);
                    }

                })
                .build();
    }

    private void addRequest(SpecialStringBuilder a, String recordingId, String methodName, CallIndex line, ReplayerRow row, String pack) {
        a
                //.add("@Test")
                .add("private void " + methodName + "(){")
                .tab(b -> {
                    b
                            .add(c -> makeTheCall(c,recordingId, line, row,pack))
                            .add(c -> retrieveTheData(c,recordingId, line, row,pack))
                            .add(c -> checkTheReturn(c,recordingId, line, row,pack));
                })
                .add("}")
                .add();
    }

    private void checkTheReturn(SpecialStringBuilder a, String recordingId, CallIndex line, ReplayerRow row, String pack) {

    }


    private void retrieveTheData(SpecialStringBuilder a, String recordingId, CallIndex line, ReplayerRow row, String pack) {
        a
                .add("int statusCode = httpResponse.getStatusLine().getStatusCode();")
                .add("String contentType = responseEntity.getContentType().getValue();");
        var response = row.getResponse();
        //var response =
        if(response.isBinaryResponse() && response.getResponseBytes()!=null && response.getResponseBytes().length>0){
            a
                    .add("InputStream in = responseEntity.getContent();")
                    .add("byte[] result = IOUtils.toByteArray(in);");
        }else if(!response.isBinaryResponse() && response.getResponseText()!=null && !response.getResponseText().isEmpty()){
            a
                    .add("InputStream in = responseEntity.getContent();")
                    .add("String result = IOUtils.toString(in, StandardCharsets.UTF_8);");
        }
    }

    private String[] bodyMethod = new String[]{
            "post","put","patch"
    };

    private void makeTheCall(SpecialStringBuilder a, String recordingId, CallIndex line, ReplayerRow row, String pack) {
        var request = row.getRequest();
        var address = RequestUtils.buildFullAddress(request);
        var buildMethod =
                request.getMethod().substring(0, 1).toUpperCase(Locale.ROOT) +
                        request.getMethod().substring(1).toLowerCase(Locale.ROOT);
        a
                .add("CloseableHttpClient httpClient = HttpClientBuilder.create();")
                .add("var request = new " + buildMethod + "(\""+address+"\");")
                .add(v ->
                        request.getHeaders().entrySet().stream().map(h ->
                                v.add("request.addHeader(\"" + h.getKey() + "\",\"" + h.getValue() + "\");")))
                .add("request.addHeader(\"Host\",\"" + request.getHost() + "\");");
        if(isRequestWithBody(request)){
            var contentType = getCleanContentType( request.getHeader("content-type"));
            var resourceFile = "/"+pack.replaceAll(".","/")+"/"+recordingId+"/row_"+row.getId()+"_req";
            a.add("HttpEntity entity = ");
            if(request.isBinaryRequest()){
                a
                        .add("var data = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(\"/"+resourceFile+"\").toURI()));")
                        .add("HttpEntity entity = new ByteArrayEntity(data, ContentType.create("+contentType+"));");
            }else{
                a
                        .add("var data = new Scanner(AppropriateClass.class.getResourceAsStream(\"/"+resourceFile+".txt\"), \"UTF-8\").next();")
                        .add("HttpEntity entity = new StringEntity(data, ContentType.create("+contentType+"));");
            }
            a
                    .add("((HttpEntityEnclosingRequestBase) request).setEntity(entity);");
        }
        a
                .add("httpResponse = httpClient.execute(request);")
                .add("HttpEntity responseEntity = httpResponse.getEntity();");
    }

    private String getCleanContentType(String contentType) {
        if(contentType.indexOf(";")>0){
            var spl = contentType.split(";");
            contentType = spl[0];
        }
        return contentType;
    }

    private boolean isRequestWithBody(Request request) {
        if(!Arrays.stream(bodyMethod).anyMatch(a->a.equalsIgnoreCase(request.getMethod()))){
            return false;
        }
        if(request.isBinaryRequest() && request.getRequestBytes()!=null && request.getRequestBytes().length >0 ){
            return true;
        }
        if(!request.isBinaryRequest() && request.getRequestText()!=null && request.getRequestText().length() >0 ){
            return true;
        }
        return false;
    }
}
