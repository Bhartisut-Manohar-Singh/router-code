package decimal.apigateway.service.security;

import decimal.apigateway.commons.AuthCryptoUtil;
import decimal.apigateway.commons.AuthCryptoUtilV2;
import decimal.apigateway.commons.CryptoUtilV3Auth;
import decimal.apigateway.commons.ICryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CryptoFactory {

    @Autowired
    ApplicationContext applicationContext;

    public ICryptoUtil getSecurityVersion(String securityVersion, String requestId) {

        if (null != securityVersion) {
            switch (securityVersion) {
                case "3":
                    System.out.println("=====================securityVersion is 3======================");
                    return (ICryptoUtil) applicationContext.getBean(CryptoUtilV3Auth.class);
                case "2":
                    System.out.println("=====================securityVersion is 2======================");
                    return (ICryptoUtil) applicationContext.getBean(AuthCryptoUtilV2.class);
                default:
                    System.out.println("=====================securityVersion is "+securityVersion+"======================");
                    return (ICryptoUtil) applicationContext.getBean(AuthCryptoUtil.class);
            }
        } else {
            System.out.println("=====================securityVersion is NULL:"+securityVersion+"======================");
            return (ICryptoUtil) applicationContext.getBean(AuthCryptoUtilV2.class);
        }
     //   return null != securityVersion && securityVersion.equals("2") ? applicationContext.getBean(CryptoUtilV2.class) : applicationContext.getBean(CryptoUtil.class);
    }
}
