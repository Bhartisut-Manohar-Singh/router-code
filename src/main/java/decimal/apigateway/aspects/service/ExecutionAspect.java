package decimal.apigateway.aspects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.LogService;
import decimal.common.micrometer.CustomEndpointMetrics;
import decimal.common.utils.CommonUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@Aspect
@Component
public class ExecutionAspect {

    private final LogService logService;

    @Autowired
    private CustomEndpointMetrics customEndpointMetrics;

    @Value("${metricsName}")
    private String metricsName;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static ObjectMapper mapper = new ObjectMapper();

    public ExecutionAspect(LogService logService) {
        this.logService = logService;
    }

    @Pointcut(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.*(..)) && args(request, httpHeaders)", argNames = "request, httpHeaders")
    public void beforeMethod(String request, Map<String, String> httpHeaders) {
    }

    @Before(value = "beforeMethod(request, httpHeaders)", argNames = "request, httpHeaders")
    public void initializeLogs(String request, Map<String, String> httpHeaders) {
        logService.initiateLogsData(request, httpHeaders);
    }

    @AfterReturning(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.*(..))", returning = "response")
    public void updateLogs(Object response) {
        logService.updateLogsData(response, HttpStatus.OK.toString(), Constant.SUCCESS_STATUS);
        try {
            this.customEndpointMetrics.persistMetrics("SUCCESS", CommonUtils.getCurrentUTC(), new Long(mapper.writeValueAsString(response).getBytes().length));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Before(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.executePlainRequest(..)) && args(request, httpHeaders)")
    public void registerGatewayProcessorMetrics(String request, Map<String, String> httpHeaders) {
        try {
            this.customEndpointMetrics.registerMetrics(metricsName, new Long(request.getBytes().length), httpHeaders.get("orgid"), httpHeaders.get("appid"), httpHeaders.get("username"), httpHeaders.get("servicename"), CommonUtils.getCurrentUTC(), "serviceVersion", httpHeaders.get("version"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Before(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.executePlainRequest(..)) && args(request, httpHeaders, orgId, appId, serviceName, version)")
    public void registerExecuterMetrics(String request, Map<String, String> httpHeaders, String orgId,
                                        String appId, String serviceName, String version) {

        try {
            this.customEndpointMetrics.registerMetrics(metricsName, new Long(request.getBytes().length), orgId, appId, httpHeaders.get("username"), serviceName, CommonUtils.getCurrentUTC(), "serviceVersion", version);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Before(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.executeRequest(..)) && args(request, httpHeaders)")
    public void registerExecuteRequestMetrics(String request, Map<String, String> httpHeaders) {

        String clientId = httpHeaders.get("clientid");

        String orgid = clientId.split(Constant.TILD_SPLITTER)[0];
        String appid = clientId.split(Constant.TILD_SPLITTER)[1];
        try {
            this.customEndpointMetrics.registerMetrics(metricsName, new Long(request.getBytes().length), orgid, appid, httpHeaders.get("username"), httpHeaders.get("servicename"), CommonUtils.getCurrentUTC(), "serviceVersion", httpHeaders.get("version"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}