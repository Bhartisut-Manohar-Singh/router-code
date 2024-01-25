package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.security.EncryptionDecryptionService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Log
@Service
public class HashValidator implements Validator {

    @Autowired
    EncryptionDecryptionService encryptionDecryptionService;

    @Autowired
    Request requestObj;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException
    {
        log.info("Validating request hash");

        log.info("Generating hash at server side");

        MicroserviceResponse microserviceResponse = encryptionDecryptionService.generateAuthResponseHash(request, httpHeaders);

        String responseHash = microserviceResponse.getMessage();

        String requestHash = httpHeaders.get("hash");

        log.info("Check request hash with server hash to be equal or not");

        if(requestHash == null || !requestHash.equalsIgnoreCase(responseHash))
        {
            log.info("Hash received by client is: " + requestHash + " and created by server is: " + responseHash);
            throw new RouterException(RouterResponseCode.REQUEST_HASH_MISMATCH, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Either hash is null or provided hash is mismatched with server hash");
        }

        log.info("Validating hash is success.");

        return new MicroserviceResponse();
    }
}
