package org.kendar.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsMime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://editor.swagger.io/
 * https://swagger.io/docs/specification/2-0/basic-structure/
 * https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations
 */
public class SwaggerTest {
    @Test
    void understand001() throws JsonProcessingException {

        final Map<String, Schema> schemas = ModelConverters.getInstance().read(Response.class);
        OpenAPI swagger = new OpenAPI()
                .addServersItem(new Server().url("http://petstore.swagger.io"));
        swagger.setInfo(new Info()
                .title("title")
                .version("2.0.0"));

        schemas.entrySet().stream().forEach(es->
                swagger.components(new Components().addSchemas(es.getKey(),es.getValue()))
        );



        //PathItem expectedPath = new PathItem().$ref("http://my.company.com/paths/health.json");
        //swagger.path("/health", expectedPath);



        /*var xxx = new Response();
        xxx.setStatusCode(200);
        xxx.setResponseText("BAH");
        xxx.addHeader("test","test");*/
        Content content = new Content()
                .addMediaType(ConstantsMime.JSON,
                        new MediaType()
                                .schema(new Schema()
                                        .$ref(Response.class.getSimpleName()))

                               /*.addExamples("0001",
                                        new Example().value(
                                                Json.mapper().writeValueAsString(xxx)
                                        ))*/);

        RequestBody requestBody = new RequestBody().content(content);



        List<Parameter> parameters = new ArrayList<>();



        parameters.add(new QueryParameter()
                .name("qp")
                .schema(new Schema()
                        .type("string"))
                .example("QUERYEXAMPLE"));
        parameters.add(new PathParameter()
                .name("pp")
                .schema(new Schema()
                        .type("string"))
                .example("QUERYEXAMPLE"));

        ApiResponse expectedResponse = new ApiResponse()
                .description("respo200");
        PathItem expectedPath = new PathItem()
                .post(  //Operation type
                        new Operation()
                                .responses(new ApiResponses()
                                        .addApiResponse("200", expectedResponse))
                                .description("desc000")
                                .requestBody(requestBody)
                                .parameters(parameters)
                );

        swagger
                .path("/health/{pp}", expectedPath);

        String swaggerJson = Json.mapper().writeValueAsString(swagger);
        OpenAPI rebuilt = Json.mapper().readValue(swaggerJson, OpenAPI.class);
        String result = Json.mapper().writeValueAsString(rebuilt);
        System.out.println(result);
    }

    @Test
    public void convertSpec() throws IOException, JsonProcessingException {
        final Schema personModel = ModelConverters.getInstance().read(Response.class).get("Response");
        //final Schema errorModel = ModelConverters.getInstance().read(Error.class).get("Error");
        final Info info = new Info()
                .version("1.0.0")
                .title("Swagger Petstore");

        final Contact contact = new Contact()
                .name("Swagger API Team")
                .email("foo@bar.baz")
                .url("http://swagger.io");

        info.setContact(contact);

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "value");
        info.addExtension("x-test2", map);
        info.addExtension("x-test", "value");

        final OpenAPI swagger = new OpenAPI()
                .info(info)
                .addServersItem(new Server()
                        .url("http://petstore.swagger.io"))

//                .securityDefinition("api-key", new ApiKeyAuthDefinition("key", In.HEADER))
//                .consumes(ConstantsMime.JSON)
//                .produces(ConstantsMime.JSON)
                .schema("Response", personModel);

        final Operation get = new Operation()
//                .produces(ConstantsMime.JSON)
                .summary("finds pets in the system")
                .description("a longer description")
                .addTagsItem("Pet Operations")
                .operationId("get pet by id")
                .deprecated(true);

        get.addParametersItem(new Parameter()
                .in("query")
                .name("tags")
                .description("tags to filter by")
                .required(false)
                .schema(new StringSchema())
        );

        get.addParametersItem(new Parameter()
                .in("path")
                .name("petId")
                .description("pet to fetch")
                .schema(new IntegerSchema().format("int64"))
        );

        final ApiResponse response = new ApiResponse()
                .description("pets returned")
                .content(new Content()
                        .addMediaType(ConstantsMime.JSON, new MediaType()
                                .schema(new Schema().$ref("Response"))
                                .example("fun")));

        final ApiResponse errorResponse = new ApiResponse()
                .description("error response")
                .link("myLink", new Link()
                        .description("a link")
                        .operationId("theLinkedOperationId")
                        .parameters("userId", "gah")
                )
                .content(new Content()
                        .addMediaType(ConstantsMime.JSON, new MediaType()
                                .schema(new Schema().$ref("Response"))));

        get.responses(new ApiResponses()
                .addApiResponse("200", response)
                .addApiResponse("default", errorResponse));

        final Operation post = new Operation()
                .summary("adds a new pet")
                .description("you can add a new pet this way")
                .addTagsItem("Pet Operations")
                .operationId("add pet")
                .responses(new ApiResponses()
                        .addApiResponse("default", errorResponse))
                .requestBody(new RequestBody()
                        .description("the pet to add")
                        .content(new Content().addMediaType("*/*", new MediaType()
                                .schema(new Schema().$ref("Response")))));

        swagger.paths(new Paths().addPathItem("/pets", new PathItem()
                .get(get).post(post)));
        final String swaggerJson = Json.mapper().writeValueAsString(swagger);
        //Json.prettyPrint(swagger);
        final OpenAPI rebuilt = Json.mapper().readValue(swaggerJson, OpenAPI.class);
        String result = Json.mapper().writeValueAsString(rebuilt);
        System.out.println(result);
        //SerializationMatchers.assertEqualsToJson(rebuilt, swaggerJson);
    }
}
