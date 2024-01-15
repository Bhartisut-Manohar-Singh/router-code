package decimal.apigateway.service.security;

import com.google.gson.JsonObject;
import decimal.apigateway.commons.*;
import decimal.apigateway.domain.Session;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Account;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.ApplicationDefConfig;
import decimal.apigateway.repository.AuthenticationSessionRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static decimal.apigateway.enums.Headers.requestid;
import static decimal.apigateway.enums.Headers.username;


@Component
@SuppressWarnings("WeakerAccess")
@Log
public class AuthSecurityImpl implements AuthSecurity {

    @Value("${deviceTimeDiffAllowedInMinutes}")
    int deviceTimeDiffAllowedInMinutes;

    @Autowired
    CryptoFactory cryptoFactory;

    @Autowired
    CryptographyService cryptographyService;


    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    AuthenticationSessionRepo authenticationSessionRepo;

    @Autowired
    Request requestObj;

    @Override
    public Map<String, String> decryptAuthHeader(String securityVersion, Account account, String headerData)
            throws RouterException {
        log.info("++++++++++++In DecryptAuthHeader+++++++++++++++");
        log.info("Processing decryptAuthHeader - Authentication");
        String key = null;

        try {

            key = (decodeBase64Request(account.getNounce()) + account.getApplicationDef().getClientSecret()).substring(0, 32);

            log.info(" ==== inside decryptAuthHeader with securityVersion, with key, header data, requestId ==== " + securityVersion + " "+ key +" "+ headerData.trim() + " " + account.getRequestId());
            String decryptedString = String.valueOf(cryptographyService.decryptJWTToken(securityVersion, key, headerData.trim(), account.getRequestId()));
            return decryptAuthorizationPayload(account.getRequestId(), decryptedString);

        } catch (RouterException ex) {
            throw ex;
        } catch (Exception e) {
            log.info("Exception in decrypting header data for key: " + key + " and headerData: " + headerData.trim() + " \n because of : " + e.getMessage()+" Exception: "+ e);
            throw new RouterException(RouterResponseCode.AUTH_HEADER_DATA_DECRYPTION_ERROR, e, Constants.ROUTER_ERROR_TYPE_SECURITY, "Exception in data decryption header data");
        }
    }

    @Override
    public boolean validateData(Account account) throws RouterException {

        log.info("+++++++++++++++++In ValidateData Method+++++++++++++++");
        log.info("Checking nounce date to be valid or not for security");
        try {
            checkNounceDate(account);

            log.info("Nounce is validated successfully");
            return true;
        } catch (RouterException e) {
            log.info("Nounce is invalid. Reason is: "+ e.getErrorHint());
            throw e;
        } catch (Exception e) {
            log.info("Error in nounce validation. Exception: "+ e);
            throw new RouterException(RouterResponseCode.NOUNCE_VALIDATION_ERROR, e, Constants.ROUTER_ERROR_TYPE_SECURITY, "Error when validating nounce data");
        }

    }

    private String decodeBase64Request(String data) throws RouterException {

        log.info("Decoding nounce for getting header data");
        try {
            return new String(AuthCryptoUtil.hexStringToByteArray(data.trim().toCharArray()));
        } catch (Exception e) {
            log.info("Exception in decode header data - Authentication" + e.getLocalizedMessage());
            throw new RouterException(RouterResponseCode.DECODE_HEX_STRING, e, Constants.ROUTER_ERROR_TYPE_SECURITY, null);
        }
    }

