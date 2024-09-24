package decimal.apigateway.service.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.UserLoginDetails;
import decimal.apigateway.enums.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class LoginDetailsStorage {

    @Autowired
    ObjectMapper objectMapper;

    private static final Supplier<Timestamp> CURRENT_TIME_STAMP = () -> new Timestamp(System.currentTimeMillis());

    private void parseDataFromHeaders(Map<String, String> httpHeaders, UserLoginDetails userLoginDetails) {
        String userName = httpHeaders.get(Headers.username.name());

        List<String> clientData = AuthRouterOperations.getStringArray(userName, Constant.TILD_SPLITTER);

        userLoginDetails.setOrgId(clientData.get(0));
        userLoginDetails.setAppId(clientData.get(1));
        userLoginDetails.setLoginId(clientData.get(2));
        userLoginDetails.setImeiNo(clientData.get(3));
        userLoginDetails.setUsername(userName);

        userLoginDetails.setRequestId(httpHeaders.get(Headers.requestid.name()));

        userLoginDetails.setPlatform(httpHeaders.get(Constant.ROUTER_HEADER_PLATFORM));
        userLoginDetails.setPlatformDetails(httpHeaders.get(Constant.ROUTER_HEADER_CLIENT_DETAILS));
        userLoginDetails.setIpAddress(httpHeaders.get(Constant.ROUTER_HEADER_SOURCE_IP));
    }
}
