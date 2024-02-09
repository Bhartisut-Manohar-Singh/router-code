package decimal.apigateway.commons;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.loggers.Loggers.ERROR_LOGGER;

@Component
public class JacksonAuth {
    @Autowired
    ObjectMapper objectMapper;

    public static <T> JsonNode objectToJson(T t) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(t);
        return mapper.readTree(jsonInString);
    }

    public static <T> String objectToJsonString(T t) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(t);
    }

    public static <T> ArrayNode objectNodeToArrayNode(T t) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arr = mapper.createArrayNode();
        String jsonInString = mapper.writeValueAsString(t);
        return arr.add(mapper.readTree(jsonInString));
    }

    public static JsonNode stringToJsonNode(String request) throws IOException {
        return new ObjectMapper().readTree(request);
    }

    public ObjectNode objectToObjectNode(Object jsonString) {
        if (jsonString == null) {
            return null;
        }

        if (jsonString instanceof String) {
            return stringToObjectNode(String.valueOf(jsonString));
        } else {
        	try
			{
				return objectMapper.convertValue(jsonString, ObjectNode.class);
			}
        	catch (Exception ex)
			{
				ERROR_LOGGER.error("Error occurred in parsing request to object node");
			}

        	return objectMapper.createObjectNode();

        }
    }

    public static Map<String, Object> stringToMap(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // convert JSON string to Map
            return mapper.readValue(jsonString, new TypeReference<Map<String, String>>() {
            });

        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> objectNodeToMap(ObjectNode node) {
        try {
            return new ObjectMapper().readValue(node.toString(), new TypeReference<Map<String, String>>() {
            });
        } catch (Exception ex) {
            return null;
        }
    }

    public ObjectNode stringToObjectNode(String request) {

        if (request == null || request.isEmpty())
            return null;


        try {
            return objectMapper.readValue(request, ObjectNode.class);
        } catch (IOException e) {
            ERROR_LOGGER.error("Error occurred in parsing request to object node");
        }

        return objectMapper.createObjectNode();
    }
}
