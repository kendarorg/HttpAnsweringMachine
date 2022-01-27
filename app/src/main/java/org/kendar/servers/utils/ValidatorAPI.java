package org.kendar.servers.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import com.saasquatch.jsonschemainferrer.*;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.utils.models.ValidatorData;
import org.kendar.servers.utils.models.ValidatorResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ValidatorAPI implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    private static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
            .setSpecVersion(SpecVersion.DRAFT_04)
            // Requires commons-validator
            //.addFormatInferrers(FormatInferrers.email(), FormatInferrers.ip())
            .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
            .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
            .build();
    @Override
    public String getId() {
        return this.getClass().getName();
    }

    protected com.networknt.schema.JsonSchema getJsonSchemaFromStringContent(String schemaContent) throws JsonProcessingException {
        //JsonSchemaFactory factory = JsonSchemaFactory.getInstance(com.networknt.schema.SpecVersion.VersionFlag.V4);
        JsonNode jsonNode = mapper.readTree(schemaContent);
        JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersionDetector.detect(jsonNode))).objectMapper(mapper).build();
        return factory.getSchema(schemaContent);
    }
    protected JsonNode getJsonNodeFromStringContent(String content) throws IOException {
        return mapper.readTree(content);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/schemavalidator/json",
            method = "POST",
            id = "1000a4b4-29tad-1jsc-9621-0242ac130002")
    public void validate(Request req, Response res) throws IOException {
        var data = mapper.readValue(req.getRequestText(), ValidatorData.class);
        if(data.getSchema()==null || data.getSchema().length()==0){
            final var sample = mapper.readTree(data.getTemplate());
            final var resultForSample = inferrer.inferForSample(sample);
            data.setSchema(mapper.writeValueAsString(resultForSample));
        }
        var schema = getJsonSchemaFromStringContent(data.getSchema());
        var toVerify = getJsonNodeFromStringContent(data.getSource());
        Set<ValidationMessage> errors = schema.validate(toVerify);

        var result = new ValidatorResult();
        var code = 200;
        for (var message : errors) {
            result.setError(true);
            code = 500;
            result.getErrors().add(message.getMessage());
        }
        res.addHeader("Content-type", "application/json");
        res.setStatusCode(code);
        res.setResponseText(mapper.writeValueAsString(result));
    }
}
