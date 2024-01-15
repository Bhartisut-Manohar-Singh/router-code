package decimal.apigateway.service.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.ConstantsAuth;
import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.domain.SSOTokenRedis;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.SSOTokenModel;
import decimal.apigateway.model.SSOTokenResponse;
import decimal.apigateway.repository.SSOTokenRepo;
import decimal.apigateway.service.AuthApplicationDefConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class VahanaSSOServiceImpl implements VahanaSSOService{

    @Autowired
    SSOTokenRepo ssoTokenRepo;

    @Autowired
    AuthenticationProcessor authenticationProcessor;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthApplicationDefConfig applicationDefConfig;


    @Override
    public Object generateSSOToken(SSOTokenModel ssoTokenModel, Map<String,String> httpHeaders) throws RouterException, IOException {
        Boolean aBoolean = validateGenerateTokenRequest(ssoTokenModel, httpHeaders);
        if(aBoolean) {
            Optional<SSOTokenRedis> ssoTokenRedis = ssoTokenRepo.findByOrgIdAndAppIdAndLoginId(ssoTokenModel.getOrgId(), ssoTokenModel.getAppId(), ssoTokenModel.getLoginId());
            if (ssoTokenRedis.isPresent()) {
                ssoTokenRedis.get().setSsoToken(UUID.randomUUID().toString());
                ssoTokenRedis.get().setExpiryTime("5"); // value to be taken from configuration hash.
                //update sso login details data
                ssoTokenRedis.get().setData(ssoTokenModel.getData());
                //
                ssoTokenRepo.save(ssoTokenRedis.get());
                return new SSOTokenResponse(ConstantsAuth.SUCCESS_STATUS, ConstantsAuth.TOKEN_MSG, null, ssoTokenRedis.get().getSsoToken());
            } else {
                SSOTokenRedis ssoTokenRedis1 = new SSOTokenRedis(ssoTokenModel);
                ssoTokenRepo.save(ssoTokenRedis1);
                return new SSOTokenResponse(ConstantsAuth.SUCCESS_STATUS, ConstantsAuth.TOKEN_MSG, null, ssoTokenRedis1.getSsoToken());
            }
        }
        else{
            return new SSOTokenResponse(ConstantsAuth.FAILURE_STATUS, ConstantsAuth.TOKEN_MSG_FAILURE, ConstantsAuth.ERROR_DETAILS, null);
        }
    }

    private Boolean validateGenerateTokenRequest(SSOTokenModel ssoTokenModel, Map<String, String> httpHeaders) throws RouterException, IOException {
        Boolean status=true;
        List<String> clientId = AuthRouterOperations.getStringArray(httpHeaders.get(ConstantsAuth.CLIENT_ID), "~");

        String orgId = clientId.get(0);
        String appId = clientId.get(1);


        ApplicationDef applicationDef = applicationDefConfig.findByOrgIdAndAppId(ssoTokenModel.getOrgId(), ssoTokenModel.getAppId());// null chec
        if(applicationDef!=null && applicationDef.getIsSSOEnabled()!=null){
         if("N".equalsIgnoreCase(applicationDef.getIsSSOEnabled())){
          status=false;
      }
      else {
             List<Map<String,String>> allowedOrgAppDtos = objectMapper.readValue(applicationDef.getAllowedOrgApp(),List.class);
             return allowedOrgAppDtos.stream().anyMatch(allowedOrgAppDto -> allowedOrgAppDto.get("orgId").equals(orgId) && allowedOrgAppDto.get("appId").equals(appId));
         }

      }
        else{
            status=false;
        }
      return status;


    }

    @Override
    public Object validateSSOToken(SSOTokenModel ssoTokenModel,Map<String,String> httpHeaders) throws IOException, RouterException {// ORG APP HEADER ME SE
           return authenticationProcessor.authenticate(objectMapper.writeValueAsString(ssoTokenModel),httpHeaders);

    }

    @Override
    public Object ssoLoginDetails(SSOTokenModel ssoTokenModel, Map<String, String> httpHeaders) throws IOException, RouterException {// ORG APP HEADER ME SE

        Optional<SSOTokenRedis> ssoUserDetails = ssoTokenRepo.findByOrgIdAndAppIdAndLoginIdAndSsoToken(
                ssoTokenModel.getOrgId(), ssoTokenModel.getAppId(), ssoTokenModel.getLoginId(), ssoTokenModel.getSsoToken());

        return ssoUserDetails.map(ssoTokenRedis -> new SSOTokenResponse(ConstantsAuth.SUCCESS_STATUS,
                        "SSO Login Details received successfully", null, null, ssoTokenRedis.getData()))
                .orElseGet(() -> new SSOTokenResponse(ConstantsAuth.FAILURE_STATUS, "NO_DATA_FOUND",
                        "No Login Details Found for the requested details",
                        null));
    }
}
