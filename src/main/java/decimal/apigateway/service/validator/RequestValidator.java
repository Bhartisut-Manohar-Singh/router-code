package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.Session;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.security.AuthenticationSession;
import decimal.apigateway.service.security.CryptographyService;
import decimal.logs.model.RequestIdentifier;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.java.Log;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Log
@Service
public class RequestValidator implements Validator {
    @Value("${system.generated.key}")
    String systemKey;

    @Autowired
    CryptographyService cryptographyService;

    @Autowired
    AuthenticationSession authenticationSession;

    @Autowired
    Request auditTraceFilter;

    public static final String TOKEN_PREFIX = "Bearer "; // the prefix of the token in the http header
    public static final String HEADER_STRING = "authorization"; // the http header containing the

    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException
    {
        RequestIdentifier requestIdentifier = auditTraceFilter.getRequestIdentifier(auditTraceFilter);

        String authorizationToken = httpHeaders.get(HEADER_STRING);
        String requestId = httpHeaders.get(Headers.requestid.name());
        System.out.println("authorizationToken:"+authorizationToken);

        String securityVersion = httpHeaders.get(Constant.ROUTER_HEADER_SECURITY_VERSION);

        log.info("Validating  request authorization token " );

        if (authorizationToken == null || !authorizationToken.startsWith(TOKEN_PREFIX)) {
            log.info("Error in processing request because of invalid authorization token found in the request");

            throw new RouterException(RouterResponseCode.ERROR_IN_PROCESSING_REQUEST, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, "Error in processing request because of invalid authorization token found in the request");
        }

        String encryptedJWTToken;

        try {
            String token = authorizationToken.replace(TOKEN_PREFIX, "");
            System.out.println("token:"+token);


            encryptedJWTToken = Jwts.parser().setSigningKey(systemKey).parseClaimsJws(token).getBody().getSubject();
            System.out.println("token after parsing:"+encryptedJWTToken);
        } catch (ExpiredJwtException e) {
            log.info("JWT Token is expired. Token is:" + authorizationToken+" Exception: "+e);

            Object username = cryptographyService.decryptJWTToken(securityVersion, systemKey, e.getClaims().getSubject(), requestId);

            List<String> userNameData = RouterOperations.getStringArray(username.toString(), Constant.TILD_SPLITTER);
            if (userNameData.size() > 3) {
                checkApplicationSessionExpiry(username.toString(), requestId);

            } else {
                authenticationSession.removeSession(username.toString());
            }
            // If App session is not valid then throw App Authentication failure
            throw new RouterException(RouterResponseCode.APP_AUTHENTICATION_FAILURE, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, "JWT token has been expired");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("=============="+RouterResponseCode.JWT_PARSING_ERROR+"=================");
            log.info("Error while parsing Bearer JWT Token. Token is:" + authorizationToken+" Exception: "+e);
            throw new RouterException(RouterResponseCode.JWT_PARSING_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, "It seems that there is some error when parsing JWT token. Check your token if it is valid or not");
        }

        log.info("JWT Token is parsed. Let Decrypt it now");
        System.out.println("Encrypted JWT Token:"+encryptedJWTToken);

        MicroserviceResponse response = new MicroserviceResponse();
        try {
            Object user = cryptographyService.decryptJWTToken(securityVersion, systemKey, encryptedJWTToken, requestId).toString();
            response.setResponse(user);

            System.out.println(user);
        } catch (Exception e) {
            System.out.println("=============="+RouterResponseCode.JWT_DECRYPTION_ERROR+"=================");
            e.printStackTrace();
            log.info("Error while decrypting Bearer JWT Token. Token is:" + encryptedJWTToken+" Exception: "+ e);
            throw new RouterException(RouterResponseCode.JWT_DECRYPTION_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, "There is some error in decrypting JWT token");
        }

        log.info("Validating authorization token is success: ");

        return response;
    }
    private void checkApplicationSessionExpiry(String username, String requestId) throws RouterException {
        try {

            List<String> userNameData = RouterOperations.getStringArray(username, Constant.TILD_SPLITTER);

            authenticationSession.removeSession(username);

            String applicationUser =  RouterOperations.getJoiningString(Constant.TILD_SPLITTER, userNameData.get(0), userNameData.get(1), userNameData.get(3));

            Session session = authenticationSession.getSession(applicationUser);

            if(session != null)
            {
                String encryptedJWTToken = Jwts.parser().setSigningKey(systemKey).parseClaimsJws(session.getAppJwtKey()).getBody().getSubject();
            }

            throw new RouterException(RouterResponseCode.USER_AUTHENTICATION_FAILURE, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, "Not able to find user session");
        } catch (ExpiredJwtException ae) {
            log.info("Exception for authentication in app level session check."+ae);

            // Fetching App session stratergy data to remove session
            List<String> userNameData = RouterOperations.getStringArray(username, "~");
            String applicationUser = RouterOperations.getJoiningString(Constant.TILD_SPLITTER, userNameData.get(0), userNameData.get(1), userNameData.get(3));

            authenticationSession.removeSession(applicationUser);

        } catch (RouterException e1) {
            log.info("Exception for authentication in app level session check."+e1);
            throw e1;

        } catch (Exception e1) {
            log.info("Exception for authentication in app level session check."+ e1);
            throw new RouterException(RouterResponseCode.ERROR_IN_PROCESSING_REQUEST, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, "Exception for authentication in app level session");
        }
    }

}
