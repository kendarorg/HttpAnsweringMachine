package org.kendar.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * https://editor.swagger.io/
 * https://swagger.io/docs/specification/2-0/basic-structure/
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

        ApiResponse expectedResponse = new ApiResponse()
                .description("respo200");

        var xxx = new Response();
        xxx.setStatusCode(200);
        xxx.setResponseText("BAH");
        xxx.addHeader("test","test");
        Content content = new Content()
                .addMediaType("application/json",
                        new MediaType()

                               .addExamples("0001",
                                        new Example().value(
                                                Json.mapper().writeValueAsString(xxx)
                                        )));

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
}
