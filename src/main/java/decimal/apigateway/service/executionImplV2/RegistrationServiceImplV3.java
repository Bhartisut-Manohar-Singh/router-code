package decimal.apigateway.service.executionImplV2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.ResponseOperations;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.PublicTokenCreationException;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.ResponseOutput;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.RegistrationServiceV3;
import decimal.apigateway.service.clients.AuthenticationClient;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.apigateway.service.validator.RequestValidatorV2;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static decimal.apigateway.commons.Constant.*;


@Service
@Log
public class RegistrationServiceImplV3 implements RegistrationServiceV3 {

    private ObjectMapper objectMapper;

    @Autowired
    ResponseOperations responseOperations;

    @Autowired
    RequestValidatorV2 requestValidatorV2;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    AuditPayload auditPayload;

    @Value("${isHttpTracingEnabled}")
    boolean isHttpTracingEnabled;

    private final SecurityClient securityClient;

    private final AuthenticationClient authenticationClient;


    @Autowired
    public RegistrationServiceImplV3(ObjectMapper objectMapper, SecurityClient securityClient, AuthenticationClient authenticationClient) {
        this.objectMapper = objectMapper;
        this.securityClient = securityClient;
        this.authenticationClient = authenticationClient;
    }

    @Override
    public Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        log.info("Executing Step 1 to validate register request.....");

        String clientId = httpHeaders.get(Constant.ORG_ID) + Constant.TILD_SPLITTER + httpHeaders.get(Constant.APP_ID);
        httpHeaders.put(Constant.CLIENT_ID, clientId);

        List<String> tokenDetails = fetchTokenDetails(httpHeaders);

        httpHeaders.put(Constant.LOGIN_ID, tokenDetails.get(0));
        httpHeaders.put(Constant.CLIENT_SECRET, tokenDetails.get(1));
        httpHeaders.put(Constant.ROUTER_HEADER_SECURITY_VERSION, "2");
        httpHeaders.put(Headers.servicename.name(), "REGISTERAPP");

        ObjectNode jsonNodes;

        try {
            jsonNodes = objectMapper.convertValue(requestValidatorV2.validatePublicRegistrationRequest(request, httpHeaders), ObjectNode.class);

            log.info("Response from Step 1 ....." + jsonNodes);

            httpHeaders.put("executionsource", "API-GATEWAY");

            log.info("Executing Step 2 to Generate token.....");

            ResponseEntity<Object> responseEntity = authenticationClient.publicRegister(request, httpHeaders);

            log.info("Response from Step 1....." + responseEntity.getBody());

            HttpHeaders responseHeaders = responseEntity.getHeaders();
            if (responseHeaders != null && responseHeaders.containsKey("status"))
                auditPayload.setStatus(responseHeaders.get("status").get(0));

            MicroserviceResponse registerResponse = objectMapper.convertValue(responseEntity.getBody(), MicroserviceResponse.class);
            Map<String, Object> rsaKeysMap = objectMapper.convertValue(registerResponse.getResponse(), new TypeReference<Map<String, Object>>() {
            });

            String jwtToken = String.valueOf(rsaKeysMap.get("jwtToken"));

            ObjectNode node = objectMapper.createObjectNode();

            response.addHeader("Authorization", "Bearer " + jwtToken);

            node.put("Authorization", "Bearer " + jwtToken);

            return new ResponseOutput(SUCCESS_STATUS, JWT_TOKEN_SUCCESS);
        } catch (Exception routerException) {
            return new PublicTokenCreationException(FAILURE_STATUS, JWT_TOKEN_FAILURE);
        }
    }

    private List<String> fetchTokenDetails(Map<String, String> httpHeaders) throws RouterException {
        String authorizationToken = httpHeaders.get("authorization");

        if (authorizationToken == null || !authorizationToken.startsWith("Basic")) {
            throw new RouterException(INVALID_REQUEST_500, "Invalid JWT token", null);
        }

        String decodedToken;

        try {
            String token = authorizationToken.replace("Basic", "");
            log.info("Encoded Token received - " + token);

            byte[] decoded = Base64.getDecoder().decode(token.split(" ")[1]);
            decodedToken = new String(decoded, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RouterException(INVALID_REQUEST_500, "Invalid JWT token", null);
        }

        log.info("decoded token = " + decodedToken);

        List<String> token = RouterOperations.getStringArray(decodedToken, Constant.COLON);

        //token format - loginId:clientsecret
        if (token.size() != 2) {
            throw new RouterException(INVALID_REQUEST_500, "Invalid JWT token", null);
        }

        return token;
    }
}
