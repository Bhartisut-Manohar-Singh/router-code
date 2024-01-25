package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.ApplicationDefConfig;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Log
@Service
public class SourceIpValidator implements Validator {
    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    Request auditTraceFilter;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException {
        String orgId = httpHeaders.get(Headers.orgid.name());
        String appId = httpHeaders.get(Headers.appid.name());
        String requestId = httpHeaders.get(Headers.requestid.name());


        log.info("Validating source Ip.");

        ApplicationDef applicationDef = applicationDefConfig.findByOrgIdAndAppId(orgId, appId);
        String sourceIp = httpHeaders.get(Constant.ROUTER_HEADER_SOURCE_IP.toLowerCase());

        String sourceIpValReq = applicationDef.getIsSourceIPValidationRequired();

        if (!"Y".equalsIgnoreCase(sourceIpValReq)) {
            log.info("Source IP validation is not enabled ");
            return new MicroserviceResponse();
        }

        String sourceIps = applicationDef.getAllowedSourceIPs();

        if (sourceIps == null || sourceIps.isEmpty())
            return new MicroserviceResponse();

        List<String> sourceIpList = Arrays.asList(sourceIps.split(Constant.TILD_SPLITTER));

        if (sourceIpList.contains(sourceIp)) {
            log.info("Validating  source ip is success.");
            return new MicroserviceResponse();
        }

        log.info("Error in validating remote IP Address. Remote addresses allowed are :" + applicationDef.getAllowedSourceIPs() + " and Request originator IP is:" + sourceIp);
        throw new RouterException(RouterResponseCode.NON_AUTHENTICATED_REMOTE_ADDRESS, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, "Validating remote IP Address failed.");
    }
}
