package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.ApiAuthorizationConfig;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.repository.SecApiAuthorizationConfigRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@Log
public class ApiAuthorizationValidator implements Validator {

    @Autowired
    ServiceValidator serviceValidator;

    @Autowired
    SecApiAuthorizationConfigRepo apiAuthorizationConfigRepo;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        log.info("httpHeaders-------------from ApiAuthorizationValidator---"+httpHeaders);
        String sourceAppId = httpHeaders.get("sourceAppId");
        String destinationAppId = httpHeaders.get(Headers.destinationappid.name());
        String serviceName = httpHeaders.get(Headers.servicename.name());

        log.info("Checking access mgmt for source app id -- " + sourceAppId + " and destination app id -- " + destinationAppId);
        log.info("Checking access for service name -- " + serviceName);

        Optional<ApiAuthorizationConfig> bySourceAppIdAndDestinationAppId = apiAuthorizationConfigRepo.findBySourceAppIdAndDestinationAppId(sourceAppId, destinationAppId);

       log.info("response from bySourceAppIdAndDestinationAppId------"+bySourceAppIdAndDestinationAppId);
        ApiAuthorizationConfig apiAuthorizationConfig;
        if (bySourceAppIdAndDestinationAppId.isPresent()) {
            log.info("Access mgmt record found");

            apiAuthorizationConfig = bySourceAppIdAndDestinationAppId.get();

            log.info("apiAuthorizationConfig -- " + apiAuthorizationConfig);

            if (apiAuthorizationConfig.getAccessApiListSet().stream().noneMatch(apiListResponse -> apiListResponse.getApiName().equalsIgnoreCase(serviceName))) {
                String msg = "Source app is not authenticated to utilize APIs" +
                        " by destination app";
                throw new RouterException(RouterResponseCode.SOURCE_APP_AUTHENTICATION_FAILURE, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, msg);
            }

        } else {
            String msg = "Source app is not subscribing the destination app: " + destinationAppId;
            throw new RouterException(RouterResponseCode.SOURCE_APP_AUTHENTICATION_FAILURE, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, msg);
        }

        log.info("Validating service scope is success.");

        return new MicroserviceResponse(Constant.SUCCESS_STATUS, "Validation is done successfully", apiAuthorizationConfig);
    }
}
