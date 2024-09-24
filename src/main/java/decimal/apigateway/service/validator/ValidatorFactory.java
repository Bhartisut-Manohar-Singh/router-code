package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.security.TxnKeyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ValidatorFactory {
    private ApplicationContext applicationContext;

    @Autowired
    public ValidatorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Validator getValidator(String validationType) throws RouterException {
        Validator validator;

        switch (validationType) {
            case "CLIENT_SECRET":
                validator = applicationContext.getBean(ClientSecretValidator.class);
                break;
            case "SERVICE_NAME":
                validator = applicationContext.getBean(ServiceValidator.class);
                break;
            case "IP":
                validator = applicationContext.getBean(SourceIpValidator.class);
                break;
            case "TXN_KEY":
                validator = applicationContext.getBean(TxnKeyValidator.class);
                break;
            case "HEADERS":
                validator = applicationContext.getBean(HeadersValidator.class);
                break;
            case "REQUEST":
                validator = applicationContext.getBean(RequestValidator.class);
                break;
            case "SESSION":
                validator = applicationContext.getBean(SessionValidator.class);
                break;
            case "SERVICE_SCOPE":
                validator = applicationContext.getBean(ServiceScopeValidator.class);
                break;
            case "API_AUTHORIZATION":
                validator = applicationContext.getBean(ApiAuthorizationValidator.class);
                break;
            case "HASH":
                validator = applicationContext.getBean(HashValidator.class);
                break;
            case "APPLICATION":
                validator = applicationContext.getBean(ApplicationValidator.class);
                break;
            case "INACTIVE_SESSION":
                validator = applicationContext.getBean(InActiveSessionValidator.class);
                break;
            case "PUBLIC_JWT_TOKEN":
                validator = applicationContext.getBean(PublicJwtTokenValidator.class);
                break;
            default:
                throw new RouterException(Constant.INVALID_VALIDATION_TYPE, "Validation type is invalid: " + validationType, null);
        }

        return validator;
    }
}
