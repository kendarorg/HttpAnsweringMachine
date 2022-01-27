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
import org.wiztools.xsdgen.ParseException;
import org.wiztools.xsdgen.XsdGen;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/schemavalidator/xml",
            method = "POST",
            id = "1000a4b4-29tad-1jsc-9621-0ww2ac130002")
    public void validateXml(Request req, Response res) throws IOException, ParseException, SAXException, ParserConfigurationException {
        var data = mapper.readValue(req.getRequestText(), ValidatorData.class);
        if(data.getSchema()==null || data.getSchema().length()==0){
            XsdGen gen = new XsdGen();
            try(var sourceStream = new ByteArrayInputStream(data.getTemplate().getBytes())) {
                gen.parse(sourceStream);
                try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    gen.write(out);
                    data.setSchema(new String( out.toByteArray(), StandardCharsets.UTF_8 ));
                }
            }
        }

        var result = new ValidatorResult();

        
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try(var schemaStream = new ByteArrayInputStream(data.getSchema().getBytes())) {
            Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setSchema(schema);
            SAXParser parser = saxFactory.newSAXParser();
            try(var sourceStream = new ByteArrayInputStream(data.getSource().getBytes())) {
                parser.parse(new InputSource(sourceStream), new DefaultHandler() {
                    // TODO: other handler methods
                    @Override
                    public void error(SAXParseException e) throws SAXException {
                        result.setError(true);
                        result.getErrors().add(e.getMessage());
                        throw e;
                    }
                });
            }
        }


        res.setStatusCode(200);
        res.addHeader("Content-type", "application/json");
        if(result.getErrors().size()>0) {
            res.setStatusCode(500);
        }
        res.setResponseText(mapper.writeValueAsString(result));
    }

}
