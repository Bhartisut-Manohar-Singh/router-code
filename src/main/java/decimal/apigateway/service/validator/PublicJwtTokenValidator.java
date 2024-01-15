package decimal.apigateway.service.validator;

import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.commons.Constants;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.security.CryptographyService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.service.validator.RequestValidator.HEADER_STRING;
import static decimal.apigateway.service.validator.RequestValidator.HEADER_STRING;
import static decimal.apigateway.service.validator.RequestValidator.TOKEN_PREFIX;

@Service
@Log
public class PublicJwtTokenValidator implements Validator{
    @Value("${system.generated.key}")
    String systemKey;

    @Autowired
    CryptographyService cryptographyService;

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        String authorizationToken = httpHeaders.get(HEADER_STRING);
        String requestId = httpHeaders.get(Headers.requestid.name());
        System.out.println("authorizationToken:"+authorizationToken);

        log.info("public authorizationToken:"+ authorizationToken);

        String securityVersion = httpHeaders.get(Constants.ROUTER_HEADER_SECURITY_VERSION);

        if (authorizationToken == null || !authorizationToken.startsWith(TOKEN_PREFIX)) {
            throw new RouterException(RouterResponseCode.ERROR_IN_PROCESSING_REQUEST, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Error in processing request because of invalid authorization token found in the request");
        }

        String encryptedJWTToken;

        try {
            String token = authorizationToken.replace(TOKEN_PREFIX, "");
            log.info("public token:"+token);


            encryptedJWTToken = Jwts.parser().setSigningKey(systemKey).parseClaimsJws(token).getBody().getSubject();
            System.out.println("public token after parsing: " + encryptedJWTToken);
        } catch (ExpiredJwtException e) {
            throw new RouterException(RouterResponseCode.APP_AUTHENTICATION_FAILURE, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "JWT token has been expired");
        } catch (Exception e) {
            System.out.println("=============="+RouterResponseCode.JWT_PARSING_ERROR+"=================");
            throw new RouterException(RouterResponseCode.JWT_PARSING_ERROR, e, Constants.ROUTER_ERROR_TYPE_SECURITY, "It seems that there is some error when parsing JWT token. Check your token if it is valid or not");
        }

        log.info("Encrypted Public JWT Token:"+encryptedJWTToken);

        MicroserviceResponse response = new MicroserviceResponse();
        try {
            Object user = cryptographyService.decryptJWTToken(securityVersion, systemKey, encryptedJWTToken, requestId).toString();
            response.setResponse(user);

            log.info("decrypted public jwt token - " + user);
        } catch (Exception e) {
            System.out.println("=============="+RouterResponseCode.JWT_DECRYPTION_ERROR+"=================");
            e.printStackTrace();
            throw new RouterException(RouterResponseCode.JWT_DECRYPTION_ERROR, e, Constants.ROUTER_ERROR_TYPE_SECURITY, "There is some error in decrypting JWT token");
        }

        return response;
    }
}
