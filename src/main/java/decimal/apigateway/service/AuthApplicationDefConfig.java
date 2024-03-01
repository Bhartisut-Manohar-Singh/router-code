package decimal.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.AuthRouterResponseCode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.ApplicationDefRedisConfig;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.repository.ApplicationDefRedisConfigRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class AuthApplicationDefConfig {

    @Autowired
    ApplicationDefRedisConfigRepo applicationDefRepo;

    @Autowired
    ObjectMapper objectMapper;

    public ApplicationDef findByOrgIdAndAppId(String orgId, String appId) throws RouterException
    {
        Optional<ApplicationDefRedisConfig> applicationDefConfig = applicationDefRepo.findByOrgIdAndAppId(orgId, appId);

        if (!applicationDefConfig.isPresent()) {
            throw new RouterException(AuthRouterResponseCode.APPID_DEF_NOT_FOUND, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Given OrgId and AppId is not registered");
        }

        try
        {
            return objectMapper.readValue(applicationDefConfig.get().getApiData(), ApplicationDef.class);
        }
        catch (IOException e) {
            throw new RouterException(AuthRouterResponseCode.APPID_DEF_NOT_FOUND, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "No valid configuration found for given orgId and appId");
        }
    }
}
