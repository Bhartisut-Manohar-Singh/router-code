package decimal.apigateway.aspects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.util.StringUtil;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.service.LogService;
import decimal.common.micrometer.ConstantUtil;
import decimal.common.micrometer.CustomEndpointMetrics;
import decimal.common.micrometer.CustomMetricsData;
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
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Aspect
public class RegistrationAspect {
    private final LogService logService;

    @Autowired
    private CustomEndpointMetrics customEndpointMetrics;

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
//        List<String> clientId;
//        try {
//
//            clientId = RouterOperations.getStringArray(httpHeaders.get("clientid"), Constant.TILD_SPLITTER);
//        } catch (Exception ex) {
//            clientId = new ArrayList<>();
//            clientId.add(httpHeaders.get("orgid"));
//            clientId.add(httpHeaders.get("appid"));
//        }
        try {
//            if (httpHeaders.get("version") != null)
//                this.customEndpointMetrics.registerMetrics(httpHeaders.get("requestid"), new Long(request.getBytes().length), clientId.get(0), clientId.get(1), httpHeaders.get("username"), httpHeaders.get("servicename"), CommonUtils.getCurrentUTC(), "serviceVersion", httpHeaders.get("version"));
//            else
//                this.customEndpointMetrics.registerMetrics(httpHeaders.get("requestid"), clientId.get(0), clientId.get(1), httpHeaders.get("username"), httpHeaders.get("servicename"), CommonUtils.getCurrentUTC(), new Long(request.getBytes().length));
            this.registerMetrics(request,  httpHeaders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logService.initiateLogsData(request, httpHeaders);

    }

    @AfterReturning(value = "execution(* decimal.apigateway.service.RegistrationServiceImpl.*(..))", returning = "response")
    public void updateLogs(Object response) {
        logService.updateLogsData(response, HttpStatus.OK.toString(), Constant.SUCCESS_STATUS);
        try {
            this.customEndpointMetrics.persistMetrics(ConstantUtil.SUCCESS_STATUS, CommonUtils.getCurrentUTC(), new Long(mapper.writeValueAsString(response).getBytes().length));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void registerMetrics(String request, Map<String, String> httpHeaders) throws ParseException {
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
            customEndpointMetrics.registerMetrics(requestId, orgId, appId, userId, httpHeaders.get("servicename"),
                    CommonUtils.getCurrentUTC(),
                    new Long(request.getBytes().length)
            );
        } else {
            customEndpointMetrics.registerMetrics(requestId, orgId, CommonUtils.getCurrentUTC(), appId, httpHeaders.get("servicename"),
                    new Long(request.getBytes().length)
            );
        }
    }
}