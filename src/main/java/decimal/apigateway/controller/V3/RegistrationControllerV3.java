package decimal.apigateway.controller.V3;


import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.EsbOutput;
import decimal.apigateway.service.ExecutionServiceV3;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.RegistrationServiceV3;
import decimal.logs.connector.LogsConnector;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.commons.Constant.INVALID_REQUEST_500;
import static decimal.apigateway.commons.Constant.JSON;
import static decimal.apigateway.commons.Constant.MULTIPART;

@RestController
@RequestMapping("engine/v3/")
@CrossOrigin(exposedHeaders = {"custom_headers", "security-version", "hash", "authorization", "deviceid", "sourceAppId", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin", "auth_scheme","storageid", "imeinumber"})
@Log
public class RegistrationControllerV3 {

    private final RegistrationServiceV3 registrationServiceV3;

    private final ExecutionServiceV3 executionService;

    private final ObjectMapper mapper;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    AuditPayload auditPayload;

    @Autowired
    public RegistrationControllerV3(RegistrationServiceV3 registrationServiceV3, ExecutionServiceV3 executionService, ObjectMapper mapper) {
        this.registrationServiceV3 = registrationServiceV3;
        this.executionService = executionService;
        this.mapper = mapper;
    }

    /**
     * Registation API for Public Requests.
     * @param request
     * @param httpHeaders
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("register")
    public Object executeService(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        log.info("-------register call v3-------------");
        return registrationServiceV3.register(request, httpHeaders, response);
    }

    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws  IOException, RouterException {
        log.info("==============================Public Gateway Processor=============================");
        String authorizationToken = httpHeaders.get("authorization");
        String responseType = httpHeaders.get("response-type");
        log.info("--------authorization token----------" + authorizationToken);
        if (authorizationToken == null || !authorizationToken.startsWith("Bearer")) {
            auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);
            throw new RouterException(INVALID_REQUEST_500, "Invalid JWT token", null);
        }
        if (responseType !=null && MULTIPART.equalsIgnoreCase(responseType))
            return executionService.executeMultiPart(request,httpHeaders);

        Object o = executionService.executePlainRequest(request, httpHeaders);
        EsbOutput output = mapper.convertValue(o, EsbOutput.class);

        if (output.getStatusCode()==null || output.getStatusCode().isEmpty()){
            return new ResponseEntity<>(output.getResponse(), HttpStatus.OK);
        }
        return new ResponseEntity<>(output.getResponse(), HttpStatus.valueOf(Integer.parseInt(output.getStatusCode())));
    }

}
