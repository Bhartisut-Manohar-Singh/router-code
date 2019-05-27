package decimal.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.EsbClient;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.apigateway.service.validator.RequestValidator;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    EsbClient esbClient;

    @Autowired
    SecurityClient securityClient;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        requestValidator.validatePlainRequest(request, httpHeaders);

        return esbClient.executeRequest(request, httpHeaders);
    }

    @Override
    public Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        Map<String, String> updatedHttpHeaders = requestValidator.validateRequest(request, httpHeaders);

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), httpHeaders);

        Object response = esbClient.executeRequest(decryptedResponse.getResponse().toString(), updatedHttpHeaders);

        System.out.println("Response from ESB: " + response);

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(response, httpHeaders);

        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus()))
        {
            throw new RouterException(decryptedResponse.getResponse());
        }

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;
    }
}
