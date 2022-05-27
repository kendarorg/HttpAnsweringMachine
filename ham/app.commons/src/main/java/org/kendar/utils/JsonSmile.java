package org.kendar.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import java.io.IOException;

public class JsonSmile {
    public static String JSON_SMILE_MIME = "application/x-jackson-smile";
    private static ObjectMapper smileMapper = new ObjectMapper(new SmileFactory());
    private static ObjectMapper mapper = new ObjectMapper();

    public static byte[] jsonToSmile(String jsonValue) throws JsonProcessingException {
            JsonNode actualObj = mapper.readTree(jsonValue);
            return smileMapper.writeValueAsBytes(actualObj);
    }

    public static JsonNode smileToJSON(byte[] smileBytes) throws IOException {
            return smileMapper.readValue(smileBytes, JsonNode.class);
    }
}