    @Override
    public void checkAuthDataHash(String securityVersion, Account account, String data, String requestId)
            throws RouterException {

        log.info("Validating Header Hash Data");

        String headerHash = cryptographyService.getHeaderDataHash
                (securityVersion, account, "Basic " + data, account.getApplicationDef().getClientSecret(), requestId);
        if (null != account.getAccountData().get("hash") && !headerHash.equalsIgnoreCase(account.getAccountData().get("hash").toString())) {

            log.info("Header Hash is invalid. Server HASH is:" + headerHash + " and HASH sent by device is:" + account.getAccountData().get("hash"));
            throw new RouterException(RouterResponseCode.INVALID_HTTP_HEADER_HASH, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Header hash is invalid");
        } else {
            if (null == account.getAccountData().get("hash")) {
                log.info("Header HASH sent by device is null.");
                throw new RouterException(RouterResponseCode.BLANK_HTTP_HEADER_HASH, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Header hash is null or empty");
            }
        }
    }

    private void checkNounceDate(Account account) throws RouterException {

        if (!RouterOperations.hexToString(account.getAccountData().get("nounce")).equalsIgnoreCase(account.getNounce()))
        {
            log.info("Invalid nounce found in request " + account.getAccountData().get("nounce"));

            throw new RouterException(RouterResponseCode.INVALID_NOUNCE, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Invalid nounce found");
        }

        log.info("Calculating time difference between the client and server");

        LocalTime timeRequest = Instant.ofEpochMilli(Long.parseLong(account.getNounce()))
                .atZone(ZoneId.ofOffset("", ZoneOffset.UTC)).toLocalTime();

        // Current time in GMT
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        Date date = Date.from(utc.toInstant());

        LocalTime timeSystem = Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.ofOffset("", ZoneOffset.UTC)).toLocalTime();


        long minutes = ChronoUnit.MINUTES.between(timeRequest, timeSystem);

        log.info("Time difference between the client and server is " + minutes + " minutes");

        if (minutes > deviceTimeDiffAllowedInMinutes)
        {
            log.info("Nounce time is invalid because time difference between server and client is " + minutes + " minutes which is more than allowed time difference " + deviceTimeDiffAllowedInMinutes);
            throw new RouterException(RouterResponseCode.INVALID_NOUNCE_TIME, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Nounce time is invalid");
        }
    }


    private Map<String, String> decryptAuthorizationPayload(String requestId, String secret) throws RouterException {
        Map<String, String> authData = new HashMap<>();
        try {

            log.info(" === inside decryptAuthorizationPayload === ");
            List<String> data = RouterOperations.getStringArray(secret, ":");
            authData.put(Constants.ROUTER_HEADER_USERNAME, data.get(0));
            authData.put("auth-type", data.get(1));
            authData.put("nounce", data.get(2));
        } catch (Exception e) {
            log.info("Error while splitting data(:) for header secret.Secret is:" + secret);
            throw new RouterException(RouterResponseCode.HEADER_SECRET_VALIDATION_ERROR, e, Constants.ROUTER_ERROR_TYPE_SECURITY, "Error while splitting data(:) for header secret");
        }
        return authData;

    }

    private static final String PRIVATE_MODULUS_STR = "private-modules";
    private static final String PRIVATE_EXPONENT_STR = "private-exponent";

    @Override
    public String decryptTxnKey(Session session, String txnKey) throws RouterException {
        JsonObject json = RouterOperations.fetchRSADatafromSession (session);
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( session.getSecurityVersion (), txnKey );
        return iCryptoUtil.decryptAESKey ( txnKey, json.get ( PRIVATE_MODULUS_STR ).getAsString (), json.get ( PRIVATE_EXPONENT_STR ).getAsString () );
    }

    @Override
    public Object decryptRequestV1(String request, Map<String, String> httpHeaders) throws RouterException
    {

        String userName = httpHeaders.get(username.name());
        String requestId = httpHeaders.get(requestid.name());
        String securityVersion = httpHeaders.get(Constants.ROUTER_HEADER_SECURITY_VERSION);

        log.info("Decrypting request process has been initiated");

        String txnKey = httpHeaders.get("txnkey");

        log.info("Finding session for userName " + userName + " in redis");

        Optional<Session> session = authenticationSessionRepo.findById(userName);

        if(!session.isPresent())
        {
            log.info("Unable to decrypt the request because session is invalid for userName " + userName);
            throw new RouterException(RouterResponseCode.INVALID_USER_SESSION, (Exception) null, Constants.ROUTER_ERROR_TYPE_VALIDATION, "Session is invalid");
        }

        log.info("Session is found so fetching RSA keys from session");

        JsonObject json = RouterOperations.fetchRSADatafromSession ( session.get() );

        try
        {
            return cryptographyService.decryptRequest(securityVersion, json, txnKey, request);

        } catch (Exception e)
        {
            log.info("RSA Keys:" + json.toString());
            log.info("Account Data:" + httpHeaders);
            log.info("Request Data:" + request);
            log.info("Exception in function decryptRequestData" + e.getLocalizedMessage());

            if (e instanceof RouterException)
            {
                RouterException ex = (RouterException) e;
                log.info("Some error occurred when decrypting request " + ex.getErrorHint());
                throw e;
            }

            throw new RouterException(RouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Error when decryption data");
        }
    }

    @Autowired
    AuthenticationSession authenticationSession;

    @Override
    public String getAesKey(Map<String, String> httpHeaders) throws RouterException {
        String txnKey = httpHeaders.get(Constants.ROUTER_HEADER_TXN_KEY);

        String requestId = httpHeaders.get(Headers.requestid.name());
        String securityVersion = httpHeaders.get(Constants.ROUTER_HEADER_SECURITY_VERSION);

        String username = httpHeaders.get(Headers.username.name());

        Session session = authenticationSession.getSession(username);

        JsonObject json = RouterOperations.fetchRSADatafromSession(session);

        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion(securityVersion, requestId);

        return iCryptoUtil.decryptAESKey ( txnKey, json.get ( PRIVATE_MODULUS_STR ).getAsString (), json.get ( PRIVATE_EXPONENT_STR ).getAsString () );
    }
}
