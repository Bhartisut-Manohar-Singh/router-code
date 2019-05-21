package decimal.apigateway.commons;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

public class Jackson {
	private Jackson()
	{
		
	}
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
	public static JsonNode stringToJsonNode(String jsonString) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(jsonString);
	}
	public static Map<String, Object> stringToMap(String jsonString) {
		try
		{
		ObjectMapper mapper = new ObjectMapper();
		// convert JSON string to Map
		return mapper.readValue(jsonString, new TypeReference<Map<String, String>>(){});
		
		}catch(Exception e)
		{
			return null;
		}
	}

	public static Map<String, String> objectNodeToMap(ObjectNode node) {
		try {
			return new ObjectMapper().readValue(node.toString(), new TypeReference<Map<String, String>>(){});
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}
