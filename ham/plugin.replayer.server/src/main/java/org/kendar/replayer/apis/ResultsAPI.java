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
import org.kendar.replayer.apis.models.RecordingItem;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.storage.TestResults;
import org.kendar.replayer.storage.TestResultsLine;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ResultsAPI  implements FilteringClass {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String replayerData;
    private HibernateSessionFactory sessionFactory;

    public ResultsAPI(JsonConfiguration configuration, HibernateSessionFactory sessionFactory){
        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();
        this.sessionFactory = sessionFactory;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/results",
            method = "GET")
    @HamDoc(description = "Retrieves all the replayer results",tags = {"plugin/replayer"},
    responses = @HamResponse(
            body = RecordingItem[].class
    ))
    public void getResults(Request request, Response response) throws Exception {

        List<TestResults> resultsList = new ArrayList<>();
        Map<Long,String> recNames = new HashMap<>();
        var result = new ArrayList<RecordingItem>();
        sessionFactory.query(em->{
            resultsList.addAll(em.createQuery("SELECT e FROM TestResults e ORDER BY e.timestamp DESC").getResultList());

            List<DbRecording> recordings = em.createQuery("SELECT e FROM DbRecording e").getResultList();
            for(var rs:recordings){
                recNames.put(rs.getId(),rs.getName());
            }
        });

        for(var rs:resultsList) {
            var ra = new RecordingItem();
            ra.setSuccessful(rs.getError()==null || rs.getError().isEmpty() );
            ra.setFileId(rs.getId());
            ra.setTestType(rs.getType());
            ra.setDate(rs.getIsoDate());
            ra.setName(recNames.get(rs.getRecordingId()));
            result.add(ra);

        }

        response.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        response.setResponseText(mapper.writeValueAsString(result));
    }
    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/results/{id}",
            method = "GET")
    @HamDoc(description = "Retrieves a single result",tags = {"plugin/replayer"},
            path = @PathParameter(key = "id"),
            responses = @HamResponse(
                    body = RecordingItem[].class
            )
    )
    public void getResult(Request request, Response response) throws Exception {
        var id = Long.parseLong(request.getPathParameter("id"));
        var result = new RecordingItem();

        sessionFactory.query(em->{
            var dbResult = (TestResults)em.createQuery("SELECT e FROM TestResults e WHERE e.id="+id).getResultList().get(0);
            var recording = (DbRecording)em.createQuery("SELECT e FROM DbRecording e WHERE e.id="+dbResult.getRecordingId()).getResultList().get(0);
            result.setResult(new ArrayList<>());
            result.setFileId(id);
            result.setDate(dbResult.getIsoDate());
            result.setSuccessful(dbResult.getErrors()==null ||dbResult.getErrors().isEmpty());
            result.setTestType(dbResult.getType());
            result.setName(recording.getName());
            for(var ss:em.createQuery("SELECT e FROM TestResultsLine e " +
                    " WHERE e.resultId="+dbResult.getId()+
                    " ORDER BY e.id DESC").getResultList()){
                em.detach(ss);
                result.getResult().add((TestResultsLine) ss);
            }
        });

        response.addHeader(ConstantsHeader.CONTENT_TYPE,ConstantsMime.JSON);
        response.setResponseText(mapper.writeValueAsString(result));

    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/results/{id}",
            method = "DELETE")
    @HamDoc(description = "Deletes a single result",tags = {"plugin/replayer"},
            path = @PathParameter(key = "id")
    )
    public void deleteresult(Request request, Response response) throws Exception {
        var id = Long.parseLong(request.getPathParameter("id"));

        sessionFactory.transactional(em->{
            var dbResult = (TestResults)em.createQuery("SELECT e FROM TestResults e WHERE e.id="+id).getResultList().get(0);
            em.createQuery("DELETE FROM TestResultsLine e " +
                    " WHERE e.resultId="+dbResult.getId()).executeUpdate();
            em.remove(dbResult);
        });
    }
    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
