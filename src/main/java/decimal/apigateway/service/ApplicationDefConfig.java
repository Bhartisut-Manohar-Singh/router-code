package decimal.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constants;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.ApplicationDefRedisConfig;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.repository.ApplicationDefRedisConfigRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Log
public class ApplicationDefConfig {

    @Autowired
    ApplicationDefRedisConfigRepo applicationDefRepo;

    @Autowired
    ObjectMapper objectMapper;

    public ApplicationDef findByOrgIdAndAppId(String orgId, String appId) throws RouterException
    {
        String metaData = String.join("~", "OrgId: " + orgId, "AppId: " + appId);

        Optional<ApplicationDefRedisConfig> applicationDefConfig = applicationDefRepo.findByOrgIdAndAppId(orgId, appId);

        try {
            log.info(" ====  applicationDefConfig ==== " + objectMapper.writeValueAsString(applicationDefConfig.get()));
        }catch (Exception exception){
            log.info(" ==== exception ==== " + exception.getMessage());
        }

        if (!applicationDefConfig.isPresent()) {
            log.info("OrgId and AppId is not registered for given details: " + metaData);
            throw new RouterException(RouterResponseCode.APPID_DEF_NOT_FOUND, (Exception) null, Constants.ROUTER_ERROR_TYPE_VALIDATION, "OrgId and AppId is not registered for given details: " + metaData);
        }

        ApplicationDef applicationDef;
        try
        {
             applicationDef =  objectMapper.readValue(applicationDefConfig.get().getApiData(), ApplicationDef.class);
        }
        catch (IOException e) {
            log.info("No valid configuration found for given orgId and appId " + metaData);
            throw new RouterException(RouterResponseCode.APPID_DEF_NOT_FOUND, (Exception) null, Constants.ROUTER_ERROR_TYPE_VALIDATION, "No valid configuration found for given orgId and appId " + metaData);
        }

        if(!Constants.ACTIVE.equalsIgnoreCase(applicationDef.getStatus())){
            log.info("Application is not active for given details " + metaData);
            throw new RouterException(RouterResponseCode.APPID_DEF_NOT_ACTIVE, (Exception) null, Constants.ROUTER_ERROR_TYPE_VALIDATION, "Application is not active for given details " + metaData);
        }

        try {
            log.info(" ====  applicationDef ==== " + objectMapper.writeValueAsString(applicationDef));
        }catch (Exception exception){
            log.info(" ==== exception ==== " + exception.getMessage());
        }
        return applicationDef;
    }

    public String getIsLogsRequiredFlag(Map<String, String> httpHeaders) throws RouterException {

        String clientId = httpHeaders.get(Constants.CLIENT_ID);
        List<String> clientIdData = RouterOperations.getStringArray(clientId, Constants.TILD_SPLITTER);

        ApplicationDef applicationDef = findByOrgIdAndAppId(clientIdData.get(0), clientIdData.get(1));

        return applicationDef.getIsServerLogRequired() == null ? "Y" : applicationDef.getIsServerLogRequired();
    }
}
