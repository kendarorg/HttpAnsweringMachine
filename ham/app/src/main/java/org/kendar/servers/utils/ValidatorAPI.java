package org.kendar.servers.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.utils.models.ValidatorData;
import org.kendar.servers.utils.models.ValidatorResult;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.xml.DiffInferrer;
import org.kendar.xml.model.XmlException;
import org.springframework.stereotype.Component;
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
import java.io.IOException;
import java.util.Set;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ValidatorAPI implements FilteringClass {

    ObjectMapper mapper = new ObjectMapper();
    DiffInferrer diffInferrer = new DiffInferrer();

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    protected com.networknt.schema.JsonSchema getJsonSchemaFromStringContent(String schemaContent) throws JsonProcessingException {
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
    @HamDoc(
            description = "Validate JSON against schema or example",
            requests = @HamRequest(
                    body = ValidatorData.class
            ),
            responses = @HamResponse(
                    body = ValidatorResult.class
            ),
            tags = {"base/utils"})
    public void validate(Request req, Response res) throws IOException {
        Set<ValidationMessage> errors;
        var result = new ValidatorResult();
        result.setError(false);

        var data = mapper.readValue(req.getRequestText(), ValidatorData.class);
        if (data.getSchema() == null || data.getSchema().length() == 0) {
            try {
                diffInferrer.diff(data.getTemplate(), data.getSource());
            } catch (XmlException ex) {
                result.setError(true);
                result.getErrors().add(ex.getMessage());
            }
        } else {
            var schema = getJsonSchemaFromStringContent(data.getSchema());
            var toVerify = getJsonNodeFromStringContent(data.getSource());
            errors = schema.validate(toVerify);
            for (var message : errors) {
                result.setError(true);
                result.getErrors().add(message.getMessage());
            }
        }


        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/schemavalidator/xml",
            method = "POST",
            id = "1000a4b4-29tad-1jsc-9621-0ww2ac130002")
    @HamDoc(
            description = "Validate XML against schema or example",
            requests = @HamRequest(
                    body = ValidatorData.class
            ),
            responses = @HamResponse(
                    body = ValidatorResult.class
            ),
            tags = {"base/utils"})
    public void validateXml(Request req, Response res) throws IOException, SAXException, ParserConfigurationException {
        var data = mapper.readValue(req.getRequestText(), ValidatorData.class);

        var result = new ValidatorResult();
        if (data.getSchema() == null || data.getSchema().length() == 0) {
            try {
                diffInferrer.diff(data.getTemplate(), data.getSource());
            } catch (XmlException ex) {
                result.setError(true);
                result.getErrors().add(ex.getMessage());
            }
        } else {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try (var schemaStream = new ByteArrayInputStream(data.getSchema().getBytes())) {
                Schema schema = schemaFactory.newSchema(new StreamSource(schemaStream));
                SAXParserFactory saxFactory = SAXParserFactory.newInstance();
                saxFactory.setSchema(schema);
                SAXParser parser = saxFactory.newSAXParser();
                try (var sourceStream = new ByteArrayInputStream(data.getSource().getBytes())) {

                    parser.parse(new InputSource(sourceStream), new DefaultHandler() {
                        @Override
                        public void error(SAXParseException e) throws SAXException {
                            result.setError(true);
                            result.getErrors().add(e.getMessage());
                        }
                    });
                }
            }
        }
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }

}
