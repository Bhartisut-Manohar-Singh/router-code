package decimal.apigateway.service.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.*;
import decimal.apigateway.domain.Session;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.repository.redis.AuthenticationSessionRepoRedis;
import decimal.apigateway.service.security.CryptoFactory;
import decimal.logs.filters.AuditTraceFilter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static decimal.apigateway.enums.Headers.*;

@Service
@Log
public class KeysGenerator
{
    @Value("${system.generated.key}")
    String systemKey;

    @Autowired
    CryptoFactory cryptoFactory;

    
    
    @Autowired
    TokenHandler tokenHandler;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    Map<String, Object> generateRsaKeys(String securityVesion, String requestId) throws RouterException
    {
        ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVesion, requestId );
        log.info("Processing authentication request " + requestId);

        return "2".equalsIgnoreCase(securityVesion) ? iCryptoUtil.getRSAKeys() : iCryptoUtil.getRSAKeys();
    }


    String encryptJWTToken(String securityVersion, String systemKey, String username, String requestId) throws RouterException {
        log.info("Encrypting JWT token" + requestId);

            ICryptoUtil iCryptoUtil = cryptoFactory.getSecurityVersion ( securityVersion, requestId );


           /* switch (securityVersion) {
                case "3":
                    System.out.println("=====================securityVersion is 3======================");
                    return iCryptoUtil.encryptTextUsingAES(username, systemKey);
                case "2":
                    System.out.println("=====================securityVersion is 2======================");
                    return CryptoUtilV2.encryptTextUsingAES(username, systemKey);
                default:
                    System.out.println("=====================securityVersion is "+securityVersion+"======================");
                    return CryptoUtil.encryptTextUsingAES(username, systemKey);
            }*/
        return iCryptoUtil.encryptTextUsingAES(username,systemKey);
    }

    String decryptJWTToken(String securityVersion, String systemKey, String encryptedJWTToken, String requestId) {
        return null;
    }

    Map<String, Object> generateTokenAndRsaKeys(Map<String, String> httpHeaders) throws RouterException {

        String securityVersion = httpHeaders.get("security-version");
        String requestId = httpHeaders.get(requestid.name());
        String userName = httpHeaders.get(username.name());

        Map<String, Object> rsaKeys = generateRsaKeys(securityVersion, requestId);

        String jwtToken = tokenHandler.build(securityVersion, systemKey, userName, requestId);

        rsaKeys.put("jwtToken", jwtToken);

        return rsaKeys;
    }

    @Autowired
    AuthenticationSessionRepoRedis authenticationSessionRepo;

    @Autowired
    ObjectMapper objectMapper;

    public Map<String, Object> getTokenAndRsaKeys(Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("Getting RSA keys and Jwt token");

        String requestId = httpHeaders.get(requestid.name());
        String userName = httpHeaders.get(username.name());

        String securityVersion = httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION);

        List<String> userNameData = AuthRouterOperations.getStringArray(userName, Constant.TILD_SPLITTER);

        StringJoiner applicationUser = new StringJoiner(Constant.TILD_SPLITTER);
        applicationUser.add(userNameData.get(0));
        applicationUser.add(userNameData.get(1));
        applicationUser.add(userNameData.get(3));

        Optional<Session> sessionOptional = authenticationSessionRepo.findById(applicationUser.toString());

        if(!sessionOptional.isPresent())
        {
            log.info("Invalid user session"+requestId);
            throw new RouterException(AuthRouterResponseCode.INVALID_USER_SESSION, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "User session is invalid");
        }

        Map<String, String> rsaKeys = sessionOptional.get().getSessionData();

        Map<String, Object> rsaKeyMap = objectMapper.readValue(rsaKeys.get("rsa"), new TypeReference<Map<String, Object>>(){});

        log.info("Generating JWT token for userName: " + userName);

        String jwtToken = tokenHandler.build(securityVersion, systemKey, userName, requestId);

        rsaKeyMap.put("jwtToken", jwtToken);
        rsaKeyMap.put("appJwtToken", sessionOptional.get().getJwtKey());

        log.info("RSA keys and jwt token has been generated successfully");
        return rsaKeyMap;

    }

    Map<String, Object> generatePublicTokenAndRsaKeys(Map<String, String> httpHeaders) throws RouterException {

        String securityVersion = httpHeaders.get("security-version");
        String requestId = httpHeaders.get(requestid.name());
        String clientid = httpHeaders.get(Constant.CLIENT_ID);

//        Map<String, Object> rsaKeys = generateRsaKeys(securityVersion, requestId);
        Map<String, Object> res = new HashMap<>();

        String jwtToken = tokenHandler.buildPublicToken(securityVersion, systemKey, clientid, requestId);

        res.put("jwtToken", jwtToken);

        return res;
    }
}
