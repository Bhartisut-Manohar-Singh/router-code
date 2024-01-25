package decimal.apigateway.commons;

import decimal.apigateway.exception.RouterException;
import decimal.logs.filters.AuditTraceFilter;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


// RSA-AES encryption/decryption.
// Uses strong encryption with 2048 key size.
@Service
@Log
public class CryptoUtilV2 implements ICryptoUtil{

    @Autowired
    AuditTraceFilter auditTraceFilter;
    

    private static final int AES_KEY_LENGTH = 256;
    private static final String ENCODING_SCHEME = "UTF-8";
    private static final String AES = "AES";
    private static final String AES_PADDING = "AES/CBC/PKCS5Padding";
    private static final String PBKDF2WithHmacSHA1 = "PBKDF2WithHmacSHA1";
    private static final String SALT = "00000000000000000000000000000000";
    private static final String INITIAL_VECTOR = "00000000000000000000000000000000";
    private static final int AES_KEY_GENERATION_ITERATION_COUNT = 100;


    private static final Logger logger = LogManager.getLogger ( CryptoUtilV2.class );


    // Create a new AES key. Uses 128 bit (weak)
    public String getSecretAESKeyAsString () throws RouterException {
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance ( "AES" );
            generator.init ( AES_KEY_LENGTH ); // The AES key size in number of bits
            SecretKey secKey = generator.generateKey ();
            return Base64.getEncoder ().encodeToString ( secKey.getEncoded () );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return null;
    }

    public SecretKey getSecretAESKeyAsString(String salt, String passphrase) throws RouterException {

        SecretKeyFactory factory = null;
        SecretKey key = null;

        try {
            factory = SecretKeyFactory.getInstance ( PBKDF2WithHmacSHA1 );
            KeySpec spec = new PBEKeySpec ( passphrase.toCharArray (), hexStringToByteArray ( salt.toCharArray () ), AES_KEY_GENERATION_ITERATION_COUNT, AES_KEY_LENGTH );
            key = new SecretKeySpec ( factory.generateSecret ( spec ).getEncoded (), AES );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace ();
        }
        return key;
    }

    // Encrypt text using AES key
    public String encryptTextUsingAES(String plainText, String aesKeyString)
            throws RouterException {

        SecretKey key = null;
        byte[] encrypted = new byte[0];
        try {
            key = getSecretAESKeyAsString ( SALT, aesKeyString );
            encrypted = doFinal ( Cipher.ENCRYPT_MODE, key, INITIAL_VECTOR, plainText.getBytes ( ENCODING_SCHEME ) );
            return base64 ( encrypted );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException e) {
            throw new RouterException( AuthRouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, e );

        }
    }

    // Decrypt text using AES key
    public String decryptTextUsingAES (String encryptedText, String aesKeyString)
            throws RouterException {

        SecretKey key = getSecretAESKeyAsString ( SALT, aesKeyString );
        byte[] sd = base64 ( encryptedText );
        byte[] decrypted = new byte[0];
        try {
            decrypted = doFinal ( Cipher.DECRYPT_MODE, key, INITIAL_VECTOR, sd );
            return new String ( decrypted, ENCODING_SCHEME );
        } catch (NoSuchPaddingException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
           log.info(e.getMessage()+":"+e);
            throw new RouterException( AuthRouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, null);
        }
    }


    public Map<String, Object> getRSAKeys()
            throws RouterException {

        KeyPairGenerator keyGen = null;
        Map<String, Object> keys = null;
        try {
            keyGen = KeyPairGenerator.getInstance ( "RSA" );
            SecureRandom random = SecureRandom.getInstance ( "SHA1PRNG", "SUN" );
            keyGen.initialize ( 2048, random );
            KeyPair keyPair = keyGen.generateKeyPair ();
            KeyFactory fact = KeyFactory.getInstance ( "RSA" );
            RSAPublicKeySpec publicSpec = fact.getKeySpec ( keyPair.getPublic (), RSAPublicKeySpec.class );
            RSAPrivateKeySpec privateSpec = fact.getKeySpec ( keyPair.getPrivate (), RSAPrivateKeySpec.class );

            keys = new HashMap<> ();
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
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e)
        {
            logger.error(e.getMessage(), e);
            throw new RouterException( AuthRouterResponseCode.ERROR_GENERATING_RSA_KEYS, e , Constant.ROUTER_ERROR_TYPE_SECURITY, "Error in generating RSA keys");
        }
        return keys;

    }
    // Decrypt AES Key using RSA public key

