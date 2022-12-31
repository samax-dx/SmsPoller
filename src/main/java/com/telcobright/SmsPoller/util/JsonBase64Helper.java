package com.telcobright.SmsPoller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class JsonBase64Helper {
    public static Map<String, String> decodeField(String str) {
        byte[] decoded = Base64.getDecoder().decode(str);
        String decodedStr = new String(decoded, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        // convert JSON string to Map
        Map<String, String> map = null;
        try {
            map = mapper.readValue(decodedStr, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
    public static String encodeField(Map<String,String> map){
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(map);
            String encoded=Base64.getEncoder().encodeToString(jsonResult.getBytes());
            return encoded;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}

