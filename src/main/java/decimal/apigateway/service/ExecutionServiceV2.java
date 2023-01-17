package decimal.apigateway.service;

import decimal.apigateway.exception.RouterException;

import java.io.IOException;
import java.util.Map;

public interface ExecutionServiceV2 {
    Object executeRequest(String sourceAppId, String sourceOrgId, String destinationAppId, String request, Map<String, String> httpHeaders) throws IOException, RouterException;
}
