package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.ApplicationDefConfig;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Log
@Service
public class ClientSecretValidator implements Validator
{
    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    Request requestObj;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException
    {

        String orgId = httpHeaders.get(Headers.orgid.name());
        String appId = httpHeaders.get(Headers.appid.name());

        String clientSecret = httpHeaders.get(Constant.CLIENT_SECRET);

        log.info("Validating client secret.");

        if(clientSecret == null || clientSecret.isEmpty())
        {
            log.info("Client secret can not be empty or blank");
            throw new RouterException(RouterResponseCode.ROUTER_CLIENT_SECRET_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Client secret is missing from the request");
        }

        ApplicationDef applicationDef = applicationDefConfig.findByOrgIdAndAppId(orgId, appId);

        log.info("CLIENT_SECRET: "  + applicationDef.getClientSecret() + "~" + clientSecret);

        if(!applicationDef.getClientSecret().equalsIgnoreCase(clientSecret))
        {
            log.info("Invalid Client Secret");
            throw new RouterException(RouterResponseCode.INVALID_CLIENT_SECRET, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, "Invalid Client Secret.");
        }

        log.info("Validation of client secret is success.");
        return new MicroserviceResponse();
    }
}
