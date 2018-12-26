package org.team_pjt.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static String getJsonString(Object obj ) {
        try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	public static <T> T getInstance(String jsonString, TypeReference<?> type) {
		if ( jsonString != null ) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                T data = mapper.readValue(jsonString, type);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
		return null;
	}
}
