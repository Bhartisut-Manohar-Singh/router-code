package decimal.apigateway.service.security;

import com.google.gson.JsonObject;
import decimal.apigateway.commons.ICryptoUtil;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.domain.Session;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Account;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Log
@Component
@SuppressWarnings("WeakerAccess")
public class CryptographyService implements Cryptography{

    @Autowired
    CryptoFactory cryptoFactory;

    private static final String PRIVATE_MODULUS_STR = "private-modules";
    private static final String PRIVATE_EXPONENT_STR = "private-exponent";

    @Override
    public Object encryptResponse (Session session, String requestId, String data) throws RouterException
    {

        log.info("Plain Response: " + data);
        log.info("Encryption processing");


        JsonObject json = RouterOperations.fetchRSADatafromSession ( session );
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( session.getSecurityVersion (), session.getRequestId () );
        String decryptedAESKey = iCryptoUtil.decryptAESKey ( requestId,
                json.get ( PRIVATE_MODULUS_STR ).getAsString (), json.get ( PRIVATE_EXPONENT_STR ).getAsString () );
        return iCryptoUtil.encryptTextUsingAES ( data, decryptedAESKey );
    }


    @Override
    public Object decryptRequest (Session session, String requestId, String data, String txnId) throws RouterException
    {
        log.info("Decryption of request from gateway URL");
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( session.getSecurityVersion (), requestId );
        JsonObject json = RouterOperations.fetchRSADatafromSession ( session );
        String decryptedAESKey = iCryptoUtil.decryptAESKey ( txnId, json.get ( PRIVATE_MODULUS_STR ).getAsString (), json.get ( PRIVATE_EXPONENT_STR ).getAsString () );

        return iCryptoUtil.decryptTextUsingAES ( data, decryptedAESKey );

    }

    @Override
    public String decryptTxnKey (Session session, String requestId) throws RouterException
    {
        log.info("Decryption processing");
        JsonObject json = RouterOperations.fetchRSADatafromSession ( session );
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( session.getSecurityVersion (), requestId );
        return iCryptoUtil.decryptAESKey ( requestId, json.get ( PRIVATE_MODULUS_STR ).getAsString (), json.get ( PRIVATE_EXPONENT_STR ).getAsString () );
    }

    @Override
    public Map<String, Object> generateKeys (String securityVersion, String requestId)
            throws RouterException {
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVersion, requestId );
        return iCryptoUtil.getRSAKeys ();
    }

    @Override
    public String getRequestHash (Session session, String requestId, String data)
            throws RouterException
    {
        log.info("Inside getRequestHash");

        JsonObject json = RouterOperations.fetchRSADatafromSession ( session );
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( session.getSecurityVersion (), requestId );

        String decryptedAESKey = iCryptoUtil.decryptAESKey ( requestId, json.get ( PRIVATE_MODULUS_STR ).getAsString (), json.get ( PRIVATE_EXPONENT_STR ).getAsString () );

        return iCryptoUtil.generateSHA512Hash ( data, decryptedAESKey );
    }

    @Override
    public Object decryptRequest (String securityVersion, JsonObject rsaData, String requestId, String data)
            throws RouterException {

        log.info("Decryption processing");

        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVersion, requestId );
        String decryptedAESKey = iCryptoUtil.decryptAESKey ( requestId, rsaData.get ( PRIVATE_MODULUS_STR ).getAsString (), rsaData.get ( PRIVATE_EXPONENT_STR ).getAsString () );

        log.info("Decrypted AES key: " + decryptedAESKey);

        return iCryptoUtil.decryptTextUsingAES ( data, decryptedAESKey );
    }


    @Override
    public Object encryptJWTToken (String securityVersion, String systemKey, String plainData, String requestId)
            throws RouterException {

        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVersion, requestId );
        return iCryptoUtil.encryptTextUsingAES ( plainData, systemKey );

    }


    @Override
    public Object decryptJWTToken (String securityVersion, String systemKey, String encryptedData, String requestId)
            throws RouterException
    {
        log.info("Decrypting JWT token");
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVersion, requestId );
        return iCryptoUtil.decryptTextUsingAES ( encryptedData, systemKey );
    }

    @Override
    public String getHeaderDataHash (String securityVersion, Account account, String data, String salt, String requestId) throws RouterException {

        log.info("Inside getRequestHash");
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVersion, requestId );
        return iCryptoUtil.generateSHA512Hash ( data, salt );
    }


}
