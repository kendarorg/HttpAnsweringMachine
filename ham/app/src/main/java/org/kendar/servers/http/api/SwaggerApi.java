package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.Primitives;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.SwaggerLoader;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.kendar.http.FilterConfig;
import org.kendar.http.FilterDescriptor;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class SwaggerApi  implements FilteringClass {
  private FilterConfig filtersConfiguration;
  private JsonConfiguration jsonConfiguration;

  public SwaggerApi(FilterConfig filtersConfiguration, JsonConfiguration jsonConfiguration){

    this.filtersConfiguration = filtersConfiguration;
    this.jsonConfiguration = jsonConfiguration;
  }

  class mt{
    public String content;
    public MediaType mediaType;
    public String description;
  }

  @HttpMethodFilter(phase = HttpFilterType.API,
          pathAddress = "/api/swagger/map.json",
          method = "GET",id="GET /api/swagger/map.json")
//  @HamDoc(
//          description = "Retrieve the OpenAPI data",
//          responses = {@HamResponse(body = OpenAPI.class)},
//          requests = {@HamRequest()})
  public void loadSwagger(Request reqp, Response resp) throws JsonProcessingException {
    var config = filtersConfiguration.get();

    OpenAPI swagger = new OpenAPI()
            .addServersItem(new Server().url("http://www.local.test"));
    swagger.setInfo(new Info()
            .title("Local API")
            .version("1.0.0"));
    Map<String, Schema> schemas = new HashMap<>();
    for (var kvp : config.filters.entrySet()) {
      if(kvp.getKey() != HttpFilterType.API ) continue;
      for(var filter : kvp.getValue()) {
        HamDoc doc = filter.getHamDoc();
        if(doc==null)continue;

        var expectedPath = new PathItem();

        List<Parameter> parameters = new ArrayList<>();

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
        // Setup header variables
        if(doc.header()!=null) {
          for (var res : doc.header()) {
            parameters.add(new HeaderParameter()
                    .name(res.key())
                    .schema(new Schema()
                            .type("string")
                            .example(res.value())));
          }
        }

        var apiResponses = new ApiResponses();
        // Setup the models for the response
        if(doc.responses()==null || doc.responses().length==0) {
          var toAddResponse = new ApiResponse();
          toAddResponse.setDescription("200");
          apiResponses.addApiResponse("200",toAddResponse);
        }
        else{
          var responses = new HashMap<Integer,List<mt>>();
          for (var res : doc.responses()) {
            var hasBody =extractSchemasForMethod(schemas, res.body());
            var expectedResponse = new mt();
            //if(res.description()!=null && !res.description().isEmpty()) {
              expectedResponse.description = res.description();
            //}
            if(hasBody){


              var schema = getSchemaHam(res.body());
              var mediaType  = new MediaType().schema(schema);
              if(res.examples()!=null){
                for(var ex :res.examples()){
                  mediaType.addExamples(ex.description(),new Example().value(ex.example()));
                }
              }
              if(!responses.containsKey(res.code())){
                responses.put(res.code(),new ArrayList<>());
              }
              var mmt = new mt();
              mmt.description = res.description();
              mmt.content = res.content();
              mmt.mediaType = mediaType;
              responses.get(res.code()).add(mmt);
             /* var content = new Content()
                      .addMediaType(res.content(),
                              mediaType);
              //}
              expectedResponse.setContent(content);*/
            }
          }
          for(var singres:responses.entrySet()){
            var toAddResponse = new ApiResponse();
            var content = new Content();
            for(var singresitem:singres.getValue()){
              content.addMediaType(singresitem.content,singresitem.mediaType);

            }
            toAddResponse.setContent(content);
            toAddResponse.setDescription(singres.getKey()+"");
            apiResponses.addApiResponse(singres.getKey()+"",toAddResponse);
          }
        }

        // Setup the models for the requests
        if(doc.requests()!=null) {
          for (var res : doc.requests()) {
            var hasBody = extractSchemasForMethod(schemas, res.body());

            var operation = new Operation();
            if (hasBody) {
              var schema = getSchemaHam(res.body());
              var mediaType  = new MediaType().schema(schema);
              if(res.examples()!=null){
                for(var ex :res.examples()){
                  mediaType.addExamples(ex.description(),new Example().value(ex.example()));
                }
              }
              var content = new Content()
                      .addMediaType(res.accept(),
                              mediaType);

              RequestBody requestBody = new RequestBody().content(content);
              operation.requestBody(requestBody);
            }
            operation.description(doc.description());
            operation.responses(apiResponses);
            operation.parameters(parameters);
            var meth = filter.getMethod();
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

            swagger.path(filter.getMethodFilter().pathAddress(), expectedPath);
          }
        }

      }
      //var instance = kvp.getValue().get(0);
      //swloader.(instance.getClass());
    }

    try {
      var components = new Components();
      schemas.entrySet().stream().forEach(es->
              components.addSchemas(es.getKey(),es.getValue())
      );
      swagger.components(components);
      String swaggerJson = Json.mapper().writeValueAsString(swagger);
      OpenAPI rebuilt = Json.mapper().readValue(swaggerJson, OpenAPI.class);
      resp.setResponseText( Json.mapper().writeValueAsString(rebuilt));
      resp.addHeader("content-type","application/json");

    }catch (Exception ex){

    }
    //var openapi = swloader.getOpenAPI();
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
