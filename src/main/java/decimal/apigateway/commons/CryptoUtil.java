package decimal.apigateway.commons;

import decimal.apigateway.exception.RouterException;
import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


// RSA-AES encryption/decryption.
// Uses strong encryption with 2048 key size.

@Service
@Log
public class CryptoUtil implements ICryptoUtil {

    @Autowired
    LogsConnector logsConnector;
    
    @Autowired
    AuditTraceFilter auditTraceFilter;

    private static final String INITIAL_VECTOR = "d1553cdbef4d0b8c";
    private static final int AES_KEY_LENGTH = 128;
    private static final String ENCODING_SCHEME = "UTF-8";


    private static final Logger logger = LogManager.getLogger ( CryptoUtilV2.class );


    // Create a new AES key. Uses 128 bit (weak)
    public String getSecretAESKeyAsString () throws RouterException {
        try {
            KeyGenerator generator = KeyGenerator.getInstance ( "AES" );
            generator.init ( AES_KEY_LENGTH ); // The AES key size in number of bits
            SecretKey secKey = generator.generateKey ();
            return Base64.getEncoder ().encodeToString ( secKey.getEncoded () );
        } catch (NoSuchAlgorithmException e) {
            throw new RouterException( AuthRouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, e );
        }
    }


    // Encrypt text using AES key
    public String encryptTextUsingAES(String plainText, String aesKeyString)
            throws RouterException {
        try {
            IvParameterSpec iv = new IvParameterSpec ( INITIAL_VECTOR.getBytes ( ENCODING_SCHEME ) );
            SecretKeySpec skeySpec = new SecretKeySpec ( aesKeyString.getBytes ( ENCODING_SCHEME ), "AES" );

            Cipher cipher = Cipher.getInstance ( "AES/CBC/PKCS5PADDING" );
            cipher.init ( Cipher.ENCRYPT_MODE, skeySpec, iv );

            byte[] encrypted = cipher.doFinal ( plainText.getBytes () );

            return convertByteArrayToHexString ( encrypted );

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchPaddingException |
                 BadPaddingException | InvalidKeyException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new RouterException( AuthRouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, e );
        }
    }

    // Decrypt text using AES key
    public String decryptTextUsingAES (String encryptedText, String aesKeyString)
            throws RouterException {
        try {
            IvParameterSpec iv = new IvParameterSpec ( INITIAL_VECTOR.getBytes ( ENCODING_SCHEME ) );
            SecretKeySpec skeySpec = new SecretKeySpec ( aesKeyString.getBytes ( ENCODING_SCHEME ), "AES" );

            Cipher cipher = Cipher.getInstance ( "AES/CBC/PKCS5PADDING" );
            cipher.init ( Cipher.DECRYPT_MODE, skeySpec, iv );

            byte[] original = cipher.doFinal ( hexStringToByteArray ( encryptedText.toCharArray () ) );

            return new String ( original );


        } catch (NoSuchPaddingException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | InvalidAlgorithmParameterException | UnsupportedEncodingException | NoSuchAlgorithmException e) {

            log.info(e.getMessage()+":"+e);
            throw new RouterException( AuthRouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, e , Constant.ROUTER_ERROR_TYPE_SECURITY, null);
        }
    }


    public Map<String, Object> getRSAKeys()
            throws RouterException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance ( "RSA" );
            SecureRandom random = SecureRandom.getInstance ( "SHA1PRNG", "SUN" );
            keyGen.initialize ( 1024, random );
            KeyPair keyPair = keyGen.generateKeyPair ();
            KeyFactory fact = KeyFactory.getInstance ( "RSA" );
            RSAPublicKeySpec publicSpec = fact.getKeySpec ( keyPair.getPublic (), RSAPublicKeySpec.class );
            RSAPrivateKeySpec privateSpec = fact.getKeySpec ( keyPair.getPrivate (), RSAPrivateKeySpec.class );
            Map<String, Object> keys = new HashMap<> ();
            keys.put ( "private-modules", String.valueOf ( privateSpec.getModulus () ) );
            keys.put ( "private-exponent", String.valueOf ( privateSpec.getPrivateExponent () ) );
            keys.put ( "public-modules", String.valueOf ( publicSpec.getModulus () ) );
            keys.put ( "public-exponent", String.valueOf ( publicSpec.getPublicExponent () ) );

            StringBuilder publicPem = new StringBuilder ( "-----BEGIN PUBLIC KEY-----" );
            publicPem.append ( "\n" );
            publicPem.append ( Base64.getMimeEncoder ().encodeToString ( keyPair.getPublic ().getEncoded () ) );
            publicPem.append ( "\n" );
            publicPem.append ( "-----END PUBLIC KEY-----" );
            keys.put ( "public-pem", publicPem );

