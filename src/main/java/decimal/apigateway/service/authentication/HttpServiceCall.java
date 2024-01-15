package decimal.apigateway.service.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.ConstantsAuth;
import decimal.apigateway.model.AnalyticsRequest;
import decimal.apigateway.model.InstallationDetails;
import decimal.apigateway.model.LastLoginDetails;
import decimal.apigateway.model.SummaryDetails;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Log
@Service
public class HttpServiceCall {

    @Autowired
    ObjectMapper objectMapper;

    @Value("${analytics.url}")
    private String analyticsUrlBase;

    @Async
    public void callAnalyticsPortalInstallationDetailsApi(String request, Map<String, String> httpHeaders) {
        try {
            String url = analyticsUrlBase + (analyticsUrlBase.endsWith("/") ? "" : "/") + ConstantsAuth.ANALYTICS_INSTALLATION_MAPPING;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            InstallationDetails installationDetails = new InstallationDetails((objectMapper.readValue(request, ObjectNode.class)),httpHeaders);
            AnalyticsRequest analyticsRequest=new AnalyticsRequest(installationDetails);
            HttpEntity<Object> entity = new HttpEntity(analyticsRequest, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.info(ConstantsAuth.RESPONSE_KEY + "error in Installation Details Api: ");
            log.info(e.toString());
            log.info(ConstantsAuth.RESPONSE_KEY + "error in Installation Details Api: ");
            log.info(e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void callAnalyticsPortalLastLoginDetailsApi(String request, Map<String, String> httpHeaders,String status) {
        try {
            String url = analyticsUrlBase + (analyticsUrlBase.endsWith("/") ? "" : "/") + ConstantsAuth.ANALYTICS_LAST_LOGIN_MAPPING;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            LastLoginDetails lastLoginDetails = new LastLoginDetails((objectMapper.readValue(request, ObjectNode.class)),httpHeaders);
            AnalyticsRequest analyticsRequest=new AnalyticsRequest(lastLoginDetails,status);
            HttpEntity<Object> entity = new HttpEntity(analyticsRequest, headers);

            String loginId = lastLoginDetails.getLoginId();

            if(loginId != null && !loginId.isEmpty() && !loginId.equalsIgnoreCase("null"))
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        } catch (Exception e) {
            log.info(ConstantsAuth.RESPONSE_KEY + "error in Last Login Details Api: ");
            log.info(e.toString());
            log.info(ConstantsAuth.RESPONSE_KEY + "error in Last Login Details Api: ");
            log.info(e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void callAnalyticsPortalSummaryDetailsApi(Map<String, String> httpHeaders,String status) {
        try {
            String url = analyticsUrlBase + (analyticsUrlBase.endsWith("/") ? "" : "/") + ConstantsAuth.ANALYTICS_SUMMARY_MAPPING;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            SummaryDetails summaryDetails = new SummaryDetails(httpHeaders);
            AnalyticsRequest analyticsRequest=new AnalyticsRequest(summaryDetails,status);
            HttpEntity<Object> entity = new HttpEntity(analyticsRequest, headers);

            String loginId = summaryDetails.getLoginId();

            if(loginId != null && !loginId.isEmpty() && !loginId.equalsIgnoreCase("null"))
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        } catch (Exception e) {
            log.info(ConstantsAuth.RESPONSE_KEY + "error in Summary Details Api: ");
            log.info(e.toString());
            log.info(ConstantsAuth.RESPONSE_KEY + "error in Summary Details Api: ");
            log.info(e.getMessage());
            e.printStackTrace();
        }
    }
}
