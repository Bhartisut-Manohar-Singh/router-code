package decimal.apigateway.service.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.ServiceConfig;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import decimal.apigateway.model.ServiceDef;
import decimal.apigateway.repository.ServiceConfigRepo;
import decimal.apigateway.service.security.VersionComparator;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log
@Service
public class ServiceValidator implements Validator{

    @Autowired
    ServiceConfigRepo serviceConfigRepo;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    Request auditTraceFilter;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info(" ======= request ======= " + request + " ======= httpHeaders ======= " + objectMapper.writeValueAsString(httpHeaders));

        String orgId = httpHeaders.get(Headers.orgid.name());
        String appId = httpHeaders.get(Headers.appid.name());

        String serviceName = httpHeaders.get(Headers.servicename.name());
        String requestId = httpHeaders.get(Headers.requestid.name());

        log.info("Validating service with name " + serviceName);

        String version = httpHeaders.get(Constant.ROUTER_HEADER_SERVICE_VERSION);

        if(version == null){
            log.info("Service version is null so setting it to latest version");
            version = "";
        }

        List<String> serviceNamesData = RouterOperations.getStringArray(serviceName, Constant.TILD_SPLITTER);

        List<String> serviceNamesVersions = RouterOperations.getStringArray(version, Constant.TILD_SPLITTER);

        List<ServiceDef> logFlags = new ArrayList<>();

        log.info("Number of services found to be validated are " + serviceNamesData.size());
        log.info("Validating service one by one");

        for (int i=0;i<serviceNamesData.size();i++)
        {
            String serviceVersion = null;

            try{
                serviceVersion = serviceNamesVersions.get(i);
            }catch (Exception e){}

            log.info("orgid-------"+orgId + "appid------"+appId + "serviceName------------"+serviceName);
            ServiceDef serviceDef = getService(orgId, appId, serviceNamesData.get(i), serviceVersion);

            logFlags.add(serviceDef);

        }

        log.info("Validating  service is success.");

       return new MicroserviceResponse(Constant.SUCCESS_STATUS, "Validation is done successfully", logFlags);
    }


    public ServiceDef getService(String orgId, String appId, String serviceName, String version) throws RouterException, IOException {
        log.info("orgid-------"+orgId + "appid------"+appId + "serviceName------------"+serviceName);
        List<ServiceConfig> configDataByOrgIdAndAppIdAndApiName = serviceConfigRepo.findByOrgIdAndAppIdAndApiName(orgId, appId, serviceName);
        log.info("configDataByOrgIdAndAppIdAndApiName=-----------"+configDataByOrgIdAndAppIdAndApiName);
        String details = RouterOperations.getJoiningString(orgId, appId, serviceName);
        log.info("details-------------"+details);

        if (configDataByOrgIdAndAppIdAndApiName.isEmpty())
        {
            log.info("No service configuration found for given serviceName " + serviceName);
            throw new RouterException(RouterResponseCode.ROUTER_SERVICE_NOT_FOUND, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "No Service found for given details. " + details);
        }

        ServiceConfig redisCommonConfig;

            redisCommonConfig = configDataByOrgIdAndAppIdAndApiName.stream().sorted(new VersionComparator()).collect(Collectors.toList()).get(0);

            if (redisCommonConfig==null)
                throw new RouterException(RouterResponseCode.ROUTER_SERVICE_NOT_FOUND_FOR_VERSION, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "No Service found for given details.");
            


        return objectMapper.readValue(redisCommonConfig.apiData, ServiceDef.class);
    }
}
