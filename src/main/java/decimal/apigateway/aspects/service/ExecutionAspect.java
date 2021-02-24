package decimal.apigateway.aspects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jmx.defaults.ServiceName;
import decimal.apigateway.commons.Constant;
import decimal.common.micrometer.ConstantUtil;
import decimal.common.micrometer.VahanaKPIMetrics;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class ExecutionAspect {

    @Autowired
    private VahanaKPIMetrics vahanaKpiMetrics;

    @Value("${dms.default.servicename}")
    private String dmsDefaultServiceName;

    @Value("${dynamic.router.default.servicename}")
    private String dynamicDefaultServiceName;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static ObjectMapper mapper = new ObjectMapper();

    @Pointcut(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.*(..)) && args(request, httpHeaders)", argNames = "request, httpHeaders")
    public void beforeMethod(String request, Map<String, String> httpHeaders) {
    }


    @Before(value =  "execution(* decimal.apigateway.controller.ExecutionController.executeService(..)) || execution(* decimal.apigateway.controller.ExecutionController.executeServicePlain(..))")
    public void setServiceName(JoinPoint joinPoint) {

            Object[] args = joinPoint.getArgs();
            Map<String,String> httpHeaders = (Map<String, String>) args[2];

            if(StringUtils.isEmpty(httpHeaders.get("servicename")) || httpHeaders.get("servicename").equals("undefined"))
            httpHeaders.put("servicename",dynamicDefaultServiceName);

            args[2] = httpHeaders;
    }

    @Before(value =  "execution(* decimal.apigateway.controller.ExecutionController.executeMultipartRequest(..)) || execution(* decimal.apigateway.controller.ExecutionController.executeFileRequest(..))")
    public void setServiceNameForDMS(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();
        Map<String,String> httpHeaders = (Map<String, String>) args[1];

        if(StringUtils.isEmpty(httpHeaders.get("servicename")) || httpHeaders.get("servicename").equals("undefined"))
            httpHeaders.put("servicename",dmsDefaultServiceName);

        args[1] = httpHeaders;

    }

    @Before(value = "beforeMethod(request, httpHeaders)", argNames = "request, httpHeaders")
    public void initializeLogs( String request, Map<String, String> httpHeaders) {
        // Register Vahana Metrics

        try {
            this.registerMetrics(request, httpHeaders);
        } catch (Exception e) {
            logger.error(e.getMessage() , e);
        }
    }

    @AfterReturning(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.*(..))", returning = "response")
    public void updateLogs(Object response) {
        try {
            // Persist Vahana HTTP Metrics
            this.vahanaKpiMetrics.persistMetrics(ConstantUtil.SUCCESS_STATUS, System.currentTimeMillis(), new Long(mapper.writeValueAsString(response).getBytes().length));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void registerMetrics(String request, Map<String, String> httpHeaders) throws ParseException {
        String clientId = httpHeaders.get("clientid");
        String orgId = clientId.split(Constant.TILD_SPLITTER)[0];//orgID
        String appId = clientId.split(Constant.TILD_SPLITTER)[1];//appID
        String userId = null;

        if (httpHeaders.containsValue("username")) {
            String userName = httpHeaders.get("username");
            System.out.println(">>>>>>>>>>>> username ="+userName);
            String[] userArr = httpHeaders.get("username").split(Constant.TILD_SPLITTER);
            if (userArr.length == 4) {
                userId = userArr[2];
            }
        }
        logger.info("going to generate user metrics");
        if (userId != null) {
            logger.info("inside if loop");

            this.vahanaKpiMetrics.persistVahanaUserKpiCounterMetrics(orgId, appId, userId);
            logger.info("user metrics generated");

        }

        this.vahanaKpiMetrics.registerVahanaHttpKpiMetrics(orgId, appId, httpHeaders.get("servicename"),   new Long(request.getBytes().length), System.currentTimeMillis());
    }




}
