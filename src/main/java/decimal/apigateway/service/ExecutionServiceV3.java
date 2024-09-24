package decimal.apigateway.service;

import decimal.apigateway.exception.RouterException;

import java.io.IOException;
import java.util.Map;

public interface ExecutionServiceV3 {

    Object executePlainRequest(String request, Map<String, String> httpHeaders)throws RouterException, IOException;

    Object executeMultiPart(String request, Map<String, String> httpHeaders) throws RouterException, IOException;
}
