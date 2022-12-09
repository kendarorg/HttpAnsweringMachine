package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.apis.models.SingleScript;
import org.kendar.replayer.apis.models.SingleScriptLine;
import org.kendar.replayer.storage.*;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.RequestUtils;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class SingleScriptAPI implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();

    private final String replayerData;
    private final FileResourcesUtils fileResourcesUtils;
    private final LoggerBuilder loggerBuilder;
    private final Md5Tester md5Tester;
    private HibernateSessionFactory sessionFactory;

    public SingleScriptAPI(
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester,
            JsonConfiguration configuration,
            HibernateSessionFactory sessionFactory) {

        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
        this.md5Tester = md5Tester;
        this.sessionFactory = sessionFactory;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/v2/recording/{id}",
            method = "GET")
    @HamDoc(description = "Retrieve all the content of a script",tags = {"plugin/replayer"},
            path = @PathParameter(key = "id"),
            responses = @HamResponse(
                    body = SingleScript.class
            )
    )
    public void listAllRecordingSteps(Request req, Response res) throws Exception {
        var id = Long.parseLong(req.getPathParameter("id"));
        var result = new SingleScript();
        sessionFactory.query(em-> {
            DbRecording recording = (DbRecording) em.createQuery("SELECT e FROM DbRecording e WHERE e.id=" + id).getResultList().get(0);
            List<CallIndex> indexLines = em
                    .createQuery("SELECT e FROM CallIndex e WHERE e.recordingId=" + id)
                    .getResultList();
            HashMap<Long,ReplayerRow> rows = new HashMap<>();
            em
                    .createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId=" + id)
                    .getResultList()
                    .stream()
                    .forEach((a)->{
                        var idr = ((ReplayerRow)a).getId();
                        if(!rows.containsKey(idr)) {
                            rows.put(idr, (ReplayerRow) a);
                        }else{
                            System.out.println("AAAAAAA");
                        }
                    });

            result.setName(recording.getName());
            result.setId(recording.getId());
            result.setDescription(recording.getDescription());
            for(var index: indexLines){

                var line = rows.get(index.getReference());
                if(line == null) continue;
                var newLine = new SingleScriptLine();
                newLine.setId(index.getId());
                newLine.setRequestMethod(line.getRequest().getMethod());
                newLine.setRequestPath(line.getRequest().getPath());
                newLine.setRequestHost(line.getRequest().getHost());
                newLine.setReference(index.getReference());
                newLine.setPactTest(index.isPactTest());
                newLine.setStimulatorTest(index.isStimulatorTest());
                newLine.setStimulatedTest(line.isStimulatedTest());
                newLine.setQueryCalc(RequestUtils.buildFullQuery(line.getRequest()));
                newLine.setPreScript(index.getPreScript()!=null);
                newLine.setScript(index.getPostScript()!=null);
                newLine.setRequestHashCalc(isHashPresent(line.getRequestHash()));
                newLine.setResponseHashCalc(isHashPresent(line.getResponseHash()));
                newLine.setResponseStatusCode(line.getResponse().getStatusCode());
                result.getLines().add(newLine);
            }

        });


        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    private boolean isHashPresent(String hash) {
        return hash!=null && !hash.isEmpty() && !hash.equalsIgnoreCase("0");
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
