package decimal.apigateway.service;

import decimal.apigateway.exception.RouterException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public interface ExecutionServiceV2 {
    Object executeRequest( String destinationAppId,String serviceName, String request, Map<String, String> httpHeaders) throws IOException, RouterException;

    Object executePlainRequest(String request, Map<String, String> httpHeaders)throws RouterException, IOException;

    Object executeDynamicRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName)throws RouterException, IOException;

    Object executeDynamicRequestPlain(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName)throws RouterException, IOException;

    Object executeMultipartRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String uploadRequest, MultipartFile[] files)throws RouterException, IOException;

    Object executeFileRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String mediaDataObjects, MultipartFile[] files)throws RouterException, IOException;
}
