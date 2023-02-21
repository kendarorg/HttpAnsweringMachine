package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.http.events.ScriptsModified;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.JsFilterConfig;
import org.kendar.servers.http.api.model.RestFilter;
import org.kendar.servers.http.api.model.RestFilterList;
import org.kendar.servers.http.api.model.RestFilterRequire;
import org.kendar.servers.http.storage.DbFilter;
import org.kendar.servers.http.storage.DbFilterRequire;
import org.kendar.servers.http.types.http.JsHttpFilterDescriptor;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.models.JsonFileData;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class JsFilterAPI implements FilteringClass {
  private final JsonConfiguration configuration;
  private final Logger logger;
  private final FileResourcesUtils fileResourcesUtils;
  private final EventQueue eventQueue;
  private HibernateSessionFactory sessionFactory;
  final ObjectMapper mapper = new ObjectMapper();

  public JsFilterAPI(JsonConfiguration configuration,
                     FileResourcesUtils fileResourcesUtils,
                     LoggerBuilder loggerBuilder,
                     EventQueue eventQueue,
                     HibernateSessionFactory sessionFactory) {

    this.logger = loggerBuilder.build(JsFilterAPI.class);
    this.configuration = configuration;
    this.fileResourcesUtils = fileResourcesUtils;
    this.eventQueue = eventQueue;
    this.sessionFactory = sessionFactory;
  }

  @Override
  public String getId() {
    return this.getClass().getName();
  }

  TypeReference<HashMap<String, String>> typeRef
          = new TypeReference<HashMap<String, String>>() {};

  @HttpMethodFilter(
      phase = HttpFilterType.API,
      pathAddress = "/api/plugins/jsfilter/filters",
      method = "GET")
  @HamDoc(tags = {"plugin/js"},
          description = "List all js filters",
          responses = @HamResponse(
                  body = RestFilterList[].class
          ))
  public void getJsFiltersList(Request req, Response res) throws Exception {
    List<DbFilter> rs = sessionFactory.queryResult(em->
            em.createQuery("SELECT e FROM DbFilter e ORDER BY e.id ASC").getResultList());
    var result = new ArrayList<RestFilterList>();
    for(var dbFilter:rs){
      var rf=new RestFilterList();
      rf.setId(dbFilter.getId());
      rf.setPhase(dbFilter.getPhase());
      rf.setPriority(dbFilter.getPriority());
      rf.setName(dbFilter.getName());
      result.add(rf);
    }
    res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
    res.setResponseText(mapper.writeValueAsString(result));
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}",
          method = "GET")
  @HamDoc(tags = {"plugin/js"},
          description = "Get Single filter",
          path = @PathParameter(key = "filtername"),
          query = @QueryString(key="full",example = "When set to true download the full filter descriptor"),
          responses = @HamResponse(
                  body = RestFilter.class
          ))
  public void getJsFilter(Request req, Response res) throws Exception {
    var jsFilterDescriptor = req.getPathParameter("filtername");
    var full =
            req.getQuery("full")==null?
                    false:
                    "true".equalsIgnoreCase(req.getQuery("full"));

    var dbFilter = (DbFilter)sessionFactory.querySingle(em->
            em.createQuery("SELECT e FROM DbFilter e WHERE e.id="+jsFilterDescriptor+" ORDER BY e.id ASC")).get();

    var rf=new RestFilter();
    rf.setId(dbFilter.getId());
    rf.setPhase(dbFilter.getPhase());
    rf.setPriority(dbFilter.getPriority());
    rf.setBlocking(dbFilter.isBlocking());
    rf.setName(dbFilter.getName());
    rf.setSource(dbFilter.getSource());
    rf.setType(dbFilter.getType());
    rf.setMatchers(mapper.readValue(dbFilter.getMatcher(),typeRef));
    rf.setRequire(new ArrayList<>());
    List<DbFilterRequire> rq = sessionFactory.queryResult(em->
            em.createQuery("SELECT e.name,e.binary FROM DbFilterRequire e WHERE e.scriptId="+dbFilter.getId()+" ORDER BY e.id ASC").getResultList());
    for(var rqf:rq){
      var dbf = new RestFilterRequire();
      dbf.setBinary(rqf.isBinary());
      dbf.setName(rqf.getName());
      if(full) {
        dbf.setContent(rqf.getContent());
      }
      rf.getRequire().add(dbf);
    }

    res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
    res.setResponseText(mapper.writeValueAsString(rf));
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}",
          method = "PUT")

  @HamDoc(tags = {"plugin/js"},
          description = "Update Single filter",
          path = @PathParameter(key = "filtername"),
          requests = @HamRequest(
              body =  RestFilter.class
          ))
  public void saveJsFilter(Request req, Response res) throws Exception {
    var jsFilterDescriptor = req.getPathParameter("filtername");

    Optional<DbFilter> dbFilterOp = sessionFactory.querySingle(em->
            em.createQuery("SELECT e FROM DbFilter e WHERE e.id="+jsFilterDescriptor+" ORDER BY e.id ASC"));

    RestFilter jsonFileData = mapper.readValue(req.getRequestText(), RestFilter.class);
    DbFilter dbFilter = new DbFilter();
    if(dbFilterOp.isPresent()){
      dbFilter = dbFilterOp.get();
    }else{
      jsonFileData.setMatchers(new HashMap<>());
    }


    dbFilter.setMatcher(mapper.writeValueAsString(jsonFileData.getMatchers()));
    dbFilter.setName(jsonFileData.getName());
    dbFilter.setPhase(jsonFileData.getPhase());
    dbFilter.setPriority(jsonFileData.getPriority());
    dbFilter.setSource(jsonFileData.getSource());
    dbFilter.setType(jsonFileData.getType());
    dbFilter.setBlocking(jsonFileData.isBlocking());

    var dbFinal = dbFilter;
    sessionFactory.transactional(em->{
      if(dbFilterOp.isPresent()) {
        em.merge(dbFinal);
      }else{
        em.persist(dbFinal);
      }
    });
    res.setResponseText(dbFinal.getId().toString());
    res.setStatusCode(200);
    eventQueue.handle(new ScriptsModified());
  }



  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}",
          method = "DELETE")
  @HamDoc(tags = {"plugin/js"},
          description = "Delete Single filter",
          path = @PathParameter(key = "filtername"))
  public void deleteJsFilter(Request req, Response res) throws Exception {
    var jsFilterDescriptor = req.getPathParameter("filtername");

    sessionFactory.transactional(em->{
      em.createQuery("DELETE FROM DbFilter e WHERE e.id="+jsFilterDescriptor).executeUpdate();
      em.createQuery("DELETE FROM DbFilterRequire e WHERE e.scriptId="+jsFilterDescriptor).executeUpdate();
    });
    res.setResponseText("OK");
    res.setStatusCode(200);
    eventQueue.handle(new ScriptsModified());
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters",
          method = "POST")
  @HamDoc(tags = {"plugin/js"},
          description = "Create Single filter",
          requests = @HamRequest(
                  body =  RestFilter.class
          ))
  public void uploadJsFilter(Request req, Response res) throws Exception {
    RestFilter jsonFileData = mapper.readValue(req.getRequestText(), RestFilter.class);

    var dbFilter = new DbFilter();
    dbFilter.setMatcher(mapper.writeValueAsString(jsonFileData.getMatchers()));
    dbFilter.setName(jsonFileData.getName());
    dbFilter.setPhase(jsonFileData.getPhase());
    dbFilter.setPriority(jsonFileData.getPriority());
    dbFilter.setSource(jsonFileData.getSource());
    dbFilter.setType(jsonFileData.getType());
    dbFilter.setBlocking(jsonFileData.isBlocking());

    sessionFactory.transactional(em->{
      em.persist(dbFilter);
    });
    res.setResponseText(dbFilter.getId().toString());
    res.setStatusCode(200);
    eventQueue.handle(new ScriptsModified());
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}/{file}",
          method = "GET")
  @HamDoc(tags = {"plugin/js"},
          description = "Retrieve the content of a filter associated file",
          path = {@PathParameter(key = "filtername"),
                  @PathParameter(key = "file")},
          responses = {@HamResponse(
                  body = String.class
          ),@HamResponse(
                  body = byte[].class
          )}
  )
  public void getJsFilterFile(Request req, Response res) throws Exception {
    var jsFilterDescriptor = req.getPathParameter("filtername");
    var fileId = req.getPathParameter("file");

    DbFilterRequire rq = (DbFilterRequire)sessionFactory.querySingle(em->
            em.createQuery("SELECT e.name FROM DbFilterRequire e WHERE " +
                    "e.scriptId="+jsFilterDescriptor+" AND e.name='"+fileId+"'")).get();

    if(rq.isBinary()) {
      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.STREAM);
      res.setResponseBytes(Base64.decodeBase64(rq.getContent()));
      res.setBinaryResponse(true);
    }else{
      res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
      res.setResponseText(rq.getContent());
    }
  }

  @HttpMethodFilter(
          phase = HttpFilterType.API,
          pathAddress = "/api/plugins/jsfilter/filters/{filtername}/{file}",
          method = "POST")

  @HamDoc(tags = {"plugin/js"},
          description = "Set the content of a filter associated file",
          path = {@PathParameter(key = "filtername"),
                  @PathParameter(key = "file")},
          requests = @HamRequest(
                  body = String.class
          ),
          query = @QueryString(key="binary",description = "True if binary file",type="boolean")
  )
  public void putJsFilterFile(Request req, Response res) throws Exception {
    //TODO: Required file upload
    var jsFilterDescriptor = req.getPathParameter("filtername");
    var fileId = req.getPathParameter("file");
    var binary = req.getQuery("binary")==null?false:Boolean.valueOf(req.getQuery("binary"));

    Optional<DbFilterRequire> rq = sessionFactory.querySingle(em->
            em.createQuery("SELECT e.name FROM DbFilterRequire e WHERE " +
                    "e.scriptId="+jsFilterDescriptor+" AND e.name='"+fileId+"'"));

  }
}
