package decimal.apigateway.service.authentication.sessionmgmt;

import com.google.gson.Gson;
import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.AuthRouterResponseCode;
import decimal.apigateway.commons.ConstantsAuth;
import decimal.apigateway.domain.PublicAuthSession;
import decimal.apigateway.domain.Session;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.UserList;
import decimal.apigateway.repository.redis.AuthenticationSessionRepoRedis;
import decimal.apigateway.repository.redis.PublicAuthenticationSessionRepoRedis;
import decimal.apigateway.repository.redis.RedisKeyValuePairRepository;
import decimal.apigateway.service.util.BuiltInUtility;
import decimal.logs.filters.AuditTraceFilter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static decimal.apigateway.commons.ConstantsAuth.LOGIN_ID;


@Service
@Log
public class AuthenticationSessionService implements SessionStrategry {

    @Autowired
    private AuthenticationSessionRepoRedis authenticationSessionRepo;

    @Autowired
    RedisKeyValuePairRepository redisKeyValuePairRepository;

    @Autowired
    private PublicAuthenticationSessionRepoRedis publicAuthenticationSessionRepo;


    @Autowired
    AuditTraceFilter auditTraceFilter;



    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${session.expiry.time}")
    int sessionExpiryTime;

//    @Autowired
//    public AuthenticationSessionService(AuthenticationSessionRepo authenticationSessionRepo) {
//        this.authenticationSessionRepo = authenticationSessionRepo;
//    }

