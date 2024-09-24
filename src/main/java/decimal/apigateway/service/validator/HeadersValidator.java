package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Log
@Service
public class HeadersValidator implements Validator {

    @Autowired
    Request requestObj;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException {

        String requestId = httpHeaders.get(Headers.requestid.name());
        String serviceName = httpHeaders.get(Headers.servicename.name());
        String userName = httpHeaders.get(Headers.username.name());

        log.info("Validating  basic headers like requestId, serviceName, userName");

        if (requestId == null || requestId.isEmpty())
        {
            log.info(RouterResponseCode.ROUTER_REQUEST_ID_MISSING);
            throw new RouterException(RouterResponseCode.ROUTER_REQUEST_ID_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validator class");
        }

        if (serviceName == null || serviceName.isEmpty())
        {
            log.info(RouterResponseCode.SERVICE_NAME_NOTNULL);
            throw new RouterException(RouterResponseCode.SERVICE_NAME_NOTNULL, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validator class");
        }

        if (userName == null || userName.isEmpty())
        {
            log.info(RouterResponseCode.ROUTER_USERNAME_MISSING);
            throw new RouterException(RouterResponseCode.ROUTER_USERNAME_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validator class");
        }

        log.info("Validating  headers is success.");
        return new MicroserviceResponse();
    }
}
