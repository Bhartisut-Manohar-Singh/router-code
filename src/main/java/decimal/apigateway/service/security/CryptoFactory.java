package decimal.apigateway.service.security;

import decimal.apigateway.commons.CryptoUtilV2;
import decimal.apigateway.commons.CryptoUtilV3;
import decimal.apigateway.commons.ICryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import decimal.apigateway.commons.CryptoUtil;


@Component
public class CryptoFactory {

    @Autowired
    ApplicationContext applicationContext;

    public ICryptoUtil getSecurityVersion(String securityVersion, String requestId) {

        if (null != securityVersion) {
            switch (securityVersion) {
                case "3":
                    System.out.println("=====================securityVersion is 3======================");
                    return  applicationContext.getBean(CryptoUtilV3.class);
                case "2":
                    System.out.println("=====================securityVersion is 2======================");
                    return  applicationContext.getBean(CryptoUtilV2.class);
                default:
                    System.out.println("=====================securityVersion is "+securityVersion+"======================");
                    return  applicationContext.getBean(CryptoUtil.class);
            }
        } else {
            System.out.println("=====================securityVersion is NULL:"+securityVersion+"======================");
            return  applicationContext.getBean(CryptoUtil.class);
        }
     //   return null != securityVersion && securityVersion.equals("2") ? applicationContext.getBean(CryptoUtilV2.class) : applicationContext.getBean(CryptoUtil.class);
    }
}
