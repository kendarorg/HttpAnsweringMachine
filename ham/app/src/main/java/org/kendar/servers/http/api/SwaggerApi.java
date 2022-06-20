package org.kendar.servers.http.api;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.jaxrs2.integration.SwaggerLoader;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.kendar.http.FilterConfig;
import org.kendar.servers.JsonConfiguration;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwaggerApi {
  private FilterConfig filtersConfiguration;
  private OpenApiReader openApiReader;
  private JsonConfiguration jsonConfiguration;

  public SwaggerApi(FilterConfig filtersConfiguration, OpenApiReader openApiReader, JsonConfiguration jsonConfiguration){

    this.filtersConfiguration = filtersConfiguration;
    this.openApiReader = openApiReader;
    this.jsonConfiguration = jsonConfiguration;
  }
  public void loadSwagger(){
    var config = filtersConfiguration.get();
    var result = new ArrayList<String>();
    var swloader = new SwaggerLoader();

    for (var kvp : config.filtersByClass.entrySet()) {
      var instance = kvp.getValue().get(0);
      //swloader.(instance.getClass());
    }
    //var openapi = swloader.getOpenAPI();
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
}
