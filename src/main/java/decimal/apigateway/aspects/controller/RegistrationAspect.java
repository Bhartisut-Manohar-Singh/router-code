package decimal.apigateway.aspects.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
/*
@Component
@Aspect
public class RegistrationAspect {

    @Autowired
    ObjectMapper objectMapper;

    @Pointcut(value = "execution(* decimal.apigateway.controller.RegistrationController.*(..)) && args(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void beforeMethod(String request, Map<String, String> httpHeaders, HttpServletResponse response) {
    }

    @Before(value = "beforeMethod(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void insert(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException {

        if(request.contains("interfaces")){

            JsonNode jsonNode = objectMapper.readTree(request);

            httpHeaders.put("applicationVersion",jsonNode.has("APPLICATION_VERSION")?jsonNode.get("APPLICATION_VERSION").textValue():"");

            httpHeaders.put("deviceLatitude",jsonNode.has("DEVICE_LATITUDE")?jsonNode.get("DEVICE_LATITUDE").textValue():"");

            httpHeaders.put("deviceLongitude",jsonNode.has("DEVICE_LONGITUDE")?jsonNode.get("DEVICE_LONGITUDE").textValue():"");

            httpHeaders.put("imeiNo",jsonNode.has("IMEI_NO")?jsonNode.get("IMEI_NO").textValue():"");


        }

    }


}
*/