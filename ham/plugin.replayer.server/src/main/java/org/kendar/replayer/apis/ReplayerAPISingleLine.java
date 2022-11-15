package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPISingleLine implements FilteringClass {

  private final FileResourcesUtils fileResourcesUtils;
  private final LoggerBuilder loggerBuilder;
  final ObjectMapper mapper = new ObjectMapper();
  private final Md5Tester md5Tester;
  private HibernateSessionFactory sessionFactory;
  private final String replayerData;

  public ReplayerAPISingleLine(
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

  @Override
  public String getId() {
    return "org.kendar.replayer.apis.ReplayerAPISingleLine";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "GET")
  @HamDoc(
          tags = {"plugin/replayer"},
          description = "Returns a single line data",
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          responses = @HamResponse(
                  body = ReplayerRow.class
          )
  )
  public void retrieveSingleLineData(Request req, Response res) throws Exception {
    var recordingId = Long.parseLong(req.getPathParameter("id"));
    var line = Long.parseLong(req.getPathParameter("line"));

    try{
    sessionFactory.query(em -> {
      var prevId = (Long) em.createQuery("SELECT COALESCE( MAX(e.id),-1) FROM CallIndex e WHERE" +
              " e.recordingId=" + recordingId + " AND e.id<" + line).getResultList().get(0);

      var nexId = (Long) em.createQuery("SELECT COALESCE( MIN(e.id),-1) FROM CallIndex e WHERE" +
              " e.recordingId=" + recordingId + " AND e.id>" + line).getResultList().get(0);

      var row = (ReplayerRow) em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
              " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);

      res.addHeader("X-NEXT", "" + nexId);
      res.addHeader("X-PREV", "" + prevId);
      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
      res.setResponseText(mapper.writeValueAsString(row));
      return;
    });

    }catch (Exception e){
      res.setStatusCode(404);
      res.setResponseText("Missing id " + recordingId + " with line " + line);
    }

  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "PUT")
  @HamDoc(description = "Modify a rreplayer row",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          requests = @HamRequest(
                  body = ReplayerRow.class
          )
  )
  public void modifySingleLineData(Request req, Response res) throws Exception {
    var recordingId = Long.parseLong(req.getPathParameter("id"));
    var line = Long.parseLong(req.getPathParameter("line"));
    var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);

    try{
    sessionFactory.transactional(em -> {

      var destination = (ReplayerRow) em.createQuery("SELECT e FROM ReplayerRow e WHERE" +
              " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);
      cloneToRow(destination, source);
      em.merge(destination);
      return;
    });
    }catch (Exception e){
      res.setStatusCode(404);
      res.setResponseText("Missing id " + recordingId + " with line " + line);
    }

  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "POST")
  @HamDoc(description = "Add a replayer row",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          requests = @HamRequest(
                  body = ReplayerRow.class
          )
  )
  public void addLineData(Request req, Response res) throws Exception {
    var recordingId = Long.parseLong(req.getPathParameter("id"));
    var line = Long.parseLong(req.getPathParameter("line"));
    var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);

    sessionFactory.transactional(em -> {
      em.createQuery("UPDATE CallIndex SET id=id+1 WHERE" +
              " recordingId="+recordingId+" AND id>="+line).executeUpdate();
      var nexId = (Long)em.createQuery("SELECT MAX(id) FROM CallIndex  WHERE" +
              " recordingId="+recordingId).getResultList().get(0)+1;

      source.setIndex(null);
      source.setId(nexId);
      source.setRecordingId(recordingId);
      em.persist(source);

      var callIndex= new CallIndex();
      callIndex.setRecordingId(recordingId);
      callIndex.setReference(source.getId());
      callIndex.setId(line);
      em.persist(callIndex);
    });


    res.setStatusCode(200);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}/line/{line}",
      method = "DELETE")
  @HamDoc(description = "Remove a replayer row with is lineindex",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")}
  )
  public void deleteSingleLineData(Request req, Response res) throws Exception {

    var recordingId = Long.parseLong(req.getPathParameter("id"));
    var line = Long.parseLong(req.getPathParameter("line"));
    var source = mapper.readValue(req.getRequestText(), ReplayerRow.class);

    sessionFactory.transactional(em -> {
      em.createQuery("DELETE CallIndex WHERE" +
              " recordingId="+recordingId+" AND reference="+line).executeUpdate();

      em.createQuery("DELETE ReplayerRow WHERE" +
              " recordingId="+recordingId+" AND id="+line).executeUpdate();

    });

    res.setStatusCode(200);
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/lineindex/{line}",
          method = "DELETE")
  @HamDoc(description = "Remove the indexline (aka the pointer to replayer row)",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")}
  )
  public void deleteSingleIndexLineData(Request req, Response res) throws Exception {
    var recordingId = Long.parseLong(req.getPathParameter("id"));
    var line = Long.parseLong(req.getPathParameter("line"));

    sessionFactory.transactional(em -> {
      var index = (CallIndex)em.createQuery("SELECT e FROM CallIndex e WHERE" +
              " e.recordingId="+recordingId+" AND e.id="+line).getResultList().get(0);

      em.detach(index);
      em.createQuery("DELETE CallIndex WHERE" +
              " recordingId="+recordingId+" AND id="+line).executeUpdate();

      Long callIndexList = (Long)em.createQuery("SELECT COUNT(*) FROM CallIndex WHERE" +
              " recordingId="+recordingId+" AND reference="+index.getReference())
              .getResultList().get(0);

      //remove the row if nobody references it
      if(callIndexList >0){
        em.createQuery("DELETE ReplayerRow WHERE" +
                " recordingId="+recordingId+" AND id="+index.getReference()).executeUpdate();
      }
    });
    res.setStatusCode(200);
  }

  private void cloneToRow(ReplayerRow destination, ReplayerRow source) {
    var res = destination.getResponse();
    var req = destination.getRequest();
    res.setBinaryResponse(source.getResponse().isBinaryResponse());
    res.setHeaders(source.getResponse().getHeaders());
    res.setStatusCode(source.getResponse().getStatusCode());
    destination.setResponse(res);

    req.setBinaryRequest(source.getRequest().isBinaryRequest());
    req.setHeaders(source.getRequest().getHeaders());
    req.setMethod(source.getRequest().getMethod());
    req.setProtocol(source.getRequest().getProtocol());
    req.setQuery(source.getRequest().getQuery());
    req.setHost(source.getRequest().getHost());
    req.setPath(source.getRequest().getPath());
    req.setPort(source.getRequest().getPort());
    req.setPostParameters(source.getRequest().getPostParameters());
    req.setStaticRequest(source.getRequest().isStaticRequest());
    req.setSoapRequest(source.getRequest().isSoapRequest());
    destination.setStimulatedTest(source.isStimulatedTest());

    destination.setRequest(req);
    destination.setResponse(res);
  }


  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/lineindex/{line}",
          method = "GET")
  @HamDoc(description = "Retrieves the indexline (aka the pointer to replayer row)",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          responses = @HamResponse(
            body =  CallIndex.class
          )
  )
  public void retrieveSingleLineIndexData(Request req, Response res) throws Exception {
    var recordingId = Long.parseLong(req.getPathParameter("id"));
    var line = Long.parseLong(req.getPathParameter("line"));

    try {
      sessionFactory.query(em -> {
        var index = (CallIndex) em.createQuery("SELECT e FROM CallIndex e WHERE" +
                " e.recordingId=" + recordingId + " AND e.id=" + line).getResultList().get(0);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(index));
        return;

      });
    }catch(Exception ex){
      res.setStatusCode(404);
      res.setResponseText("Missing id " + recordingId + " with line " + line);
    }

  }



  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/lineindex/{line}",
          method = "PUT")
  @HamDoc(description = "Addes the indexline (aka the pointer to replayer row)",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id", description="Script Id"),@PathParameter(key = "line",description = "Script line")},
          requests = @HamRequest(
                  body =  CallIndex.class
          )
  )
  public void modifySingleLineIndexData(Request req, Response res) throws Exception {
    var recordingId = Long.parseLong(req.getPathParameter("id"));
    var line = Long.parseLong(req.getPathParameter("line"));
    var source = mapper.readValue(req.getRequestText(), CallIndex.class);
    try{
    sessionFactory.transactional(em -> {
      var destination = (CallIndex)em.createQuery("SELECT e FROM CallIndex e WHERE" +
              " e.recordingId="+recordingId+" AND e.id="+line).getResultList().get(0);
      cloneToIndex(destination, source);
      em.merge(destination);
      return;

    });
    }catch (Exception e){
      res.setStatusCode(404);
      res.setResponseText("Missing id " + recordingId + " with line " + line);
    }
  }

  private void cloneToIndex(CallIndex destination, CallIndex source) {
    destination.setStimulatorTest(source.isStimulatorTest());
    destination.setPactTest(source.isPactTest());
    destination.setReference(source.getReference());
    destination.setDescription(source.getDescription());
  }
}