    private Session createSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys, String jwtToken) throws RouterException {


        String securityVersion = httpHeaders.get(ConstantsAuth.ROUTER_HEADER_SECURITY_VERSION);
        String requestId = httpHeaders.get(Headers.requestid.name());
        String userName = httpHeaders.get(Headers.username.name());

       // logsConnector.textPayload("Application session creation", auditTraceFilter.requestIdentifier);

        Map<String, String> rsaData = new HashMap<>();
        rsaData.put("rsa", new Gson().toJson(rsaKeys));

        List<String> clientId = AuthRouterOperations.getStringArray(userName, ConstantsAuth.TILD_SPLITTER);

        String orgId = clientId.get(0);
        String appId = clientId.get(1);
        String imeiNo = clientId.get(2);

        AuthRouterOperations.validateSessionParameters(requestId, orgId, appId, imeiNo);

        Session appSession = new Session();
        appSession.setOrgId(orgId);
        appSession.setAppId(appId);
        appSession.setDeviceId(imeiNo);
        appSession.setSessionId(requestId);
        appSession.setJwtKey(jwtToken);
        appSession.setRequestId(requestId);
        appSession.setSessionData(rsaData);
        appSession.setUsername(userName);
        appSession.setLastLogin(BuiltInUtility.simpleDateFormat());

//            appSession.setCreatedOn(new Timestamp(System.currentTimeMillis()));
        appSession.setSecurityVersion(securityVersion);

        return appSession;
    }

    @Override
    public Session isValidSession(String requestId, String key) {

        Optional<Session> appSession = authenticationSessionRepo.findById(key);

        if (appSession.isPresent()) {
            log.info("Application Session found in Redis."+ auditTraceFilter.requestIdentifier);
            return appSession.get();
        }

        return null;
    }

    @Override
    public boolean removeSession(String key) {
        log.info("Calling Remove session for username -- " + key);
        Optional<Session> byId = authenticationSessionRepo.findById(key);

        byId.ifPresent(session -> {
            log.info("Authentication session exist for username -- " + key +
                    ", Going to remove it");
            authenticationSessionRepo.delete(session);
            log.info("Session removed for -- " + key);
        }
        );

        return true;
    }

    /* (non-Javadoc)
     * @see decimal.router.engine.service.sessionmgmt.SessionStrategry#getKeys(java.lang.String)
     */
    @Override
    public Set<String> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public void createUserSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys) throws RouterException {

        log.info("Creating user session for userName: " + httpHeaders.get(Headers.username.name()));
        System.out.println("Creating user session for userName: " + httpHeaders.get(Headers.username.name()));
        String jwtToken = String.valueOf(rsaKeys.get("jwtToken"));

        Session session = createSession(httpHeaders, rsaKeys, jwtToken);

        String loginId = httpHeaders.get(Headers.username.name()).split("~")[2];
        String deviceId = httpHeaders.get(Headers.username.name()).split("~")[3];

        AuthRouterOperations.validateSessionParameters(loginId);

        session.setLoginId(loginId);
        session.setAppJwtKey(String.valueOf(rsaKeys.get("appJwtToken")));
        session.setDeviceId(deviceId);
        session.setLastLogin(BuiltInUtility.simpleDateFormat());

        authenticationSessionRepo.save(session);
        System.out.println("============================================Session Saved in Redis for username " + httpHeaders.get(Headers.username.name()));
        log.info("User session has been created successfully for userName: " + httpHeaders.get(Headers.username));
    }

    @Override
    public void createAndSaveAppSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys) throws RouterException {
        System.out.println("============================================Creating App session for userName: " + httpHeaders.get(Headers.username.name()));
        String jwtToken = String.valueOf(rsaKeys.get("jwtToken"));

        Session session = createSession(httpHeaders, rsaKeys, jwtToken);
        authenticationSessionRepo.save(session);
        System.out.println("-------------------Session Id-----------------"+session.getUsername());
        if (httpHeaders.get("platform")!=null)
        {
            String platform= httpHeaders.get("platform");
            if(platform.equalsIgnoreCase("Web")) {
                System.out.println("----------------------Expiring Session--------------------------------");
                expireSession(session, sessionExpiryTime);
            }
        }
        System.out.println("============================================Session Saved in Redis for username " + httpHeaders.get(Headers.username.name()));

    }


    private void expireSession(Session session, int expiryTimeInHours) {

        String session_expire ="AUTHENTICATION_SESSION:orgId:"+ session.getOrgId();
        stringRedisTemplate.expire(session_expire, expiryTimeInHours, TimeUnit.HOURS);
        String session_expire1 ="AUTHENTICATION_SESSION:appId:" + session.getAppId();
        stringRedisTemplate.expire(session_expire1, expiryTimeInHours, TimeUnit.HOURS);
        String session_expire2 ="AUTHENTICATION_SESSION:loginId:" + session.getLoginId();
        stringRedisTemplate.expire(session_expire2, expiryTimeInHours, TimeUnit.HOURS);
        String session_expire3 ="AUTHENTICATION_SESSION:deviceId:" + session.getDeviceId();
        stringRedisTemplate.expire(session_expire3, expiryTimeInHours, TimeUnit.HOURS);
        String session_expire4 ="AUTHENTICATION_SESSION:requestId:" + session.getRequestId();
        stringRedisTemplate.expire(session_expire4, expiryTimeInHours, TimeUnit.HOURS);
        String  session_expire5 = "AUTHENTICATION_SESSION:" + session.getUsername();
        stringRedisTemplate.expire(session_expire5, expiryTimeInHours, TimeUnit.HOURS);
        session_expire5 = session_expire5 + ":idx";
        stringRedisTemplate.expire(session_expire5, expiryTimeInHours, TimeUnit.HOURS);
    }


    @Override
    public void checkInactiveSession(String token, String requestId, int inactiveSessionExpTimeMinutes) throws RouterException {

    }

    @Override
    public void storeInactiveSessionDetails(String token, String requestId, Long inactiveSessionExpTimeMinutes) throws RouterException {

        if (inactiveSessionExpTimeMinutes == null || inactiveSessionExpTimeMinutes == 0) {
            throw new RouterException(AuthRouterResponseCode.INVALID_SESSION_EXPIRY_PARAMETERS, null);
        } else {
            redisKeyValuePairRepository.add(token, "", inactiveSessionExpTimeMinutes.intValue());
            log.info(AuthRouterOperations.getLogMessage(requestId,
                    "================tokenDetails stored is Redis with expiry of minutes:" + inactiveSessionExpTimeMinutes + "============================"));
        }
    }


    public List<Session> findByOrgIdAndAppIdAndLoginId(String orgId, String appId, String loginId) {
        return authenticationSessionRepo.findByOrgIdAndAppIdAndLoginId(orgId, appId, loginId);
    }

    public void removeAllSessions(List<Session> sessions) {
        authenticationSessionRepo.deleteAll(sessions);
    }

    public List<Session> findByOrgIdAndAppIdAndDeviceId(String orgId, String appId, String deviceId) {
        return authenticationSessionRepo.findByOrgIdAndAppIdAndDeviceId(orgId, appId, deviceId);
    }

    public void removeSessionByOrgIdAndAppIdAndDeviceId(String orgId, String appId, String deviceId) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        joiner.add(orgId);
        joiner.add(appId);
        joiner.add(deviceId);

        String logMessage = "[orgId, appId, deviceId] " + joiner.toString();

        log.info("Finding session for " + logMessage);

        List<Session> byOrgIdAndAppIdAndDeviceId = authenticationSessionRepo.findByOrgIdAndAppIdAndDeviceId(orgId, appId, deviceId);

        if (byOrgIdAndAppIdAndDeviceId.isEmpty()) {
            log.info("No Session found for " + logMessage);
            return;
        }

        log.info("Number of sessions found for " + logMessage + " is " + byOrgIdAndAppIdAndDeviceId.size());

        log.info("Filtering user sessions for " + logMessage);

        List<Session> userSessions = byOrgIdAndAppIdAndDeviceId.stream().filter(session -> session.getLoginId() != null && !session.getLoginId().isEmpty()).collect(Collectors.toList());

        log.info("Number of User session found on device: " + logMessage + " is: " + userSessions.size());

        log.info("Killing all the user sessions for " + logMessage);

        removeAllSessions(userSessions);
    }

    public List<Session> findByOrgIdAndAppId(String orgId, String appId) {
        return authenticationSessionRepo.findByOrgIdAndAppId(orgId, appId);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) { Set<Object> seen = ConcurrentHashMap.newKeySet(); return t -> seen.add(keyExtractor.apply(t)); }

    public List<UserList> getUserList(String orgId,String appId)
    {
        List<UserList> users=new LinkedList<>();

        List<Session> sessions=authenticationSessionRepo.findByOrgIdAndAppId(orgId, appId);
        sessions.forEach(session -> {

            if(null!=session.getLoginId() && !session.getLoginId().isEmpty()){
                UserList userList=new UserList(session.getUsername(),session.getOrgId(),session.getAppId(),session.getLoginId(),session.getDeviceId(),session.getSessionId(),session.getLastLogin());
                users.add(userList);
            }
        });

        return users.stream().filter(distinctByKey(UserList::getLoginId)).collect(Collectors.toList());

    }

    @Override
    public void createAndSavePublicSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys) throws RouterException {
        log.info("============================================Creating Public session for userName: " + httpHeaders.get(Headers.username.name()));
        String jwtToken = String.valueOf(rsaKeys.get("jwtToken"));

        PublicAuthSession session = createPublicSession(httpHeaders, rsaKeys, jwtToken);
        publicAuthenticationSessionRepo.save(session);
        System.out.println("-------------------Session Id-----------------"+session.getUsername());
        if (httpHeaders.get("platform")!=null)
        {
            String platform= httpHeaders.get("platform");
            if(platform.equalsIgnoreCase("Web")) {
                System.out.println("----------------------Expiring Session--------------------------------");
//                expireSession(session, sessionExpiryTime);
            }
        }
        System.out.println("============================================Session Saved in Redis for username " + httpHeaders.get(Headers.username.name()));

    }
    private PublicAuthSession createPublicSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys, String jwtToken) throws RouterException {


        String securityVersion = httpHeaders.get(ConstantsAuth.ROUTER_HEADER_SECURITY_VERSION);
        String requestId = httpHeaders.get(Headers.requestid.name());
        String clientId = httpHeaders.get(ConstantsAuth.CLIENT_ID);
        String loginId = httpHeaders.get(LOGIN_ID);


        Map<String, String> rsaData = new HashMap<>();
        rsaData.put("rsa", new Gson().toJson(rsaKeys));

        List<String> clientIdData = AuthRouterOperations.getStringArray(clientId, ConstantsAuth.TILD_SPLITTER);

        String orgId = clientIdData.get(0);
        String appId = clientIdData.get(1);


        PublicAuthSession publicSession = new PublicAuthSession();
        publicSession.setOrgId(orgId);
        publicSession.setAppId(appId);
        publicSession.setSessionId(requestId);
        publicSession.setJwtKey(jwtToken);
        publicSession.setRequestId(requestId);
        publicSession.setUsername(clientId + ConstantsAuth.TILD_SPLITTER + loginId);
        publicSession.setLastLogin(BuiltInUtility.simpleDateFormat());

        publicSession.setSecurityVersion(securityVersion);

        return publicSession;
    }
}
