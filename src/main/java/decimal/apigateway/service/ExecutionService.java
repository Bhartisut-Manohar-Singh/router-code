package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import decimal.apigateway.exception.RouterExceptionV1;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public interface ExecutionService {

    Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterExceptionV1, IOException;

    Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterExceptionV1, IOException;

    Object executeDynamicRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterExceptionV1, IOException;

    Object executeMultipartRequest(HttpServletRequest httpServletRequest, String interfaces, Map<String, String> httpHeaders, String serviceName,String uploadRequest, MultipartFile[] files) throws RouterExceptionV1, IOException;

    Object executeFileRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String mediaDataObjects, MultipartFile[] files) throws RouterExceptionV1, IOException;

    Object executeDynamicRequestPlain(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterExceptionV1, JsonProcessingException;
}
