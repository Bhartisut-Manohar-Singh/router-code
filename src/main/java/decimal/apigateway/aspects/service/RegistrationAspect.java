package decimal.apigateway.aspects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.LogService;
import decimal.common.micrometer.ConstantUtil;
import decimal.common.micrometer.VahanaKPIMetrics;
import decimal.common.utils.CommonUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Map;

@Component
@Aspect
public class RegistrationAspect {

    private final LogService logService;

    @Autowired
    private VahanaKPIMetrics vahanaKpiMetrics;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static ObjectMapper mapper = new ObjectMapper();

    public RegistrationAspect(LogService logService) {
        this.logService = logService;
    }

    @Pointcut(value = "execution(* decimal.apigateway.service.RegistrationServiceImpl.*(..)) && args(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void beforeMethod(String request, Map<String, String> httpHeaders, HttpServletResponse response) {
    }

    @Before(value = "beforeMethod(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void initializeLogs(String request, Map<String, String> httpHeaders, HttpServletResponse response) {
        logService.initiateLogsData(request, httpHeaders);
        // Register Vahana Metrics
        try {
            this.registerMetrics(request,  httpHeaders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterReturning(value = "execution(* decimal.apigateway.service.RegistrationServiceImpl.*(..))", returning = "response")
    public void updateLogs(Object response) {
        logService.updateLogsData(response, HttpStatus.OK.toString(), Constant.SUCCESS_STATUS);
        try {
            this.vahanaKpiMetrics.persistMetrics(ConstantUtil.SUCCESS_STATUS, CommonUtils.getCurrentUTC(), new Long(mapper.writeValueAsString(response).getBytes().length));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void registerMetrics(String request, Map<String, String> httpHeaders) throws ParseException {
        String requestId = httpHeaders.get("requestid");
        String clientId = httpHeaders.get("clientid");

        String orgId = clientId.split(Constant.TILD_SPLITTER)[0];//orgID
        String appId = clientId.split(Constant.TILD_SPLITTER)[1];//appID
        String userId = null;

        if (httpHeaders.containsValue("username")) {
            String userName = httpHeaders.get("username");
            String[] userArr = httpHeaders.get("username").split(Constant.TILD_SPLITTER);
            if (userArr.length == 4) {
                userId = userArr[2];
            }
        }

        if (userId != null) {
            this.vahanaKpiMetrics.persistVahanaUserKpiCounterMetrics(orgId, appId, userId);
        } else {
            this.vahanaKpiMetrics.registerVahanaHttpKpiMetrics(orgId, appId, httpHeaders.get("servicename"),
                    new Long(request.getBytes().length),CommonUtils.getCurrentUTC() );
        }
    }
}