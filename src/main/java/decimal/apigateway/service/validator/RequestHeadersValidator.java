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
public class RequestHeadersValidator implements Validator {

    @Autowired
    Request requestObj;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException {

        String requestId = httpHeaders.get(Headers.requestid.name());
        String txnkey = httpHeaders.get(Headers.txnkey.name());
        String nounce = httpHeaders.get(Headers.nounce.name());

        String hash = httpHeaders.get(Headers.hash.name());
        String clientid = httpHeaders.get(Headers.clientid.name());
        String authorization = httpHeaders.get(Headers.authorization.name());


        log.info("Validating  basic headers like requestId, serviceName, userName");

        if (requestId == null || requestId.isEmpty())
        {
            log.info(RouterResponseCode.ROUTER_REQUEST_ID_MISSING);
            throw new RouterException(RouterResponseCode.ROUTER_REQUEST_ID_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validation Failed");
        }

        if (txnkey == null || txnkey.isEmpty())
        {
            log.info(RouterResponseCode.ROUTER_TXNKEY_MISSING);
            throw new RouterException(RouterResponseCode.ROUTER_TXNKEY_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validator Failed");
        }

        if (clientid == null || clientid.isEmpty())
        {
            log.info(RouterResponseCode.ROUTER_CLIENT_ID_MISSING);
            throw new RouterException(RouterResponseCode.ROUTER_CLIENT_ID_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validator Failed");
        }

        if (hash == null || hash.isEmpty())
        {
            log.info(RouterResponseCode.ROUTER_HASH_MISSING);
            throw new RouterException(RouterResponseCode.ROUTER_HASH_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validator Failed");
        }


        if (authorization == null || authorization.isEmpty())
        {
            log.info(RouterResponseCode.ROUTER_AUTH_TOKEN_MISSING);
            throw new RouterException(RouterResponseCode.ROUTER_AUTH_TOKEN_MISSING, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Validator Failed");
        }

        log.info("Validating  headers is success.");
        return new MicroserviceResponse();
    }
}
