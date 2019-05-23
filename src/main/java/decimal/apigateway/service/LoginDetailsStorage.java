package decimal.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.AppLoginDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class LoginDetailsStorage {

    @Autowired
    ObjectMapper objectMapper;

    public void storeLoginDetails(Object request, Map<String, String> httpHeaders) throws IOException {
        AppLoginDetails appLoginDetails = objectMapper.readValue(String.valueOf(request), AppLoginDetails.class);

        appLoginDetails.setRequestId(httpHeaders.get("requestid"));
        appLoginDetails.setImeiNo(httpHeaders.get("username").split(Constant.TILD_SPLITTER)[3]);

        System.out.println(objectMapper.writeValueAsString(appLoginDetails));
    }


    public void storeRegistrationDeviceDetails(Object request, Map<String, String> httpHeaders) throws IOException
    {
//        DeviceInstallationDetails appLoginDetails = objectMapper.readValue(String.valueOf(request), DeviceInstallationDetails.class);
    }
}
