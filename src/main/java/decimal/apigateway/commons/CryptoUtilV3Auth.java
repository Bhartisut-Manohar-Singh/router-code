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
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Log
public class CryptoUtilV3Auth {

    private static final Logger logger = LogManager.getLogger(CryptoUtilV3Auth.class);
    private static final String ENCODING_SCHEME = "UTF-8";
    @Autowired
    LogsConnector logsConnector;

    @Autowired
    AuditTraceFilter auditTraceFilter;

  /*  @Override
    public String getSecretAESKeyAsString() throws RouterException {
        return null;
    }*/

    private static byte[] hexStringToByteArray(char[] data) {
        byte[] out = null;
        try {
            int len = data.length;

            out = new byte[len >> 1];

            // two characters form the hex value.
            for (int i = 0, j = 0; j < len; i++) {
                int f = toDigit(data[j]) << 4;
                j++;
                f = f | toDigit(data[j]);
                j++;
                out[i] = (byte) (f & 0xFF);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        return out;
    }

    private static int toDigit(char ch) {
        int digit = 0;
        try {
            digit = Character.digit(ch, 16);
        } catch (Exception ex) {
            logger.error(ex);
        }
        return digit;
    }

    private static String convertByteArrayToHexString(byte[] tempArray) {

        int v;
        try {
            if (tempArray == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder(tempArray.length * 2);

            for (byte aTempArray : tempArray) {
                v = aTempArray & 0xff;

                if (v < 16) {
                    sb.append('0');
                }
                sb.append(Integer.toHexString(v));

            }
            return sb.toString();
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    private static String base64(byte[] bytes) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
    }

    private static byte[] base64(String str) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(str);
    }

    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Function to generate a 128 bit key from the given password and iv
     *
     * @param password
     * @param iv
     * @return Secret key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static SecretKey generateSecretKey(String password, byte[] iv) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), iv, 65536, 128); // AES-128
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    public static String encryptTextUsingAES(String plainText, String aesKeyString) throws RouterException {

        try {
            //   System.out.println("Encrypting data using V3;plainText="+plainText+";aesKeyString="+aesKeyString);
            System.out.println("==============================================Encrypting data using V3;plainText=" + plainText + ";aesKeyString=" + aesKeyString + ";=======================================================================");

            //Prepare the nonce
            SecureRandom secureRandom = new SecureRandom();

            //Noonce should be 12 bytes
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            //Prepare your key/password
            SecretKey secretKey = generateSecretKey(aesKeyString, iv);


            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

            //Encryption mode on!
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            //Encrypt the data
            byte[] encryptedData = cipher.doFinal(plainText.getBytes());

            //Concatenate everything and return the final data
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);
            return base64(byteBuffer.array());
        } catch (Exception e) {
            throw new RouterException(AuthRouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, e);
        }
    }

    public String decryptTextUsingAES(String encryptedText, String aesKeyString) throws RouterException {

        System.out.println("==============================================Decrypting data using V3;encryptedText=" + encryptedText + ";aesKeyString=" + aesKeyString + ";=======================================================================");

        try {
            //Wrap the data into a byte buffer to ease the reading process
            ByteBuffer byteBuffer = ByteBuffer.wrap(base64(encryptedText));

            int noonceSize = byteBuffer.getInt();

            //Make sure that the file was encrypted properly
            if (noonceSize < 12 || noonceSize >= 16) {
                throw new IllegalArgumentException("Nonce size is incorrect. Make sure that the incoming data is an AES encrypted file.");
            }
            byte[] iv = new byte[noonceSize];
            byteBuffer.get(iv);

            //Prepare your key/password
            SecretKey secretKey = generateSecretKey(aesKeyString, iv);

            //get the rest of encrypted data
            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

            //Encryption mode on!
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            //Encrypt the data
            return new String(cipher.doFinal(cipherBytes), ENCODING_SCHEME);
        } catch (Exception e) {
            log.info(e.getMessage() + e);

            throw new RouterException(AuthRouterResponseCode.REQUEST_DATA_DECRYPTION_ERROR, e, ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, null);
        }
    }


    public Map<String, Object> getRSAKeys()
            throws RouterException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            KeyFactory fact = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec publicSpec = fact.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec privateSpec = fact.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);
            Map<String, Object> keys = new HashMap<>();
            keys.put("private-modules", String.valueOf(privateSpec.getModulus()));
            keys.put("private-exponent", String.valueOf(privateSpec.getPrivateExponent()));
            keys.put("public-modules", String.valueOf(publicSpec.getModulus()));
            keys.put("public-exponent", String.valueOf(publicSpec.getPublicExponent()));

            StringBuilder publicPem = new StringBuilder("-----BEGIN PUBLIC KEY-----");
            publicPem.append("\n");
            publicPem.append(Base64.getMimeEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            publicPem.append("\n");
            publicPem.append("-----END PUBLIC KEY-----");
            keys.put("public-pem", publicPem);

            return keys;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            log.info(e.getMessage() + e);
            throw new RouterException(AuthRouterResponseCode.ERROR_GENERATING_RSA_KEYS, e, ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, "Error when generating RSA keys");
        }
    }


    public String decryptAESKey(String encryptedAESKey, String privateModules,
                                String privateExponent) throws RouterException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            PrivateKey privateKey = getPrivateKey(privateModules, privateExponent);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(hexStringToByteArray(encryptedAESKey.toCharArray())));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
            throw new RouterException(AuthRouterResponseCode.TXN_ID_DECRYPTION_ERROR, e, ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, "Error when decrypting AES key");
        }
    }

    public String encryptAESKey(String encryptedAESKey, String privateModules,
                                String privateExponent) throws RouterException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            PublicKey publicKey = getPublicKey(privateModules, privateExponent);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return convertByteArrayToHexString(cipher.doFinal(encryptedAESKey.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException e) {
            throw new RouterException(AuthRouterResponseCode.TXN_ID_DECRYPTION_ERROR, e);
        }
    }

    public String generateSHA512Hash(String data, String salt)
            throws RouterException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(ENCODING_SCHEME));
            byte[] bytes = md.digest(data.getBytes(ENCODING_SCHEME));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RouterException(AuthRouterResponseCode.TXN_ID_DECRYPTION_ERROR, e, ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, null);
        }
    }

    private PrivateKey getPrivateKey(String priModulus, String priExponent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateKeySpec keySpec =
                new RSAPrivateKeySpec(new BigInteger(priModulus), new BigInteger(priExponent));
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePrivate(keySpec);

    }

    private PublicKey getPublicKey(String priModulus, String priExponent)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        RSAPublicKeySpec keySpec =
                new RSAPublicKeySpec(new BigInteger(priModulus), new BigInteger(priExponent));
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(keySpec);

    }

}