    public String decryptAESKey (String encryptedAESKey, String privateModules,
                                 String privateExponent)
            throws RouterException {

        Cipher cipher;
        try {
            cipher = Cipher.getInstance ( "RSA/ECB/PKCS1Padding" );
            PrivateKey privateKey = getPrivateKey ( privateModules, privateExponent );
            cipher.init ( Cipher.DECRYPT_MODE, privateKey );
            return new String ( cipher.doFinal ( hexStringToByteArray ( encryptedAESKey.toCharArray () ) ) );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
            throw new RouterException( AuthRouterResponseCode.TXN_ID_DECRYPTION_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, "Error when decrypting AES key" );
        }

    }

    public String encryptAESKey (String encryptedAESKey, String privateModules,
                                 String privateExponent) throws RouterException {

        Cipher cipher;
        try {
            cipher = Cipher.getInstance ( "RSA/ECB/PKCS1Padding" );
            PublicKey publicKey = getPublicKey ( privateModules, privateExponent );
            cipher.init ( Cipher.ENCRYPT_MODE, publicKey );
            return convertByteArrayToHexString ( cipher.doFinal ( encryptedAESKey.getBytes () ) );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
            log.info(e.getMessage()+":"+e);
        }
        return null;

    }

    public String generateSHA512Hash (String data, String salt)
            throws RouterException {
        Mac sha512_HMAC = null;
        String result = null;

        try {
            byte[] byteKey = salt.getBytes ( "UTF-8" );
            final String HMAC_SHA512 = "HmacSHA512";
            sha512_HMAC = Mac.getInstance ( HMAC_SHA512 );
            SecretKeySpec keySpec = new SecretKeySpec ( byteKey, HMAC_SHA512 );
            sha512_HMAC.init ( keySpec );
            byte[] mac_data = sha512_HMAC.
                    doFinal ( data.getBytes ( "UTF-8" ) );
            //result = Base64.encode(mac_data);
            result = bytesToHex ( mac_data );
            return result;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RouterException( AuthRouterResponseCode.HMAC_DECRYPTION_ERROR, e , Constant.ROUTER_ERROR_TYPE_SECURITY, null);
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


    private static byte[] hexStringToByteArray (char[] data){
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
            logger.error ( ex );
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


    private static byte[] doFinal (int encryptMode, SecretKey key, String iv, byte[] bytes) throws NoSuchPaddingException, NoSuchAlgorithmException{
        Cipher cipher = Cipher.getInstance ( AES_PADDING );
        byte[] ivArray = hexStringtoByteArray ( iv );
        try {
            cipher.init ( encryptMode, key, new IvParameterSpec ( ivArray ) );
            return cipher.doFinal ( bytes );

        } catch (InvalidKeyException e) {
            e.printStackTrace ();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace ();
        } catch (BadPaddingException e) {
            e.printStackTrace ();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace ();
        }
        return null;
    }


    private static String base64 (byte[] bytes){
        return org.apache.commons.codec.binary.Base64.encodeBase64String ( bytes );
    }

    private static byte[] base64 (String str){
        return org.apache.commons.codec.binary.Base64.decodeBase64 ( str );
    }

    private static byte[] hexStringtoByteArray (String str){
        int len = str.length ();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit ( str.charAt ( i ), 16 ) << 4) + Character.digit ( str.charAt ( i + 1 ), 16 ));
        }
        return data;
    }

    private static String bytesToHex (byte[] bytes){
        final char[] hexArray = "0123456789ABCDEF".toCharArray ();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String ( hexChars );
    }


}

