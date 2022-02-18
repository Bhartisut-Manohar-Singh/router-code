package decimal.apigateway.aspects.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Component
@Aspect
public class ExecutionAspect {

    @Autowired
    ObjectMapper objectMapper;

    @Before(value = "(execution(* decimal.apigateway.controller.ExecutionController.*(..)) && args(request,httpHeaders)) || (execution(* decimal.apigateway.controller.RegistrationController.*(..)) && args(request,httpHeaders))")
    public void insertForGateway(String request, Map<String, String> httpHeaders) throws IOException {


        this.insertHeaders(request, httpHeaders);

    }

    /*
    @Before(value = "execution(* decimal.apigateway.controller.ExecutionController.executePlainRequest(..)) && args(request,httpHeaders,orgId,appId,serviceName,version)")
    public void insertForPlainRequest(String request, Map<String, String> httpHeaders,String orgId, String appId, String serviceName,String version) throws IOException {

        this.insertHeaders(request, httpHeaders);

    }




    @Before(value = "execution(* decimal.apigateway.controller.ExecutionController.executeService(..)) && args(request,httpServletRequest,httpHeaders,serviceName) || execution(* decimal.apigateway.controller.ExecutionController.executeServicePlain(..)) && args(request,httpServletRequest,httpHeaders,serviceName)")
    public void insertForDynamicRequest(String request, HttpServletRequest httpServletRequest, Map<String, String> httpHeaders, String serviceName) throws IOException {

        this.insertHeaders(request, httpHeaders);

    }

    @Before(value = "execution(* decimal.apigateway.controller.ExecutionController.executeMultipartRequest(..)) && args(request,httpHeaders,serviceName,files,uploadRequest,httpServletRequest) || execution(* decimal.apigateway.controller.ExecutionController.executeFileRequest(..)) && args(request,httpHeaders,serviceName,files,uploadRequest,httpServletRequest)")
    public void insertForMultiPart(String request, Map<String, String> httpHeaders, String serviceName, MultipartFile[] files,String uploadRequest,HttpServletRequest httpServletRequest) throws IOException {

        this.insertHeaders(request, httpHeaders);

    }


     */
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
