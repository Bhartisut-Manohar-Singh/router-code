package decimal.apigateway.service.validator;

import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.security.AuthenticationSession;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Log
@Service
public class SessionValidator implements Validator {

    private final AuthenticationSession authenticationSession;

    @Autowired
    Request auditTraceFilter;

    public SessionValidator(AuthenticationSession authenticationSession) {
        this.authenticationSession = authenticationSession;
    }

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException
    {
        String userName = httpHeaders.get(Headers.username.name());

        log.info("Validating session ");


        authenticationSession.getSession(userName);

        log.info("Session is found for userName " + userName);

        return new MicroserviceResponse();
    }
}
