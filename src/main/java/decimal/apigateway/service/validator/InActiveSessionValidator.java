package decimal.apigateway.service.validator;

//import decimal.logs.connector.LogsConnector;
//import decimal.logs.model.RequestIdentifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.Session;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import decimal.apigateway.repository.redis.AuthenticationSessionRepoRedis;
import decimal.apigateway.repository.redis.RedisKeyValuePairRepository;
import decimal.apigateway.service.ApplicationDefConfig;
import decimal.apigateway.service.authentication.sessionmgmt.AuthenticationSessionService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log
@Service
public class InActiveSessionValidator implements Validator
{

    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    RedisKeyValuePairRepository redisKeyValuePairRepository;

    @Autowired
    AuthenticationSessionService authenticationSessionService;

    @Autowired
    Request auditTraceFilter;

//    @Autowired
//    LogsConnector logsConnector;
    
    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

//        RequestIdentifier requestIdentifier = auditTraceFilter.getRequestIdentifier(auditTraceFilter);

        String clientId = httpHeaders.get(Constant.CLIENT_ID);
        String requestId = httpHeaders.get(Constant.ROUTER_HEADER_REQUEST_ID);
        String userName = httpHeaders.get(Constant.ROUTER_HEADER_USERNAME);
        log.info("Validating  inactive session: "+userName);

        List<String> clientIdData = RouterOperations.getStringArray(clientId, Constant.TILD_SPLITTER);

        ApplicationDef applicationDef = applicationDefConfig.findByOrgIdAndAppId(clientIdData.get(0), clientIdData.get(1));

        if (!("Y".equalsIgnoreCase(applicationDef.getIsUserInactiveSessionRequired())))
        {
            log.info("Inactive session is not enabled for appId " + applicationDef.getAppId());
            return new MicroserviceResponse();
        }

        String token = httpHeaders.get("username");

        if(RouterOperations.getStringArray(userName, Constant.TILD_SPLITTER).size() > 3 && applicationDef.getIsUserInactiveSessionRequired() != null && applicationDef.getIsUserInactiveSessionRequired().equalsIgnoreCase("Y"))
        {
            Object tokenDetails = redisKeyValuePairRepository.get(token);

            if (tokenDetails != null) {
                log.info(requestId + " - "+"============tokenDetails found in Redis===================" + tokenDetails);
                // refresh last active time
                storeInactiveSessionDetails(token, requestId, applicationDef.getUserInactiveSessionExpiryTime().intValue());


            } else {

                log.info(requestId+" - "+"==============================tokenDetails not found in Redis.Inactive session expired.=========================");
                authenticationSessionService.removeSession(token);
                throw new RouterException(RouterResponseCode.INACTIVE_USER_SESSION, null);
            }
        }

        log.info("Validating  inactive session is success.");

       return new MicroserviceResponse();
    }

    private void storeInactiveSessionDetails(String token, String requestId, int inactiveSessionExpTimeMinutes) throws RouterException {

        log.info("Refreshing last active time for given token " + token + " in redis");

        if (inactiveSessionExpTimeMinutes == 0)
        {
            log.info("InactiveSessionExpTimeMinutes value is 0 so session detail can not be saved into redis");

            throw new RouterException(RouterResponseCode.INVALID_SESSION_EXPIRY_PARAMETERS, null);
        } else {
            redisKeyValuePairRepository.add(token, "", inactiveSessionExpTimeMinutes);
            log.info(requestId + " - "+ "================tokenDetails stored is Redis with expiry of minutes:" + inactiveSessionExpTimeMinutes + "============================");
        }
    }
}
