package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Account;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import  decimal.apigateway.service.security.AuthSecurity;
import decimal.apigateway.service.ApplicationDefConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static decimal.apigateway.enums.Headers.nounce;
import static decimal.apigateway.enums.Headers.requestid;


@Service
@Log
public class AuthHeaderValidator implements Validator {

    @Autowired
    AuthSecurity authSecurity;

    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    Request requestObj;
   
    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("+++++++++++++In AuthHeaderValidator++++++++++");

        String requestId = httpHeaders.get(requestid.name());

        log.info("Processing authorization request");

        Account account = new Account();

        //Validate basic header parameters for Registration/Auth
        initializeAccountData(account, httpHeaders);

        // Security Checks for authentications of Application auth or User auth
        Map<String, String> responseData = authSecurity.decryptAuthHeader(account.getSecurityVersion(), account, account.getAuthorization());
        account.setUsername(responseData.get(Constant.ROUTER_HEADER_USERNAME));
        account.setNounce(responseData.get("nounce"));

        // Validate nounce data
        authSecurity.validateData(account);

        MicroserviceResponse response = new MicroserviceResponse();
        response.setResponse(account);

        log.info("Processing authorization request has been done");
        return response;
    }

    private void initializeAccountData(Account account, Map<String, String> httpHeaders) throws RouterException {

        log.info("+++++++++In initializeAccountData +++++++++++");
        String requestId = httpHeaders.get(requestid.name());

        log.info("Validating basic header parameters (nounce, authorization) of request for security");

        try {
            account.setAccountData(httpHeaders);
            account.setRequestId(requestId);
            account.setNounce(httpHeaders.get(nounce.name()));
            account.setAuthorization(httpHeaders.get("authorization").replace("Basic ", ""));
            account.setSecurityVersion(httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION));

        } catch (Exception e) {
            String message = "Error in getting parameters from header parameters.Please make sure you have passed header parameters with name nounce, authorization, requestid";

            log.info(message + " Exception: " + e.getMessage());

            throw new RouterException(RouterResponseCode.SET_ACCOUNT_DATA_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, message);
        }

        setApplicationDefData(account);

        log.info("Validating mandatory security headers check has been done successfully");
    }

    private void setApplicationDefData(Account account) throws RouterException {

        log.info("+++++++In set ApplicationDefData+++++++++++++++");
        log.info("Validating for orgId and appId to be exist in redis ");

        try {
            List<String> clientIdData = RouterOperations.getStringArray(account.getAccountData().get("clientid"), "~");

            log.info("clientIdData : " +clientIdData);
            ApplicationDef def = applicationDefConfig.findByOrgIdAndAppId(clientIdData.get(0), clientIdData.get(1));
            account.setApplicationDef(def);
        } catch (RouterException ex)
        {
            log.info("Some error occurred when validating application: " + ex.getErrorHint());
            throw ex;
        } catch (Exception e) {
            String message = "Error in getting OrgId and AppId from ClientId parameter.Please make sure you have passed parameter in header with name clientid with value orgid~appid";
            log.info(message + " Exception: " + e.getMessage());

            throw new RouterException(RouterResponseCode.ORGID_APPID_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, message);
        }

        log.info("Validation for orgId and appId to be exist in redis is done");



    }
}
