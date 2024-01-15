package decimal.apigateway.service.authentication;

import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.AuthRouterResponseCode;
import decimal.apigateway.commons.ConstantsAuth;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Account;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.service.AuthApplicationDefConfig;
import decimal.logs.filters.AuditTraceFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@Log
@SuppressWarnings("WeakerAccess")
public class TokenHandler {

    @Value("${system.generated.key}")
    String systemKey;

    // final String secret = "V66BX3Z0Zjy82pFQqZfIjdBG1ccRwjqN"; // private key, better read it from
    // an external file
    public static final String TOKEN_PREFIX = "Bearer"; // the prefix of the token in the http header
    public static final String HEADER_STRING = "Authorization"; // the http header containing the


    
    @Autowired
    KeysGenerator cryptographyService;

    @Autowired
    AuthApplicationDefConfig applicationDefConfig;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    /**
     * Generate a token from the username.
     *
     * @return The generated token.
     * @throws RouterException
     */
    public String build (String securityVersion, Account account) throws RouterException {
        String username = account.getUsername ();

        Date sessionExpiry = fetchExpiryTimeDate(account.getUsername(), account.getApplicationDef());
       log.info("JWT Token creation for user:" + username + " with expiry date:" + sessionExpiry+":"+account.getRequestId());

        Date now = new Date ();
        String jwtToken ;
        try {
            jwtToken = Jwts.builder ().setId ( UUID.randomUUID ().toString () )
                    .setSubject (
                            cryptographyService.encryptJWTToken ( securityVersion, systemKey, account.getUsername (), account.getRequestId () ).toString () )
                    .setIssuedAt ( now )
                    .setExpiration ( sessionExpiry )
                    .signWith ( SignatureAlgorithm.HS512, systemKey ).compact ();
        } catch (Exception e)
        {
            log.info("Unable to create a new JWT token because of: " + e.getMessage()+":"+account.getRequestId()+":"+e);
            throw new RouterException( AuthRouterResponseCode.JWT_CREATION_ERROR, e, ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, "Unable to create a JWT token" );

        }
        return jwtToken;
    }

    /**
     * Parse a token and extract the subject (username).
     *
     * @param token A token to parse.
     * @return The subject (username) of the token.
     */
    public String parse (String securityVersion, String requestId, String token) throws RouterException {
        String encryptedJWTToken;

        try {
            encryptedJWTToken = Jwts.parser ().setSigningKey ( systemKey ).parseClaimsJws ( token.replace ( TOKEN_PREFIX, "" ) ).getBody ().getSubject ();
        } catch (ExpiredJwtException e) {
            log.info("JWT Token is expired. Token is:" + token+":"+requestId+":"+e);
            throw e;
        } catch (Exception e) {
            log.info("Error while parsing Bearer JWT Token. Token is:" + token+":"+requestId+":"+e);
            throw new RouterException( AuthRouterResponseCode.JWT_PARSING_ERROR, e , ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, "It seems that there is some error when parsing JWT token. Check your token if it is valid or not");
        }
        log.info("JWT Token is parsed. Let Decrypt it now"+requestId);

        try {
            return cryptographyService.decryptJWTToken ( securityVersion, systemKey, encryptedJWTToken, requestId ).toString ();
        } catch (Exception e) {

            log.info("Error while decrypting Bearer JWT Token. Token is:" + encryptedJWTToken+":"+requestId+":"+e);
            throw new RouterException( AuthRouterResponseCode.JWT_DECRYPTION_ERROR, e , ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, "There is some error in decrypting JWT token");
        }
    }

    public Long fetchExpiryTime (String userName, ApplicationDef applicationDef) throws RouterException {
        List<String> userNameData = AuthRouterOperations.getStringArray ( userName, "~" );

        Long expiryTime = userNameData.size () > 3 ? applicationDef.getUserSessionExpiryTime () : applicationDef.getAppSessionExpiryTime ();

        if (expiryTime == null || expiryTime == 0) {
            throw new RouterException( AuthRouterResponseCode.INVALID_SESSION_EXPIRY_PARAMETERS, null );
        }

        return userNameData.size () > 3 ? AuthRouterOperations.getSessionExpiryInMinutes ( expiryTime ) : AuthRouterOperations.getSessionExpiryInDays ( expiryTime );
    }

