package decimal.apigateway.service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constants;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.Session;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Request;
import decimal.apigateway.repository.redis.AuthenticationSessionRepoRedis;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log
@Service
public class AuthenticationSession
{
    @Autowired
    AuthenticationSessionRepoRedis authenticationSessionRepo;

    @Autowired
    Request auditTraceFilter;

    @Autowired
    ObjectMapper objectMapper;

    public Session getSession(String userName) throws RouterException
    {
        log.info("Check session for userName " + userName + " in redis");

        log.info(" ==== userName for getting session ==== " + userName);
        Optional<Session> sessionOptional = authenticationSessionRepo.findById(userName);

        int size = RouterOperations.getStringArray(userName, Constants.TILD_SPLITTER).size();

        if(!sessionOptional.isPresent())
        {
            log.info("No session found for given userName " + userName);
            throw new RouterException(size > 3 ? RouterResponseCode.INVALID_USER_SESSION : RouterResponseCode.INVALID_APPLICATION_SESSION, (Exception) null, Constants.ROUTER_ERROR_TYPE_VALIDATION, "User session is invalid");
        }

        log.info("Session is found for given userName " + userName);
        try {
            log.info("==== session info ==== " + objectMapper.writeValueAsString(sessionOptional.get()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return sessionOptional.get();
    }

    public void removeSession(String userName)
    {
        log.info("Removing session for userName " + userName);
        Optional<Session> sessionOptional = authenticationSessionRepo.findById(userName);

        sessionOptional.ifPresent(session ->  authenticationSessionRepo.delete(session));
    }
}
