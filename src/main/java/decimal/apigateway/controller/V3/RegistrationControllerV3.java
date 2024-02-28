package decimal.apigateway.controller.V3;


import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.ExecutionServiceV3;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.RegistrationServiceV3;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.commons.Constant.*;


@RestController
@RequestMapping("engine/v3/")
@CrossOrigin(exposedHeaders = {"custom_headers", "security-version", "hash", "authorization", "deviceid", "sourceAppId", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin", "auth_scheme","storageid", "imeinumber"})
@Log
public class RegistrationControllerV3 {

    private final RegistrationServiceV3 registrationServiceV3;

    private final ExecutionServiceV3 executionService;
    @Autowired
    AuditPayload auditPayload;

    @Autowired
    LogsWriter logsWriter;
    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    public RegistrationControllerV3(RegistrationServiceV3 registrationServiceV3, ExecutionServiceV3 executionService) {
        this.registrationServiceV3 = registrationServiceV3;
        this.executionService = executionService;
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
            throw new RouterException(INVALID_REQUEST_500, "Invalid JWT token", null);
        }
        if (responseType !=null && MULTIPART.equalsIgnoreCase(responseType))
            return executionService.executeMultiPart(request,httpHeaders);
        auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);
        auditTraceFilter.setIsServicesLogsEnabled(true);
        return executionService.executePlainRequest(request, httpHeaders);
    }

}
