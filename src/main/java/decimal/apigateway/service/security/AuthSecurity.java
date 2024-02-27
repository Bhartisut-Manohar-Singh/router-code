package decimal.apigateway.service.security;



import decimal.apigateway.exception.RouterException;
import decimal.apigateway.domain.Session;
import decimal.apigateway.model.Account;

import java.util.Map;

public interface AuthSecurity{

    Map<String, String> decryptAuthHeader(String securityVersion, Account account, String data) throws RouterException;

    void checkAuthDataHash(String securityVersion, Account account, String data, String requestId) throws RouterException;

    boolean validateData(Account account) throws RouterException;

    String decryptTxnKey(Session session, String txnKey) throws RouterException;

    Object decryptRequestV1(String request, Map<String, String> httpHeaders) throws RouterException;

    String getAesKey(Map<String, String> httpHeaders) throws RouterException;
}
