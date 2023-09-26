package decimal.apigateway.service;

import decimal.apigateway.exception.RouterExceptionV1;

import java.io.IOException;
import java.util.Map;

public interface ExecutionServiceV3 {

    Object executePlainRequest(String request, Map<String, String> httpHeaders)throws RouterExceptionV1, IOException;

    Object executeMultiPart(String request, Map<String, String> httpHeaders) throws RouterExceptionV1, IOException;
}
