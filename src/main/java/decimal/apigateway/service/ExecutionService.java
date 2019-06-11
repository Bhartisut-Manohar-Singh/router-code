package decimal.apigateway.service;

import exception.RouterException;

import java.io.IOException;
import java.util.Map;

public interface ExecutionService {

    Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException;

    Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException;
}
