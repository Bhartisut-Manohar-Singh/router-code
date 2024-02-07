package decimal.apigateway.service.security;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.ICryptoUtil;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.ApplicationDefConfig;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static decimal.apigateway.commons.Constant.IS_DIGITALLY_SIGNED;
import static decimal.apigateway.commons.Constant.IS_PAYLOAD_ENCRYPTED;
import static decimal.apigateway.enums.Headers.requestid;


@Service
@Log
public class EncryptionDecryptionServiceImpl implements EncryptionDecryptionService {

    private static final String PRIVATE_MODULUS_STR = "private-modules";
    private static final String PRIVATE_EXPONENT_STR = "private-exponent";

    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    CryptoFactory cryptoFactory;

    @Autowired
    AuthSecurity authSecurity;

    @Autowired
    AuthenticationSession authenticationSession;

    @Override
    public MicroserviceResponse generateResponseHash(String request, Map<String, String> httpHeaders) throws RouterException {
        log.info("In security service - generate response hash.....");
        List<String> clientId = RouterOperations.getStringArray(httpHeaders.get("clientid"), Constant.TILD_SPLITTER);

        String orgId = clientId.get(0);
        String appId = clientId.get(1);

        log.info("org id -- " + orgId);
        log.info("app id -- " + appId);

        ApplicationDef byOrgIdAndAppId = applicationDefConfig.findByOrgIdAndAppId(orgId, appId);

        log.info("application def found... ");

        String clientSecret = byOrgIdAndAppId.getClientSecret();

        String securityVersion = httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION);
        String requestId = httpHeaders.get(Constant.ROUTER_HEADER_REQUEST_ID);


        ICryptoUtil securityVersion1 = cryptoFactory.getSecurityVersion(securityVersion, requestId);

        log.info("crypto util found....");

        String responseHash = securityVersion1.generateSHA512Hash(request, clientSecret);

        log.info("Response hash generated.....");

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constant.SUCCESS_STATUS);
        response.setMessage(responseHash);

        return response;
    }

    @Override
    public MicroserviceResponse decryptRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        String plainRequest = String.valueOf(authSecurity.decryptRequestV1(request, httpHeaders));
        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constant.SUCCESS_STATUS);
        response.setResponse(plainRequest);

        return response;
    }
    @Override
    public MicroserviceResponse decryptRequestWithoutSession(String request, Map<String, String> httpHeaders) throws RouterException {

        Map<String,String> customMap = new HashMap<>();
        String requestId = httpHeaders.get(requestid.name());
        String clientSecret = httpHeaders.get(Headers.clientsecret.name());
        String securityVersion = httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION);

        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVersion, requestId );
        MicroserviceResponse response = new MicroserviceResponse();

        if(("Y").equalsIgnoreCase(httpHeaders.get(IS_PAYLOAD_ENCRYPTED)))
        {
            String txnKey = httpHeaders.get(Headers.txnkey.name());
            String decryptedTxnKey= iCryptoUtil.decryptTextUsingAES ( txnKey, clientSecret );
            String plainRequest = iCryptoUtil.decryptTextUsingAES (request , decryptedTxnKey );
            request =plainRequest;
            response.setResponse(plainRequest);
        }

        if(("Y").equalsIgnoreCase(httpHeaders.get(IS_DIGITALLY_SIGNED))) {
            String signature = httpHeaders.get(Headers.hash.name());
            String hash = iCryptoUtil.generateSHA512Hash(request, clientSecret);

            if (!hash.equalsIgnoreCase(signature)) {
                System.out.println("-----------------hmac validation failed--------------");
                throw new RouterException(RouterResponseCode.INVALID_SIGNATURE, Constant.ROUTER_ERROR_TYPE_SECURITY, "Signature verification failed","Signature verification failed",null);
            }



        }

        response.setStatus(Constant.SUCCESS_STATUS);
        response.setCustomData(customMap);

        return response;
    }

    @Override
    public MicroserviceResponse generateAuthResponseHash(String body, Map<String, String> httpHeaders) throws RouterException {

        String decryptedAESKey = authSecurity.getAesKey(httpHeaders);

        String requestId = httpHeaders.get(Headers.requestid.name());
        String securityVersion = httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION);

        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion(securityVersion, requestId);

        String responseHash = iCryptoUtil.generateSHA512Hash ( body, decryptedAESKey );

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constant.SUCCESS_STATUS);
        response.setMessage(responseHash);

        return response;
    }

    @Override
    public MicroserviceResponse encryptResponse(String request, Map<String, String> httpHeaders) throws RouterException {

        String decryptedAESKey = authSecurity.getAesKey(httpHeaders);

        String txnKey = httpHeaders.get("txnkey");
        String securityVersion = httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION);

        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion(securityVersion, txnKey);

        String responseHash = iCryptoUtil.encryptTextUsingAES ( request, decryptedAESKey );

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constant.SUCCESS_STATUS);
        response.setMessage(responseHash);

        return response;
    }

    @Override
    public MicroserviceResponse encryptResponseWithoutSession(String request, Map<String, String> httpHeaders) throws RouterException {

        String txnKey = httpHeaders.get(Headers.txnkey.name());
        String clientSecret = httpHeaders.get(Headers.clientsecret.name());
        String securityVersion = httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION);

        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion(securityVersion, txnKey);

        String decryptedTxnKey= iCryptoUtil.decryptTextUsingAES ( txnKey, clientSecret );
        String responseHash = iCryptoUtil.encryptTextUsingAES(request, decryptedTxnKey);

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constant.SUCCESS_STATUS);
        response.setMessage(responseHash);

        return response;
    }
}
