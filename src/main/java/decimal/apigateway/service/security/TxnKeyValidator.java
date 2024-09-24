package decimal.apigateway.service.security;

import decimal.apigateway.domain.Session;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TxnKeyValidator implements Validator
{
    @Autowired
    AuthSecurity authSecurity;

    @Autowired
    ValidateTxnKey validateTxnKey;

    @Autowired
    AuthenticationSession authenticationSession;


    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException {
        String userName = httpHeaders.get("username");
        String txnKey = httpHeaders.get("txnkey");
        String securityVersion = httpHeaders.get("security-version");

        Session session = authenticationSession.getSession(userName);

        String decryptedTxnKey = authSecurity.decryptTxnKey(session, txnKey);

        validateTxnKey.validateTxnKey(txnKey, decryptedTxnKey, securityVersion);

        return new MicroserviceResponse();
    }
}
