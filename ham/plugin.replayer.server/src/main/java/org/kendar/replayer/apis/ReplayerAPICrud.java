package org.kendar.replayer.apis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.apis.models.*;
import org.kendar.replayer.engine.ReplayerStatus;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DbRecording;
import org.kendar.replayer.engine.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.RequestUtils;
import org.kendar.servers.http.Response;
import org.kendar.servers.models.JsonFileData;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPICrud implements FilteringClass {
  final ObjectMapper mapper = new ObjectMapper();
  private final Logger logger;
  private final ReplayerStatus replayerStatus;
  private final Md5Tester md5Tester;
  private HibernateSessionFactory sessionFactory;

  private final FileResourcesUtils fileResourcesUtils;
  private final LoggerBuilder loggerBuilder;

  private static TypeReference<HashMap<String, String>> typeRef
          = new TypeReference<>() {
  };

  public ReplayerAPICrud(
          FileResourcesUtils fileResourcesUtils,
          LoggerBuilder loggerBuilder,
          ReplayerStatus replayerStatus,
          Md5Tester md5Tester,
          JsonConfiguration configuration,
          HibernateSessionFactory sessionFactory) {


    this.fileResourcesUtils = fileResourcesUtils;

    this.loggerBuilder = loggerBuilder;
    this.logger = loggerBuilder.build(ReplayerAPICrud.class);
    this.replayerStatus = replayerStatus;
    this.md5Tester = md5Tester;
    this.sessionFactory = sessionFactory;
  }

  @Override
  public String getId() {
    return "org.kendar.replayer.apis.ReplayerAPICrud";
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording",
      method = "GET")
  @HamDoc(description = "Retrieves the list of recordings",tags = {"plugin/replayer"},
    responses = @HamResponse(body = String[].class))
  public void listAllLocalRecordings(Request req, Response res) throws Exception {

    var listOfItems = new ArrayList<LocalRecording>();
    var currentScript = replayerStatus.getCurrentScript();
    sessionFactory.query((em -> {
      List< DbRecording> allRecs= em.createQuery("SELECT e FROM DbRecording e").getResultList();
      for(var rs: allRecs){
        var lr = new LocalRecording();
        lr.setId(rs.getId());
        lr.setState(ReplayerState.NONE);
        lr.setName(rs.getName());
        if (rs.getId()==currentScript) {
          lr.setState(replayerStatus.getStatus());
        }
        listOfItems.add(lr);
      }
    }));
    res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
    res.setResponseText(mapper.writeValueAsString(listOfItems));
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}",
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
    var isEmpty = true;
    sessionFactory.query(em-> {
      var rslist = em.createQuery("SELECT e FROM DbRecording e WHERE e.id=" + id).getResultList();
      if(rslist.size()==0){
        result.setLines(null);
        return;
      }
      DbRecording recording = (DbRecording)rslist.get(0);
      if(recording.getFilter()!=null && !recording.getFilter().isEmpty()){
        result.setFilter(mapper.readValue(recording.getFilter(),typeRef));
      }else{
        result.setFilter(new HashMap<>());
      }
      List<CallIndex> indexLines = em
              .createQuery("SELECT e FROM CallIndex e WHERE e.recordingId=" + id)
              .getResultList();
      HashMap<Long, ReplayerRow> rows = new HashMap<>();
      em
              .createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId=" + id)
              .getResultList()
              .stream()
              .forEach((a)->{
                var idr = ((ReplayerRow)a).getId();
                if(!rows.containsKey(idr)) {
                  rows.put(idr, (ReplayerRow) a);
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
        newLine.setStimulatorTest(index.isStimulatorTest());
        newLine.setType(line.getType());
        newLine.setQueryCalc(RequestUtils.buildFullQuery(line.getRequest()));
        newLine.setPreScript(index.getPreScript()!=null);
        newLine.setScript(index.getPostScript()!=null);
        newLine.setRequestHashCalc(isHashPresent(line.getRequestHash()));
        newLine.setResponseHashCalc(isHashPresent(line.getResponseHash()));
        newLine.setResponseStatusCode(line.getResponse().getStatusCode());
        result.getLines().add(newLine);
      }

    });

    if(result.getLines()==null){
      res.setStatusCode(404);
      res.setResponseText("MISSING RECORDING WITH ID "+id);
    }else {
      result.getLines().sort(Comparator.comparingLong(SingleScriptLine::getId));
      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
      res.setResponseText(mapper.writeValueAsString(result));
    }
  }

  private boolean isHashPresent(String hash) {
    return hash!=null && !hash.isEmpty() && !hash.equalsIgnoreCase("0");
  }


  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "DELETE")
  @HamDoc(description = "Delete a recording",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id")
  )
  public void deleteRecordin(Request req, Response res) throws Exception {
    var id = Long.parseLong(req.getPathParameter("id"));
    sessionFactory.transactional(em->{
      em.createQuery("DELETE From DbRecording WHERE id="+id).executeUpdate();
      em.createQuery("DELETE From ReplayerRow WHERE recordingId="+id).executeUpdate();
      em.createQuery("DELETE From CallIndex WHERE recordingId="+id).executeUpdate();
    });

    res.setStatusCode(200);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording/{id}",
      method = "PUT")
  @HamDoc(description = "Modify an exesting recording",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          requests = @HamRequest(body = ReplayerResult.class)
  )
  public void updateRecord(Request req, Response res) throws Exception {
    var id = Long.parseLong(req.getPathParameter("id"));
    var scriptData = mapper.readValue(req.getRequestText(), ScriptData.class);
    sessionFactory.transactional(em->{
      DbRecording recording = (DbRecording)em.createQuery("SELECT e FROM DbRecording e WHERE e.id="+id).getResultList().get(0);
      List<CallIndex> indexLines = em.createQuery("SELECT e FROM CallIndex e WHERE e.recordingId="+id).getResultList();
      List<ReplayerRow> rows = em.createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId="+id).getResultList();

      recording.setDescription(scriptData.getDescription());
      recording.setName(scriptData.getName());
      recording.setFilter(mapper.writeValueAsString(scriptData.getFilter()));
      em.merge(recording);
      for(var ci:indexLines){
        ci.setStimulatorTest(scriptData.getStimulatorTest().stream().anyMatch(a->a.intValue()==ci.getId()));
        em.merge(ci);
      }
    });

    res.setStatusCode(200);
  }


  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/full",
          method = "GET")
  @HamDoc(description = "Alternative retrieval of recording",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          responses = @HamResponse(body = String.class)

  )
  public void getFull(Request req, Response res) throws Exception {
    var id = req.getPathParameter("id");
    var result = new ReplayerResult();


    sessionFactory.query(em-> {
      DbRecording recording = (DbRecording) em.createQuery("SELECT e FROM DbRecording e WHERE e.id=" + id).getResultList().get(0);
      List<CallIndex> indexLines = em.createQuery("SELECT e FROM CallIndex e WHERE e.recordingId=" + id).getResultList();
      List<ReplayerRow> rows = em.createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId=" + id).getResultList();

      result.setName(recording.getName());
      result.setDescription(recording.getDescription());
      for(var row:rows){
        if(row.isStaticRequest()){
          result.getStaticRequests().add(row);
        }else{
          result.getDynamicRequests().add(row);
        }
      }
      for(var indexLine:indexLines){
        result.getIndexes().add(indexLine);
      }
    });

    res.setResponseText(mapper.writeValueAsString(result));
    res.addHeader(ConstantsHeader.CONTENT_TYPE,ConstantsMime.JSON);
    res.addHeader("Content-Disposition", "attachment;"+id+".json");
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/replayer/recording",
      method = "POST")
  @HamDoc(description = "Create/update recording",tags = {"plugin/replayer"},
    requests = @HamRequest(body=JsonFileData.class))
  public void uploadRecording(Request req, Response res) throws Exception {
    JsonFileData jsonFileData = mapper.readValue(req.getRequestText(), JsonFileData.class);
    String realFileName = FilenameUtils.removeExtension(jsonFileData.getName());
    var replayerResult = mapper.readValue(jsonFileData.readAsString(),ReplayerResult.class);
    var recording = new DbRecording();
    recording.setDescription(replayerResult.getDescription());
    recording.setName(realFileName);
    sessionFactory.transactional(em->{
      em.persist(recording);
      for(var row:replayerResult.getDynamicRequests()){
        row.setIndex(null);
        row.setRecordingId(recording.getId());
        em.persist(row);
      }
      for(var row:replayerResult.getStaticRequests()){
        row.setIndex(null);
        row.setRecordingId(recording.getId());
        em.persist(row);
      }
      for(var row:replayerResult.getIndexes()){
        row.setIndex(null);
        row.setRecordingId(recording.getId());
        em.persist(row);
      }
    });

    logger.info("Uploaded replayer binary script ");
    res.setResponseText(String.valueOf(recording.getId()));
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/deletelines",
          method = "POST")
  @HamDoc(description = "Delete multiple lines of script",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          requests = @HamRequest(body = String[].class,
          examples = @Example(example = "[1,2,3]"))
  )
  public void deleteLines(Request req, Response res) throws Exception {
    List<Long> jsonFileData = Arrays.stream(mapper.readValue(req.getRequestText(), Long[].class)).collect(Collectors.toList());
    var id = Long.valueOf(req.getPathParameter("id"));
    sessionFactory.transactional(em->{
      for(var itemId:jsonFileData) {
        em.createQuery("DELETE FROM CallIndex WHERE reference="+itemId+" AND recordingId="+id).executeUpdate();
        em.createQuery("DELETE FROM ReplayerRow WHERE id="+itemId+" AND recordingId="+id).executeUpdate();
      }
    });

    res.setResponseText("{}");
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}/clone/{newid}",
          method = "POST")
  @HamDoc(description = "Clone the selected lines in a new request",tags = {"plugin/replayer"},
          path = {@PathParameter(key = "id"),@PathParameter(key = "newid")},
          requests = @HamRequest(body = String[].class,
          examples = @Example(example = "[1,2,3]"))
  )
  public void clone(Request req, Response res) throws Exception {
    Set<Long> jsonFileData = new HashSet<Long>(
            Arrays.stream(mapper.readValue(req.getRequestText(), Long[].class)).collect(Collectors.toList()));
    var id = Long.valueOf(req.getPathParameter("id"));
    AtomicLong recordingId = new AtomicLong();
    sessionFactory.query(em-> {
      DbRecording recording = (DbRecording) em.createQuery("SELECT e FROM DbRecording e WHERE e.id=" + id).getResultList().get(0);
      recordingId.set(recording.getId());
      List<CallIndex> indexLines = em.createQuery("SELECT e FROM CallIndex e WHERE e.recordingId=" + id).getResultList();
      List<ReplayerRow> rows = em.createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId=" + id).getResultList();

      em.detach(recording);
      recording.setId(null);
      em.persist(recording);
      Set<Long> references = new HashSet<>();
      for(var indexLine:indexLines){

        if(!jsonFileData.contains(indexLine.getId()))continue;
        references.add(indexLine.getReference());
        em.detach(indexLine);
        indexLine.setId(null);
        indexLine.setRecordingId(recording.getId());
        em.persist(indexLine);
      }
      for(var row:rows){
        if(!references.contains(row.getId()))continue;
        em.detach(row);
        row.setIndex(null);
        row.setRecordingId(recording.getId());
        em.persist(row);
      }

    });

    res.setResponseText(String.valueOf(recordingId.get()));
    res.setStatusCode(200);
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/replayer/recording/{id}",
          method = "POST")
  @HamDoc(description = "Search on recording req/res bodies for specific strings",tags = {"plugin/replayer"},
          path = @PathParameter(key = "id"),
          requests = @HamRequest(body = String[].class)
  )
  public void searchRecords(Request req, Response res) throws Exception {
    var id = Long.parseLong(req.getPathParameter("id"));
    var queryParams =  Arrays.stream(mapper.readValue(req.getRequestText(), String[].class)).collect(Collectors.toList());
    var result = new SingleScript();
    var isEmpty = true;
    sessionFactory.query(em-> {
      var rslist = em.createQuery("SELECT e FROM DbRecording e WHERE e.id=" + id).getResultList();
      if(rslist.size()==0){
        result.setLines(null);
        return;
      }
      DbRecording recording = (DbRecording)rslist.get(0);
      if(recording.getFilter()!=null && !recording.getFilter().isEmpty()){
        result.setFilter(mapper.readValue(recording.getFilter(),typeRef));
      }else{
        result.setFilter(new HashMap<>());
      }
      List<CallIndex> indexLines = em
              .createQuery("SELECT e FROM CallIndex e WHERE e.recordingId=" + id)
              .getResultList();
      HashMap<Long, ReplayerRow> rows = new HashMap<>();
      em
              .createQuery("SELECT e FROM ReplayerRow e WHERE e.recordingId=" + id)
              .getResultList()
              .stream()
              .forEach((a)->{
                var idr = ((ReplayerRow)a).getId();
                if(!rows.containsKey(idr)) {
                  var add = false;
                  if (queryParams.size() == 0) {
                    add = true;
                  } else {
                    var rs = ((ReplayerRow) a).getResponse();
                    var rq = ((ReplayerRow) a).getRequest();
                    if (rq.getRequestText()!=null){
                        if(queryParams.stream().anyMatch((v) -> rq.getRequestText().contains(v))){
                          add =true;
                        }
                    }
                    if (add == false && rs.getResponseText()!=null){
                      if(queryParams.stream().anyMatch((v) -> rs.getResponseText().contains(v))){
                        add =true;
                      }
                    }
                  }
                  if(add){
                    rows.put(idr, (ReplayerRow) a);
                  }
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
        newLine.setStimulatorTest(index.isStimulatorTest());
        newLine.setType(line.getType());
        newLine.setQueryCalc(RequestUtils.buildFullQuery(line.getRequest()));
        newLine.setPreScript(index.getPreScript()!=null);
        newLine.setScript(index.getPostScript()!=null);
        newLine.setRequestHashCalc(isHashPresent(line.getRequestHash()));
        newLine.setResponseHashCalc(isHashPresent(line.getResponseHash()));
        newLine.setResponseStatusCode(line.getResponse().getStatusCode());
        result.getLines().add(newLine);
      }

    });

    if(result.getLines()==null){
      res.setStatusCode(404);
      res.setResponseText("MISSING RECORDING WITH ID "+id);
    }else {
      result.getLines().sort(Comparator.comparingLong(SingleScriptLine::getId));
      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
      res.setResponseText(mapper.writeValueAsString(result));
    }
  }
}