            return keys;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            logger.error(e.getMessage(), e);
            throw new RouterException( AuthRouterResponseCode.ERROR_GENERATING_RSA_KEYS, e, Constant.ROUTER_ERROR_TYPE_SECURITY, "Error when generating RSA keys" );
        }
    }
    // Decrypt AES Key using RSA public key

    public String decryptAESKey (String encryptedAESKey, String privateModules,
                                 String privateExponent) throws RouterException {
        try {
            Cipher cipher = Cipher.getInstance ( "RSA/ECB/PKCS1Padding" );
            PrivateKey privateKey = getPrivateKey ( privateModules, privateExponent );
            cipher.init ( Cipher.DECRYPT_MODE, privateKey );
            return new String ( cipher.doFinal ( hexStringToByteArray ( encryptedAESKey.toCharArray () ) ) );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
            throw new RouterException( AuthRouterResponseCode.TXN_ID_DECRYPTION_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, "Error when decrypting AES key" );
        }
    }

    public String encryptAESKey (String encryptedAESKey, String privateModules,
                                 String privateExponent) throws RouterException {
        try {
            Cipher cipher = Cipher.getInstance ( "RSA/ECB/PKCS1Padding" );
            PublicKey publicKey = getPublicKey ( privateModules, privateExponent );
            cipher.init ( Cipher.ENCRYPT_MODE, publicKey );
            return convertByteArrayToHexString ( cipher.doFinal ( encryptedAESKey.getBytes () ) );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
            throw new RouterException( AuthRouterResponseCode.TXN_ID_DECRYPTION_ERROR, e );
        }
    }

    public String generateSHA512Hash (String data, String salt)
            throws RouterException {
        try {
            MessageDigest md = MessageDigest.getInstance ( "SHA-512" );
            md.update ( salt.getBytes ( ENCODING_SCHEME ) );
            byte[] bytes = md.digest ( data.getBytes ( ENCODING_SCHEME ) );
            StringBuilder sb = new StringBuilder ();
            for (byte aByte : bytes) {
                sb.append ( Integer.toString ( (aByte & 0xff) + 0x100, 16 ).substring ( 1 ) );
            }
            return sb.toString ();

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RouterException( AuthRouterResponseCode.TXN_ID_DECRYPTION_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, null );
        }


    }

    private PrivateKey getPrivateKey (String priModulus, String priExponent)
            throws NoSuchAlgorithmException, InvalidKeySpecException{
        RSAPrivateKeySpec keySpec =
                new RSAPrivateKeySpec ( new BigInteger ( priModulus ), new BigInteger ( priExponent ) );
        KeyFactory fact = KeyFactory.getInstance ( "RSA" );
        return fact.generatePrivate ( keySpec );

    }

    private PublicKey getPublicKey (String priModulus, String priExponent)
            throws InvalidKeySpecException, NoSuchAlgorithmException{
        RSAPublicKeySpec keySpec =
                new RSAPublicKeySpec ( new BigInteger ( priModulus ), new BigInteger ( priExponent ) );
        KeyFactory fact = KeyFactory.getInstance ( "RSA" );
        return fact.generatePublic ( keySpec );

    }


    public static byte[] hexStringToByteArray (char[] data){
        byte[] out = null;
        try {
            int len = data.length;

            out = new byte[len >> 1];

            // two characters form the hex value.
            for (int i = 0, j = 0; j < len; i++) {
                int f = toDigit ( data[j] ) << 4;
                j++;
                f = f | toDigit ( data[j] );
                j++;
                out[i] = (byte) (f & 0xFF);
            }
        } catch (Exception ex) {
            logger.error ( ex.getMessage(), ex );
        }

        return out;
    }

    private static int toDigit (char ch){
        int digit = 0;
        try {
            digit = Character.digit ( ch, 16 );
        } catch (Exception ex) {
            logger.error ( ex );
        }
        return digit;
    }

    private static String convertByteArrayToHexString (byte[] tempArray){

        int v;
        try {
            if (tempArray == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder ( tempArray.length * 2 );

            for (byte aTempArray : tempArray) {
                v = aTempArray & 0xff;

                if (v < 16) {
                    sb.append ( '0' );
                }
                sb.append ( Integer.toHexString ( v ) );

            }
            return sb.toString ();
        } catch (Exception e) {
            logger.error ( e );
        }
        return null;
    }
}

