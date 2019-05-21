package decimal.apigateway.service;

import decimal.apigateway.service.clients.EsbClient;
import decimal.apigateway.service.validator.RequestValidator;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    EsbClient esbClient;

    @Override
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        requestValidator.validatePlainRequest(httpHeaders);

        return esbClient.executeAuthentication(request, httpHeaders);
    }
}
