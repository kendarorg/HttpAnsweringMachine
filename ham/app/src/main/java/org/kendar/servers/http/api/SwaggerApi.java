package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.Primitives;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.kendar.http.FilterConfig;
import org.kendar.http.FilterDescriptor;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.SwaggerEnricher;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.HamSecurity;
import org.kendar.http.annotations.multi.Header;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class SwaggerApi  implements FilteringClass {
  private final String localAddress;
  private final List<SwaggerEnricher> enrichers;
  private FilterConfig filtersConfiguration;
  private JsonConfiguration jsonConfiguration;

  public SwaggerApi(FilterConfig filtersConfiguration, JsonConfiguration jsonConfiguration,
                    List<SwaggerEnricher> enrichers){

    this.filtersConfiguration = filtersConfiguration;
    this.jsonConfiguration = jsonConfiguration;
    this.localAddress = jsonConfiguration.getValue("global.localAddress");
    this.enrichers = enrichers;
  }

  class mt{
    public String content;
    public MediaType mediaType;
    public String description;
    public Map<String, Header> headers = new HashMap<>();
  }

  @HttpMethodFilter(phase = HttpFilterType.API,
          pathAddress = "/api/swagger/map.json",
          method = "GET",id="GET /api/swagger/map.json")
  @HamDoc(
          description = "Retrieve the swagger api",
          responses = @HamResponse(
                  body = String.class,
                  description = "The json for the swagger api"
          ),
          tags = {"base/utils"})
  public void loadSwagger(Request reqp, Response resp) throws JsonProcessingException {
    var config = filtersConfiguration.get();

    OpenAPI swagger = new OpenAPI()
            .addServersItem(new Server().url("http://"+localAddress));
    swagger.setInfo(new Info()
            .title("Local API")
            .version("1.0.0"));
    Map<String, Schema> schemas = new HashMap<>();
    Map<String, PathItem> expectedPaths = new HashMap<>();
    for (var kvp : config.filters.entrySet()) {
      if(kvp.getKey() != HttpFilterType.API ) continue;
      for(var filter : kvp.getValue()) {
        handleSingleFilter(swagger, schemas, expectedPaths, filter);
      }
    }

    for(var enricher:enrichers){
      enricher.enrich(swagger);
    }
    publishResponse(resp, swagger, schemas);
  }

  private void handleSingleFilter(OpenAPI swagger, Map<String, Schema> schemas, Map<String, PathItem> expectedPaths, FilterDescriptor filter) {
    HamDoc doc = filter.getHamDoc();
    if(doc==null) return;

    if(!expectedPaths.containsKey(filter.getMethodFilter().pathAddress())){
      expectedPaths.put(filter.getMethodFilter().pathAddress(),new PathItem());
    }
    var expectedPath = expectedPaths.get(filter.getMethodFilter().pathAddress());

    if(doc.todo()){
      setupTodoApi(swagger, filter, doc, expectedPath);
    }else {
      setupRealApi(swagger, schemas, filter, doc, expectedPath);
    }
  }

  private void publishResponse(Response resp, OpenAPI swagger, Map<String, Schema> schemas) {
    try {
      var components = swagger.getComponents();
      if(components==null) {
        components = new Components();
      }
      var scc = components;
      schemas.entrySet().stream().forEach(es->
              scc.addSchemas(es.getKey(),es.getValue())
      );
      swagger.components(components);
      String swaggerJson = Json.mapper().writeValueAsString(swagger);
      OpenAPI rebuilt = Json.mapper().readValue(swaggerJson, OpenAPI.class);
      resp.setResponseText( Json.mapper().writeValueAsString(rebuilt));
      resp.addHeader("content-type","application/json");

    }catch (Exception ex){

    }
  }

  private void setupTodoApi(OpenAPI swagger, FilterDescriptor filter, HamDoc doc, PathItem expectedPath) {
    var meth = filter.getMethod();
    var operation = new Operation();
    operation.description("TODO");
    var parameters= new ArrayList<Parameter>();
    if(doc.path()!=null) {
      for (var res : doc.path()) {
        parameters.add(new PathParameter()
                .name(res.key())
                .schema(new Schema()
                        .type(res.type())
                        .example(res.example())));
      }
    }
    //TODO Security
    operation.parameters(parameters);
    if(doc.tags()!=null && doc.tags().length>0) {
      operation.tags(Arrays.asList(doc.tags()));
    }
    setupMethod(expectedPath, operation, meth);
    swagger.path(filter.getMethodFilter().pathAddress(), expectedPath);


    swagger
            .path("/health/{pp}", expectedPath);
  }

  private void setupRealApi(OpenAPI swagger, Map<String, Schema> schemas, FilterDescriptor filter, HamDoc doc, PathItem expectedPath) {
    List<Parameter> parameters = new ArrayList<>();

    prepareQuery(doc, parameters);
    prpearePath(doc, parameters);
    prepareHeaders(doc, parameters);

    var apiResponses = new ApiResponses();
    // Setup the models for the response
    if(doc.responses()==null || doc.responses().length==0) {
      buildEmptyResponse(apiResponses);
    }
    else{
      var responses = new HashMap<Integer,List<mt>>();
      for (var res : doc.responses()) {
        prepareResponse(schemas, responses, res);
      }
      for(var singres:responses.entrySet()){
        buildResponse(apiResponses, singres);
      }
    }

    // Setup the models for the requests
    if(doc.requests()!=null && doc.requests().length>0) {
      for (var res : doc.requests()) {
        buildRequest(swagger, schemas, filter, doc, expectedPath, parameters, apiResponses, res);
      }
    }else{
      buildEmptyRequest(swagger, schemas, filter, doc, expectedPath, parameters, apiResponses);
    }
  }

  private void buildResponse(ApiResponses apiResponses, Map.Entry<Integer, List<mt>> singres) {
    var toAddResponse = new ApiResponse();
    var content = new Content();
    for(var singresitem: singres.getValue()){
      content.addMediaType(singresitem.content,singresitem.mediaType);

      for(var hea:singresitem.headers.values()){
        toAddResponse.addHeaderObject(
                hea.key(),
                new io.swagger.v3.oas.models.headers.Header()
                        .schema(getSchemaHam(String.class))
                        .description(hea.description())
                        .example(hea.value())
        );
      }
    }
    toAddResponse.setContent(content);
    toAddResponse.setDescription(singres.getKey()+"");
    apiResponses.addApiResponse(singres.getKey()+"",toAddResponse);
  }

  private void prepareResponse(Map<String, Schema> schemas, HashMap<Integer, List<mt>> responses, HamResponse res) {
    var hasBody =extractSchemasForMethod(schemas, res.body());

    if(hasBody){
      var mmt = new mt();
      if(res.headers()!=null && res.headers().length>0){
        for(var hea : res.headers()){
          mmt.headers.put(hea.key(),hea);
        }
      }
      var schema = getSchemaHam(res.body());
      var mediaType  = new MediaType().schema(schema);
      if(res.examples()!=null){
        for(var ex : res.examples()){
          mediaType.addExamples(ex.description(),new Example().value(ex.example()));
        }
      }
      if(!responses.containsKey(res.code())){
        responses.put(res.code(),new ArrayList<>());
      }
      mmt.description = res.description();
      mmt.content = res.content();
      mmt.mediaType = mediaType;
      responses.get(res.code()).add(mmt);
    }
  }

  private void buildRequest(OpenAPI swagger, Map<String, Schema> schemas, FilterDescriptor filter, HamDoc doc, PathItem expectedPath, List<Parameter> parameters, ApiResponses apiResponses, HamRequest res) {
    var resBody = res.body();
    var resExamples = res.examples();
    var resAccept = res.accept();

    setupRequest(swagger, schemas, filter, doc, expectedPath, parameters, apiResponses, resBody, resExamples, resAccept);
  }

  private void buildEmptyRequest(OpenAPI swagger, Map<String, Schema> schemas, FilterDescriptor filter, HamDoc doc, PathItem expectedPath, List<Parameter> parameters, ApiResponses apiResponses) {
    Class<?> resBody = Object.class;
    org.kendar.http.annotations.multi.Example[] resExamples = null;
    String resAccept = null;

    setupRequest(swagger, schemas, filter, doc, expectedPath, parameters, apiResponses, resBody, resExamples, resAccept);
  }

  private void buildEmptyResponse(ApiResponses apiResponses) {
    var toAddResponse = new ApiResponse();
    toAddResponse.setDescription("200");
    apiResponses.addApiResponse("200",toAddResponse);
  }

  private void prepareQuery(HamDoc doc, List<Parameter> parameters) {
    // Setup query strings
    if(doc.query()!=null) {
      for (var res : doc.query()) {
        parameters.add(new QueryParameter()
                .name(res.key())
                .schema(new Schema()
                        .type(res.type())
                        .example(res.example())));
      }
    }
  }

  private void prpearePath(HamDoc doc, List<Parameter> parameters) {
    // Setup path variables
    if(doc.path()!=null) {
      for (var res : doc.path()) {
        parameters.add(new PathParameter()
                .name(res.key())
                .schema(new Schema()
                        .type(res.type())
                        .example(res.example())));
      }
    }
  }

  private void prepareHeaders(HamDoc doc, List<Parameter> parameters) {
    // Setup header variables
    if(doc.header()!=null) {
      for (var res : doc.header()) {
        parameters.add(new HeaderParameter()
                .name(res.key())
                        .description(res.key())
                .schema(new Schema()
                        .type("string")
                        .example(res.value())));
      }
    }
  }

  private void setupRequest(OpenAPI swagger, Map<String, Schema> schemas, FilterDescriptor filter, HamDoc doc, PathItem expectedPath, List<Parameter> parameters, ApiResponses apiResponses, Class<?> resBody, org.kendar.http.annotations.multi.Example[] resExamples, String resAccept) {
    var hasBody = extractSchemasForMethod(schemas, resBody);

    var operation = new Operation();
    if (hasBody) {
      setupRequestBody(resBody, resExamples, resAccept, operation);
    }
    operation.description(doc.description());
    operation.responses(apiResponses);
    operation.parameters(parameters);
    var meth = filter.getMethod();
    if(doc.tags()!=null && doc.tags().length>0) {
      operation.tags(Arrays.asList(doc.tags()));
    }
    if(doc.security()!=null && doc.security().length>0){
      for(var sec:doc.security()){
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.put(sec.name(), Arrays.stream(sec.scopes()).collect(Collectors.toList()));
        operation.addSecurityItem(securityRequirement);
      }
    }
    setupMethod(expectedPath, operation, meth);

    swagger.path(filter.getMethodFilter().pathAddress(), expectedPath);
  }

  private void setupRequestBody(Class<?> resBody, org.kendar.http.annotations.multi.Example[] resExamples, String resAccept, Operation operation) {
    var schema = getSchemaHam(resBody);
    var mediaType  = new MediaType().schema(schema);
    if(resExamples !=null){
      for(var ex : resExamples){
        mediaType.addExamples(ex.description(),new Example().value(ex.example()));
      }
    }
    var content = new Content()
            .addMediaType(resAccept,
                    mediaType);

    RequestBody requestBody = new RequestBody().content(content);
    operation.requestBody(requestBody);
  }

  private void setupMethod(PathItem expectedPath, Operation operation, String meth) {
    if (meth.equalsIgnoreCase("GET")) {
      expectedPath.get(operation);
    } else if (meth.equalsIgnoreCase("POST")) {
      expectedPath.post(operation);
    } else if (meth.equalsIgnoreCase("PUT")) {
      expectedPath.put(operation);
    } else if (meth.equalsIgnoreCase("OPTIONS")) {
      expectedPath.options(operation);
    } else if (meth.equalsIgnoreCase("DELETE")) {
      expectedPath.delete(operation);
    }
  }

  private boolean extractSchemasForMethod(Map<String, Schema> schemas, Class<?> bodyRequest) {
    if(bodyRequest == Object.class) return false;
    if(Primitives.isWrapperType(bodyRequest))return true;
    if(bodyRequest.isPrimitive())return true;
    if(bodyRequest.isArray()){
      return extractSchemasForMethod(schemas,bodyRequest.getComponentType());
    }
    if(Collection.class.isAssignableFrom(bodyRequest)) return true;
    var request =  ModelConverters.getInstance().readAll(bodyRequest);
    for(var req :request.entrySet()){
      schemas.put(req.getKey(),req.getValue());
    }
    return true;
  }

  private Schema getSchemaHam(Class<?> bodyRequest) {
    if(bodyRequest == byte[].class){
      //FIXME
      return new Schema().type("string").format("byte");

    }
    if(Primitives.isWrapperType(bodyRequest)){
      return new Schema().type(Primitives.unwrap(bodyRequest).getSimpleName().toLowerCase(Locale.ROOT));
    }
    if(bodyRequest.isPrimitive()){
      return new Schema().type(bodyRequest.getSimpleName().toLowerCase(Locale.ROOT));
    }
    if(bodyRequest == String.class){
      //FIXME
      return new Schema().type(bodyRequest.getSimpleName().toLowerCase(Locale.ROOT));
    }
    if(bodyRequest.isArray()){
      return new Schema()
              .type("array")
              .items(getSchemaHam(bodyRequest.getComponentType()));
    }
    if(List.class.isAssignableFrom(bodyRequest)){
      return new Schema()
              .type("array")
              .items(getSchemaHam(bodyRequest.getComponentType()));
    }
    if(Collection.class.isAssignableFrom(bodyRequest)){
      return new Schema()
              .type("array")
              .items(getSchemaHam(bodyRequest.getComponentType()));
    }

    return  new Schema().$ref(bodyRequest.getSimpleName());
  }

  @Override
  public String getId() {
    return this.getClass().getName();
  }
}
