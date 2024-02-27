package decimal.apigateway.commons;

import decimal.apigateway.exception.RouterException;


import javax.crypto.SecretKey;
import java.util.Map;

public interface ICryptoUtil {
   // String getSecretAESKeyAsString() throws RouterException;
    default SecretKey getSecretAESKeyAsString(String salt, String passphrase) throws RouterException{
        return null;
    }
    String encryptTextUsingAES(String plainText, String aesKeyString) throws RouterException;
    String decryptTextUsingAES(String encryptedText, String aesKeyString) throws RouterException;
    Map<String, Object> getRSAKeys() throws RouterException;
    String decryptAESKey(String encryptedAESKey, String privateModules, String privateExponent) throws RouterException;
    String encryptAESKey(String encryptedAESKey, String privateModules, String privateExponent) throws RouterException;
    String generateSHA512Hash(String data, String salt) throws RouterException;
}
