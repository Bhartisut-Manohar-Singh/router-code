package decimal.apigateway.aspects.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
@Aspect
public class ExecutionAspect {

    @Autowired
    ObjectMapper objectMapper;

    @Before(value = "(execution(* decimal.apigateway.service.ExecutionServiceImpl.*(..)) && args(request,httpHeaders)) ")
    public void insertForGateway(String request, Map<String, String> httpHeaders) throws IOException {

        this.insertHeaders(request, httpHeaders);

    }



    @Pointcut(value = "execution(* decimal.apigateway.controller.RegistrationController.*(..)) && args(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void beforeMethod(String request, Map<String, String> httpHeaders, HttpServletResponse response) {
    }

    @Before(value = "beforeMethod(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void insert(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException {

        this.insertHeaders(request,httpHeaders);
    }


    @Before(value = "execution(* decimal.apigateway.controller.ExecutionController.executeService(..)) && args(request,httpServletRequest,httpHeaders,serviceName) || execution(* decimal.apigateway.controller.ExecutionController.executeServicePlain(..)) && args(request,httpServletRequest,httpHeaders,serviceName)")
    public void insertForDynamicRequest(String request, HttpServletRequest httpServletRequest, Map<String, String> httpHeaders, String serviceName) throws IOException {

        this.insertHeaders(request, httpHeaders);

    }


    private void insertHeaders(String request, Map<String, String> httpHeaders) throws IOException {

        System.out.println("---------------------------------------------Inside header insert----------------------");
        if(request.contains("interfaces")){

            JsonNode jsonNode = objectMapper.readTree(request);

            httpHeaders.put("applicationVersion",jsonNode.has("APPLICATION_VERSION")?jsonNode.get("APPLICATION_VERSION").textValue():"");

            httpHeaders.put("deviceLatitude",jsonNode.has("DEVICE_LATITUDE")?jsonNode.get("DEVICE_LATITUDE").textValue():"");

            httpHeaders.put("deviceLongitude",jsonNode.has("DEVICE_LONGITUDE")?jsonNode.get("DEVICE_LONGITUDE").textValue():"");

            httpHeaders.put("imeiNo",jsonNode.has("IMEI_NO")?jsonNode.get("IMEI_NO").textValue():"");


        }
    }
}
