package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import decimal.apigateway.model.ServiceDef;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log
@Service
public class ServiceScopeValidator implements Validator{

    @Autowired
    ServiceValidator serviceValidator;

    @Autowired
    Request auditTraceFilter;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        String serviceName = httpHeaders.get(Headers.servicename.name());

        String userName = httpHeaders.get(Headers.username.name());

        String scopeToCheck = httpHeaders.get("scopetocheck");

        String orgId = httpHeaders.get(Headers.orgid.name());
        String appId = httpHeaders.get(Headers.appid.name());

        String version = httpHeaders.get(Constant.ROUTER_HEADER_SERVICE_VERSION);

        log.info("Validating Service scope for services name " + serviceName);
        if(version == null)
            version = "";

        List<String> serviceNamesData = RouterOperations.getStringArray(serviceName, Constant.TILD_SPLITTER);

        List<String> serviceNamesVersions = RouterOperations.getStringArray(version, Constant.TILD_SPLITTER);

        log.info("Number of services to be validated are " + serviceNamesData.size());
        log.info("Validating service scope one by one");

        for (int i=0;i<serviceNamesData.size();i++)
        {
            String serviceVersion = null;

            try{serviceVersion = serviceNamesVersions.get(i);}catch (Exception e){}

            ServiceDef serviceDef = serviceValidator.getService(orgId, appId, serviceNamesData.get(i), serviceVersion);

            validateScope(serviceDef, scopeToCheck, userName);

        }

        log.info("Validating  service scope is success.");

        return new MicroserviceResponse();
    }

    private void validateScope(ServiceDef serviceDef, String scopeToCheck, String userName) throws RouterException {

       int size = RouterOperations.getStringArray(userName, Constant.TILD_SPLITTER).size();

       boolean isUserSession = size > 3;

       String details = RouterOperations.getJoiningString(serviceDef.getOrgId(), serviceDef.getAppId(), serviceDef.getName());

       String serviceScope = serviceDef.getScope();

       if("PUBLIC".equalsIgnoreCase(scopeToCheck) && !scopeToCheck.equalsIgnoreCase(serviceScope))
       {
           String msg = "It seems that service scope is configured as " + serviceScope + " but it is called as PUBLIC for " + details;
           log.info(msg);
           throw new RouterException(RouterResponseCode.INVALID_REQUEST_SCOPE, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, msg);
       }

       if(!isUserSession && serviceScope.equalsIgnoreCase("SECURE"))
       {

           String msg = "It seems that service scope is configured as " + serviceScope + " but it is called as OPEN for " + details;
           log.info(msg);
           throw new RouterException(RouterResponseCode.INVALID_REQUEST_SCOPE, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, msg);
       }

    }
}
