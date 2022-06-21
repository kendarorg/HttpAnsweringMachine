package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.SwaggerLoader;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.*;
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


import java.io.IOException;
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

  @HttpMethodFilter(phase = HttpFilterType.API,
          pathAddress = "/api/swagger/map.json",
          method = "GET",id="GET /api/swagger/map.json")
  @HamDoc(
          responses = {@HamResponse(body = OpenAPI.class)},
          requests = {@HamRequest()})
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
        var apiResponses = new ApiResponses();

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

        // Setup the models for the response
        if(doc.responses()!=null) {
          for (var res : doc.responses()) {
            var hasBOdy =extractSchemasForMethod(schemas, res.body());
            ApiResponse expectedResponse = new ApiResponse()
                    .description(res.description());
            if(hasBOdy){

              expectedResponse.$ref("#/components/schemas/"+res.body().getSimpleName());
            }
            apiResponses.addApiResponse(res.code() + "", expectedResponse);
          }
        }

        // Setup the models for the requests
        if(doc.requests()!=null) {
          for (var res : doc.requests()) {
            var hasBody = extractSchemasForMethod(schemas, res.body());

            var operation = new Operation();
            if (hasBody) {
              var content = new Content()
                      .addMediaType(res.accept(),
                              new MediaType().schema(new Schema().$ref(res.body().getSimpleName())));

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
    var request =  ModelConverters.getInstance().readAll(bodyRequest);
    for(var req :request.entrySet()){
      schemas.put(req.getKey(),req.getValue());
    }
    return true;
  }


//
//  public void convertSpec() throws IOException {
//    final Model personModel = ModelConverters.getInstance().read(Person.class).get("Person");
//    final Model errorModel = ModelConverters.getInstance().read(Error.class).get("Error");
//    final Info info = new Info()
//            .version("1.0.0")
//            .title("Swagger Petstore");
//
//    final Contact contact = new Contact()
//            .name("Swagger API Team")
//            .email("foo@bar.baz")
//            .url("http://swagger.io");
//
//    info.setContact(contact);
//
//    final Map<String, Object> map = new HashMap<String, Object>();
//    map.put("name", "value");
//    info.setVendorExtension("x-test2", map);
//    info.setVendorExtension("x-test", "value");
//
//    final Swagger swagger = new Swagger()
//            .info(info)
//            .host("petstore.swagger.io")
//            .securityDefinition("api-key", new ApiKeyAuthDefinition("key", SecurityScheme.In.HEADER))
//            .scheme(Scheme.HTTP)
//            .consumes("application/json")
//            .produces("application/json")
//            .model("Person", personModel)
//            .model("Error", errorModel);
//
//    final Operation get = new Operation()
//            .produces("application/json")
//            .summary("finds pets in the system")
//            .description("a longer description")
//            .tag("Pet Operations")
//            .operationId("get pet by id")
//            .deprecated(true);
//
//    get.parameter(new QueryParameter()
//            .name("tags")
//            .description("tags to filter by")
//            .required(false)
//            .property(new StringProperty())
//    );
//
//    get.parameter(new PathParameter()
//            .name("petId")
//            .description("pet to fetch")
//            .property(new LongProperty())
//    );
//
//    final Response response = new Response()
//            .description("pets returned")
//            .schema(new RefProperty().asDefault("Person"))
//            .example("application/json", "fun!");
//
//    final Response errorResponse = new Response()
//            .description("error response")
//            .schema(new RefProperty().asDefault("Error"));
//
//    final Response fileType = new Response()
//            .description("pets returned")
//            .schema(new FileProperty())
//            .example("application/json", "fun!");
//
//    get.response(200, response)
//            .defaultResponse(errorResponse);
//
//    get.response(400, fileType)
//            .defaultResponse(errorResponse);
//
//    final Operation post = new Operation()
//            .summary("adds a new pet")
//            .description("you can add a new pet this way")
//            .tag("Pet Operations")
//            .operationId("add pet")
//            .defaultResponse(errorResponse)
//            .parameter(new BodyParameter()
//                    .description("the pet to add")
//                    .schema(new RefModel().asDefault("Person")));
//
//    swagger.path("/pets", new Path().get(get).post(post));
//    final String swaggerJson = Json.mapper().writeValueAsString(swagger);
//    final Swagger rebuilt = Json.mapper().readValue(swaggerJson, Swagger.class);
//    System.out.println(Json.pretty(rebuilt));
//    SerializationMatchers.assertEqualsToJson(rebuilt, swaggerJson);
//  }

  public void doStuff(){
    OpenAPI swagger = new OpenAPI()
            .addServersItem(new Server().url("http://petstore.swagger.io"));
    //PathItem expectedPath = new PathItem().$ref("http://my.company.com/paths/health.json");
    //swagger.path("/health", expectedPath);

    ApiResponse expectedResponse = new ApiResponse().$ref("http://my.company.com/paths/{pp}");
    Content content = new Content()
            .addMediaType("application/json", new MediaType()
                    .schema(new ObjectSchema()
                            .example(new Object())));//TODO Here goes object SCHEAM

    RequestBody requestBody = new RequestBody().content(content);

    List<Parameter> parameters = new ArrayList<>();
    parameters.add(new QueryParameter()
            .name("qp").example("QUERYEXAMPLE"));
    parameters.add(new PathParameter()
            .name("pp").example("PATHEXAMPLE"));
    PathItem expectedPath = new PathItem()
            .get(
                    new Operation()
                            .responses(new ApiResponses()
                                    .addApiResponse("200", expectedResponse))
                            .requestBody(requestBody)
                            .parameters(parameters)
            );

    swagger.path("/health", expectedPath);
  }

  @Override
  public String getId() {
    return this.getClass().getName();
  }
}
