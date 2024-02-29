package decimal.apigateway.service.authentication;


import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.SSOTokenModel;

import java.io.IOException;
import java.util.Map;

public interface VahanaSSOService {

    Object generateSSOToken(SSOTokenModel ssoTokenModel, Map<String, String> httpHeaders) throws RouterException, IOException;

    Object validateSSOToken(SSOTokenModel ssoTokenModel, Map<String, String> httpHeaders) throws IOException, RouterException;

    Object ssoLoginDetails(SSOTokenModel ssoTokenModel, Map<String, String> httpHeaders) throws IOException, RouterException;

}
