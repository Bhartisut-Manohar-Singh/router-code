package decimal.apigateway.service.validator;


import decimal.apigateway.commons.Constants;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.ApplicationDefConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationValidator implements Validator {

    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    Request requestObj;

    /*@Autowired
    LogsConnector logsConnector;*/
    
    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws IOException, RouterException {

/*        RequestIdentifier requestIdentifier = requestObj.getRequestIdentifier(requestObj);*/

        String clientId = httpHeaders.get(Constants.CLIENT_ID);

        /*logsConnector.textPayload("Validating application Id for existing", requestIdentifier);*/

        if (clientId == null || clientId.isEmpty()) {
            /*logsConnector.textPayload("Client Id is missing from the request headers", requestIdentifier);*/
            throw new RouterException(RouterResponseCode.ORGID_APPID_ERROR, (Exception) null, Constants.ROUTER_ERROR_TYPE_VALIDATION, "Client Id is missing from the request");
        }

        List<String> clientIdData = RouterOperations.getStringArray(clientId, Constants.TILD_SPLITTER);

        applicationDefConfig.findByOrgIdAndAppId(clientIdData.get(0), clientIdData.get(1));

        /*logsConnector.textPayload("Validation has been done for application successfully", requestIdentifier);*/

        return new MicroserviceResponse();
    }
}