    public Date fetchExpiryTimeDate (String userName, ApplicationDef applicationDef) throws RouterException {
        List<String> userNameData = AuthRouterOperations.getStringArray ( userName, "~" );

        Long expiryTime = userNameData.size () > 3 ? applicationDef.getUserSessionExpiryTime () : applicationDef.getAppSessionExpiryTime ();

        if (expiryTime == null || expiryTime == 0) {
            throw new RouterException( AuthRouterResponseCode.INVALID_SESSION_EXPIRY_PARAMETERS, null );
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));

        if(userNameData.size() > 3)
            calendar.add(Calendar.MINUTE, expiryTime.intValue());
        else
            calendar.add(Calendar.DAY_OF_MONTH, expiryTime.intValue());

        return calendar.getTime();
    }

    public String build(String securityVersion, String systemKey, String userName, String requestId) throws RouterException {

        List<String> userNameData = AuthRouterOperations.getStringArray(userName, ConstantsAuth.TILD_SPLITTER);

        ApplicationDef applicationDef = applicationDefConfig.findByOrgIdAndAppId(userNameData.get(0), userNameData.get(1));

        Date sessionExpiry = fetchExpiryTimeDate(userName, applicationDef);

       log.info("JWT Token creation for user:" + userName + " with expiry date:" + sessionExpiry+":"+requestId);

        Date now = new Date ();
        String jwtToken ;
        try {
            jwtToken = Jwts.builder ().setId ( UUID.randomUUID ().toString () )
                    .setSubject (
                            cryptographyService.encryptJWTToken ( securityVersion, systemKey, userName, requestId ))
                    .setIssuedAt ( now )
                    .setExpiration ( sessionExpiry )
                    .signWith ( SignatureAlgorithm.HS512, systemKey ).compact ();
        } catch (Exception e)
        {
            log.info("Unable to create a new JWT token because of: " + e.getMessage()+":"+requestId+":"+e);
            throw new RouterException( AuthRouterResponseCode.JWT_CREATION_ERROR, e, ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, "Unable to create a JWT token" );

        }
        return jwtToken;
    }


    public String buildPublicToken(String securityVersion, String systemKey, String clientId, String requestId) throws RouterException {
        List<String> clientIdData = Arrays.asList(clientId.split(ConstantsAuth.TILD_SPLITTER));

        ApplicationDef applicationDef = applicationDefConfig.findByOrgIdAndAppId(clientIdData.get(0), clientIdData.get(1));

        Date sessionExpiry = fetchExpiryTimeDateForPublicToken(applicationDef);

        log.info("JWT Token creation for user:" + clientId + " with expiry date:" + sessionExpiry);

        log.info("JWT Token creation for user:" + clientId + " with expiry date:" + sessionExpiry+":"+requestId);

        Date now = new Date ();
        String jwtToken ;
        try {
            jwtToken = Jwts.builder ().setId ( UUID.randomUUID ().toString () )
                    .setSubject (
                            cryptographyService.encryptJWTToken ( securityVersion, systemKey, clientId, requestId ))
                    .setIssuedAt ( now )
                    .setExpiration ( sessionExpiry )
                    .signWith ( SignatureAlgorithm.HS512, systemKey ).compact ();
        } catch (Exception e)
        {
            log.info("Unable to create a new JWT token because of: " + e.getMessage()+":"+requestId+":"+e);
            throw new RouterException(AuthRouterResponseCode.JWT_CREATION_ERROR, e, ConstantsAuth.ROUTER_ERROR_TYPE_SECURITY, "Unable to create a JWT token" );

        }
        return jwtToken;
    }

    public Date fetchExpiryTimeDateForPublicToken (ApplicationDef applicationDef) throws RouterException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));

        calendar.add(Calendar.DAY_OF_MONTH, applicationDef.getAppSessionExpiryTime().intValue());

        return calendar.getTime();
    }
}
