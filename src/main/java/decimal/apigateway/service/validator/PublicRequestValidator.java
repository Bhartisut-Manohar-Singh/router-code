package decimal.apigateway.service.validator;


import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.Request;
import decimal.apigateway.service.ApplicationDefConfig;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Log
public class PublicRequestValidator implements Validator {

    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    Request requestObj;
    
    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        String clientId = httpHeaders.get(Constant.CLIENT_ID);

        if (clientId == null || clientId.isEmpty()) {
            throw new RouterException(RouterResponseCode.ORGID_APPID_ERROR, (Exception) null, Constant.ROUTER_ERROR_TYPE_VALIDATION, "Client Id is missing from the request");
        }

        ApplicationDef def = new ApplicationDef();
        try {
            List<String> clientIdData = RouterOperations.getStringArray(clientId, Constant.TILD_SPLITTER);

            log.info("clientIdData : " +clientIdData);

           def = applicationDefConfig.findByOrgIdAndAppId(clientIdData.get(0), clientIdData.get(1));
        } catch (RouterException ex) {
            log.info("  ==========================123====================== ");
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            String message = "Error in getting OrgId and AppId from ClientId parameter.Please make sure you have passed parameter in header with name clientid with value orgid~appid";
            log.info("  ==========================321====================== ");
            throw new RouterException(RouterResponseCode.ORGID_APPID_ERROR, e, Constant.ROUTER_ERROR_TYPE_SECURITY, message);
        }
        MicroserviceResponse response = new MicroserviceResponse();
        response.setResponse(def);

        return response;
    }
}
