package org.kendar.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

public class Serializer {
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectMapper smileMapper = new ObjectMapper(new SmileFactory());

    private static String toJson(Object source) throws JsonProcessingException {
        return mapper.writeValueAsString(source);
    }

    private static  <T> T  fromJson(String content,Class<T> type) throws JsonProcessingException {
        return mapper.readValue(content,type);
    }

    private static  <T> T fromJson(String content, TypeReference<T> valueTypeRef) throws JsonProcessingException {
        return mapper.readValue(content,valueTypeRef);
    }
}
