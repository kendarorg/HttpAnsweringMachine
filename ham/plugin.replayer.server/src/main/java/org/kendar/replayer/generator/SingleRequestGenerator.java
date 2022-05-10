package org.kendar.replayer.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.RequestUtils;
import org.kendar.utils.FileResourcesUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SingleRequestGenerator {
    private final ObjectMapper mapper = new ObjectMapper();

    public SingleRequestGenerator(FileResourcesUtils fileResourcesUtils) {

    }

    public Map<String, byte[]> generateRequestResponse(String pack,String recordingId, ReplayerResult data) throws JsonProcessingException {
        var result = new HashMap<String, byte[]>();

        var allRows = new HashMap<Integer, ReplayerRow>();
        for (var row : data.getStaticRequests()) {
            allRows.put(row.getId(), row);
        }
        for (var row : data.getDynamicRequests()) {
            allRows.put(row.getId(), row);
        }

        //Build the code
        var srcDir = "src/test/java/"+pack.replaceAll("\\.","/")+"/"+recordingId;
        result.put(srcDir+"/"+recordingId+"Test.java", buildTestCode(pack,recordingId, data,allRows).getBytes(StandardCharsets.UTF_8));

        //Build the resources
        var rsrcDir = "src/test/resources/"+pack.replaceAll("\\.","/")+"/"+recordingId;

        var replayData = mapper.writeValueAsString(data);
        var resourceFileData = rsrcDir+"/recording.json";
        result.put(resourceFileData,replayData.getBytes(StandardCharsets.UTF_8));
        byte[] pom = new byte[0];
        try (var stream = this.getClass().getResourceAsStream("/standards/pom.xml")){
            pom = stream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.put("pom.xml",pom);

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
                .add("package "+pack+"."+recordingId+";")
                .add()
                .add("import org.apache.commons.io.IOUtils;")
                .add("import org.apache.http.HttpEntity;")
                .add("import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;")
                .add("import org.apache.http.client.methods.HttpGet;")
                .add("import org.apache.http.client.methods.HttpPost;")
                .add("import org.apache.http.entity.ContentType;")
                .add("import org.apache.http.entity.StringEntity;")
                .add("import org.apache.http.impl.client.CloseableHttpClient;")
                .add("import org.apache.http.impl.client.HttpClientBuilder;")
                .add("import org.junit.jupiter.api.Test;")
                .add()
                .add("import java.io.IOException;")
                .add("import java.io.InputStream;")
                .add("import java.nio.charset.StandardCharsets;")
                .add("import java.util.Base64;")
                .add("import java.util.Scanner;")
                .add()
                .add("public class " + recordingId + "Test {")
                .tab(a -> {
                    a
                            .add("@Test")
                            .add("void doTestNavigation() throws IOException{")
                            .tab(b -> {
                                b
                                        .add("//UPLOAD THE REPLAYER RESULT")
                                        .add("CloseableHttpClient httpClient = HttpClientBuilder.create().build();")
                                        .add("var request = new HttpPost(\"http://www.local.test\");")
                                        .add("var data = this.getClass().getResourceAsStream(\"/"+pack.replaceAll("\\.","/")+"/"+recordingId+"/recording.json\").readAllBytes();")
                                        .add("var jsonFile=\"{\\\"name\\\":\\\""+recordingId+".json\\\",\\\"data\\\":\\\"\"+Base64.getEncoder().encodeToString(data)+\"\\\"}\";")
                                        .add("HttpEntity entity = new StringEntity(jsonFile, ContentType.create(\"application/json\"));")
                                        .add("((HttpEntityEnclosingRequestBase) request).setEntity(entity);")
                                        .add("var httpResponse = httpClient.execute(request);")
                                        .add("HttpEntity responseEntity = httpResponse.getEntity();")
                                        .add()
                                        .add("//STARTREPLAYING");
                                for (var line : data.getIndexes()) {
                                    var row = allRows.get(line.getReference());
                                    b.add("d_"+line.getId()+"();");
                                }
                            })
                            .add("}")
                            .add();
                    for (var line : data.getIndexes()) {
                        var row = allRows.get(line.getReference());
                        addRequest(a,recordingId, "d_" + line.getId(), line, row,pack);
                    }

                })
                .add("}")
                .build();
    }

    private void addRequest(SpecialStringBuilder a, String recordingId, String methodName, CallIndex line, ReplayerRow row, String pack) {
        a
                //.add("@Test")
                .add("private void " + methodName + "() throws IOException{")
                .tab(b -> b
                        .add(c -> makeTheCall(c,recordingId, line, row,pack))
                        .add(c -> retrieveTheData(c,recordingId, line, row,pack))
                        .add(c -> checkTheReturn(c,recordingId, line, row,pack)))
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

    private final String[] bodyMethod = new String[]{
            "post","put","patch"
    };

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void makeTheCall(SpecialStringBuilder a, String recordingId, CallIndex line, ReplayerRow row, String pack) {
        var request = row.getRequest();
        var response = row.getResponse();
        var address = RequestUtils.buildFullAddress(request,false);
        var buildMethod =
                request.getMethod().substring(0, 1).toUpperCase(Locale.ROOT) +
                        request.getMethod().substring(1).toLowerCase(Locale.ROOT);
        a
                .add("CloseableHttpClient httpClient = HttpClientBuilder.create().build();")
                .add("var request = new Http" + buildMethod + "(\""+address+"\");")
                .add(v ->
                        request.getHeaders().entrySet().stream().map(h ->
                                v.add("request.addHeader(\"" + h.getKey() + "\",\"" + h.getValue().replaceAll("\"","\\\\\"") + "\");")).collect(Collectors.toList()));
        var resourceFileResponse = pack.replaceAll("\\.","/")+"/"+recordingId+"/row_"+row.getId()+"_res";
        if(isRequestWithBody(request)){
            var contentType = getCleanContentType( request.getHeader("content-type"));
            var resourceFile = pack.replaceAll("\\.","/")+"/"+recordingId+"/row_"+row.getId()+"_req";
            if(request.isBinaryRequest()){
                a
                        .add("var data = this.getClass().getResourceAsStream(\"/"+resourceFile+"\").readAllBytes();")
                        .add("HttpEntity entity = new ByteArrayEntity(data, ContentType.create(\""+contentType+"\"));");
            }else{
                a
                        .add("var data = new String(this.getClass().getResourceAsStream(\"/"+resourceFile+"\").readAllBytes());")
                        .add("HttpEntity entity = new StringEntity(data, ContentType.create(\""+contentType+"\"));");
            }
            a
                    .add("((HttpEntityEnclosingRequestBase) request).setEntity(entity);");
        }

        if(response.isBinaryResponse()){
            a
                    .add("var expectedResponseData = this.getClass().getResourceAsStream(\"/"+resourceFileResponse+"\").readAllBytes();");
        }else{
            a
                    .add("var expectedResponseData = new String(this.getClass().getResourceAsStream(\"/"+resourceFileResponse+"\").readAllBytes());");
        }

        a
                .add("var expectedResponseCode = "+response.getStatusCode()+";")
                .add("var httpResponse = httpClient.execute(request);")
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
        if(Arrays.stream(bodyMethod).noneMatch(a->a.equalsIgnoreCase(request.getMethod()))){
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
