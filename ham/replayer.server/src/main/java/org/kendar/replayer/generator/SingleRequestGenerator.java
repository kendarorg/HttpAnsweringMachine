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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SingleRequestGenerator {
    private FileResourcesUtils fileResourcesUtils;
private ObjectMapper mapper = new ObjectMapper();

    public SingleRequestGenerator(FileResourcesUtils fileResourcesUtils) {

        this.fileResourcesUtils = fileResourcesUtils;
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
                .add("import org.apache.http.client.methods.*;")
                .add("import org.apache.http.impl.client.CloseableHttpClient;")
                .add("import org.apache.http.impl.client.HttpClients;")
                .add("import org.junit.jupiter.api.Test;")
                .add("import java.io.IOException;")
                .add("import java.util.Scanner;")
                .add()
                .add("public class " + recordingId + "Test {")
                .tab(a -> {
                    a
                            .add("@Test")
                            .add("void doTestNavigation(){")
                            .tab(b -> {
                                b
                                        .add("//UPLOAD THE REPLAYER RESULT")
                                        .add("CloseableHttpClient httpClient = HttpClientBuilder.create();")
                                        .add("var request = new HttpPost(\"https://www.local.test\");")
                                        .add("var data = new Scanner(this.getClass().getResourceAsStream(\"/"+pack.replaceAll("\\.","/")+"/recording.json\"), \"UTF-8\").next();")
                                        .add("var jsonFile=\"{\\\"name\\\":\\\""+recordingId+".json\\\",\\\"data\\\":\\\"\"+Base64.getEncoder().encodeToString(data.getBytes())+\"\\\"}\";")
                                        .add("HttpEntity entity = new StringEntity(data, ContentType.create(\"application/json\"));")
                                        .add("((HttpEntityEnclosingRequestBase) request).setEntity(entity);")
                                        .add("httpResponse = httpClient.execute(request);")
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
        var response = row.getResponse();
        var address = RequestUtils.buildFullAddress(request);
        var buildMethod =
                request.getMethod().substring(0, 1).toUpperCase(Locale.ROOT) +
                        request.getMethod().substring(1).toLowerCase(Locale.ROOT);
        a
                .add("CloseableHttpClient httpClient = HttpClientBuilder.create();")
                .add("var request = new Http" + buildMethod + "(\""+address+"\");")
                .add(v ->
                        request.getHeaders().entrySet().stream().map(h ->
                                v.add("request.addHeader(\"" + h.getKey() + "\",\"" + h.getValue() + "\");")).collect(Collectors.toList()))
                .add("request.addHeader(\"Host\",\"" + request.getHost() + "\");");
        var resourceFileResponse = pack.replaceAll("\\.","/")+"/"+recordingId+"/row_"+row.getId()+"_res";
        if(isRequestWithBody(request)){
            var contentType = getCleanContentType( request.getHeader("content-type"));
            var resourceFile = pack.replaceAll("\\.","/")+"/"+recordingId+"/row_"+row.getId()+"_req";
            if(request.isBinaryRequest()){
                a
                        .add("var data = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(\"/"+resourceFile+"\").toURI()));")
                        .add("HttpEntity entity = new ByteArrayEntity(data, ContentType.create(\""+contentType+"\"));");
            }else{
                a
                        .add("var data = new Scanner(this.getClass().getResourceAsStream(\"/"+resourceFile+"\"), \"UTF-8\").next();")
                        .add("HttpEntity entity = new StringEntity(data, ContentType.create(\""+contentType+"\"));");
            }
            a
                    .add("((HttpEntityEnclosingRequestBase) request).setEntity(entity);");
        }

        if(response.isBinaryResponse()){
            a
                    .add("var expectedResponseData = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(\"/"+resourceFileResponse+"\").toURI()));");
        }else{
            a
                    .add("var expectedResponseData = new Scanner(this.getClass().getResourceAsStream(\"/"+resourceFileResponse+"\"), \"UTF-8\").next();");
        }

        a
                .add("var expectedResponseCode = "+response.getStatusCode()+";")
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
