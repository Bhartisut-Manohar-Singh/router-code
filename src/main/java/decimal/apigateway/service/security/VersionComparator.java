package decimal.apigateway.service.security;


import decimal.apigateway.domain.ServiceConfig;

import java.util.Comparator;

public class VersionComparator implements Comparator<ServiceConfig> {

    @Override
    public int compare(ServiceConfig webServiceConfig, ServiceConfig t1) {
        Double ver1 = Double.valueOf(webServiceConfig.getVersion().replace("v", ""));
        Double ver2 = Double.valueOf(t1.getVersion().replace("v", ""));

        return ver2.compareTo(ver1);
    }
}
