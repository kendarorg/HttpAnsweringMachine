package org.kendar.servers.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.utils.models.ValidatorData;
import org.kendar.servers.utils.models.ValidatorResult;

import java.io.IOException;

import static org.junit.Assert.*;

public class ValidatorAPITestJson {
    private final String S_VALID = "{\n" +
            "  \"firstName\": \"John\",\n" +
            "  \"lastName\": \"Doe\",\n" +
            "  \"age\": 21\n" +
            "}";
    private final String SCHEMA ="{\n" +
            "  \"$id\": \"https://example.com/person.schema.json\",\n" +
            "  \"$schema\": \"https://json-schema.org/draft-07/schema\",\n" +
            "  \"title\": \"Person\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"firstName\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"The person's first name.\"\n" +
            "    },\n" +
            "    \"lastName\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"The person's last name.\"\n" +
            "    },\n" +
            "    \"age\": {\n" +
            "      \"description\": \"Age in years which must be equal to or greater than zero.\",\n" +
            "      \"type\": \"integer\",\n" +
            "      \"minimum\": 0\n" +
            "    }\n" +
            "  }\n" +
            "}";
    private final String TEMPLATE= "{\n" +
            "  \"squadName\": \"Super hero squad\",\n" +
            "  \"homeTown\": \"Metro City\",\n" +
            "  \"formed\": 2016,\n" +
            "  \"secretBase\": \"Super tower\",\n" +
            "  \"active\": true,\n" +
            "  \"members\": [\n" +
            "    {\n" +
            "      \"name\": \"Molecule Man\",\n" +
            "      \"age\": 29,\n" +
            "      \"secretIdentity\": null,\n" +
            "      \"powers\": [\n" +
            "        \"Radiation resistance\",\n" +
            "        \"Turning tiny\",\n" +
            "        \"Radiation blast\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Madame Uppercut\",\n" +
            "      \"age\": 39,\n" +
            "      \"secretIdentity\": \"Jane Wilson\",\n" +
            "      \"powers\": [\n" +
            "        \"Million tonne punch\",\n" +
            "        \"Damage resistance\",\n" +
            "        \"Superhuman reflexes\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Eternal Flame\",\n" +
            "      \"age\": 1000000,\n" +
            "      \"secretIdentity\": \"Unknown\",\n" +
            "      \"powers\": [\n" +
            "        \"Immortality\",\n" +
            "        \"Heat Immunity\",\n" +
            "        \"Inferno\",\n" +
            "        \"Teleportation\",\n" +
            "        \"Interdimensional travel\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private final String VALID = "{\n" +
            "  \"squadName\": \"Super hero squad\",\n" +
            "  \"homeTown\": \"Metro City\",\n" +
            "  \"formed\": 2016,\n" +
            "  \"secretBase\": \"Super tower\",\n" +
            "  \"active\": true,\n" +
            "  \"members\": [\n" +
            "    {\n" +
            "      \"name\": \"Molecule Man\",\n" +
            "      \"age\": 29,\n" +
            "      \"secretIdentity\": \"Dan Jukes\",\n" +
            "      \"powers\": [\n" +
            "        \"Radiation resistance\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private final String VALID_NULL = "{\n" +
            "  \"squadName\": \"Super hero squad\",\n" +
            "  \"homeTown\": \"Metro City\",\n" +
            "  \"formed\": 2016,\n" +
            "  \"secretBase\": \"Super tower\",\n" +
            "  \"active\": true,\n" +
            "  \"members\": [\n" +
            "    {\n" +
            "      \"name\": \"Molecule Man\",\n" +
            "      \"age\": 29,\n" +
            //"      \"secretIdentity\": \"Dan Jukes\",\n" +
            "      \"powers\": [\n" +
            "        \"Radiation resistance\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private final String INVALID_NULL = "{\n" +
            "  \"squadName\": \"Super hero squad\",\n" +
            "  \"homeTown\": \"Metro City\",\n" +
            "  \"formed\": 2016,\n" +
            "  \"secretBase\": \"Super tower\",\n" +
            "  \"active\": true,\n" +
            "  \"members\": [\n" +
            "    {\n" +
            "      \"name\": null,\n" +
            "      \"age\": 29,\n" +
            //"      \"secretIdentity\": \"Dan Jukes\",\n" +
            "      \"powers\": [\n" +
            "        \"Radiation resistance\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private final String VALID_EMPTYARRAY = "{\n" +
            "  \"squadName\": \"Super hero squad\",\n" +
            "  \"homeTown\": \"Metro City\",\n" +
            "  \"formed\": 2016,\n" +
            "  \"secretBase\": \"Super tower\",\n" +
            "  \"active\": true,\n" +
            "  \"members\": [\n" +
            "  ]\n" +
            "}";
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldValidate() throws IOException {
        var target = new ValidatorAPI();
        var req = new Request();
        var res = new Response();
        var input = new ValidatorData();
        input.setTemplate(TEMPLATE);
        input.setSource(VALID);
        req.setRequestText(mapper.writeValueAsString(input));

        target.validate(req,res);
        var result = mapper.readValue(res.getResponseText(), ValidatorResult.class);
        assertFalse(result.isError());
        assertEquals(0,result.getErrors().size());
    }

    @Test
    public void shouldNotValidateSimple() throws IOException {
        var target = new ValidatorAPI();
        var req = new Request();
        var res = new Response();
        var input = new ValidatorData();
        input.setTemplate(TEMPLATE);
        input.setSource("{}");
        req.setRequestText(mapper.writeValueAsString(input));

        target.validate(req,res);
        var result = mapper.readValue(res.getResponseText(), ValidatorResult.class);
        assertTrue(result.isError());
        assertEquals(1,result.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateEmptyArray() throws IOException {
        var target = new ValidatorAPI();
        var req = new Request();
        var res = new Response();
        var input = new ValidatorData();
        input.setTemplate(TEMPLATE);
        input.setSource(VALID_EMPTYARRAY);
        req.setRequestText(mapper.writeValueAsString(input));

        target.validate(req,res);
        var result = mapper.readValue(res.getResponseText(), ValidatorResult.class);
        assertFalse(result.isError());
    }

    @Test
    @Ignore
    public void shouldValidateNUll() throws IOException {
        var target = new ValidatorAPI();
        var req = new Request();
        var res = new Response();
        var input = new ValidatorData();
        input.setTemplate(TEMPLATE);
        input.setSource(VALID_NULL);
        req.setRequestText(mapper.writeValueAsString(input));

        target.validate(req,res);
        var result = mapper.readValue(res.getResponseText(), ValidatorResult.class);
        assertFalse(result.isError());
    }

    @Test
    public void shouldNotValidateMissingField() throws IOException {
        var target = new ValidatorAPI();
        var req = new Request();
        var res = new Response();
        var input = new ValidatorData();
        input.setTemplate(TEMPLATE);
        input.setSource(INVALID_NULL);
        req.setRequestText(mapper.writeValueAsString(input));

        target.validate(req,res);
        var result = mapper.readValue(res.getResponseText(), ValidatorResult.class);
        assertTrue(result.isError());
        assertEquals(1,result.getErrors().size());
    }

    @Test
    public void shouldValidateWithSchema() throws IOException {
        var target = new ValidatorAPI();
        var req = new Request();
        var res = new Response();
        var input = new ValidatorData();
        input.setSchema(SCHEMA);
        input.setSource(S_VALID);
        req.setRequestText(mapper.writeValueAsString(input));

        target.validate(req,res);
        var result = mapper.readValue(res.getResponseText(), ValidatorResult.class);
        assertFalse(result.isError());
    }
}
