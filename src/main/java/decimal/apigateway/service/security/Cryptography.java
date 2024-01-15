package decimal.apigateway.service.security;

import com.google.gson.JsonObject;
import decimal.apigateway.domain.Session;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Account;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public interface Cryptography{

    public Object encryptResponse(Session session, String requestId, String data)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, RouterException;

    public Object decryptRequest(Session session, String requestId, String data, String txnId)
            throws RouterException;

    public Map<String, Object> generateKeys(String securityVersion, String requestId)
            throws RouterException;

    public String getRequestHash(Session session, String requestId, String data)
            throws RouterException;

    public Object decryptRequest(String securityVersion, JsonObject rsaData, String requestId, String data)
            throws RouterException;

    public Object encryptJWTToken(String securityVersion, String systemKey, String plainData, String requestId)
            throws RouterException;

    public Object decryptJWTToken(String securityVersion, String systemKey, String encryptedData, String requestId)
            throws RouterException;

    public String getHeaderDataHash(String securityVersion, Account account, String data, String salt, String requestId)
            throws RouterException;

    public String decryptTxnKey(Session session, String requestId)
            throws RouterException;

}
