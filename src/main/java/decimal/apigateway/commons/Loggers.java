package decimal.apigateway.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loggers {

    public static final Logger GENERAL_LOGGER = LogManager.getLogger(Constant.GENERAL_LOGGER);
    public static final Logger ERROR_LOGGER = LogManager.getLogger(Constant.ERROR_LOGGER);
    public static final Logger AUDIT_LOGGER = LogManager.getLogger(Constant.AUDIT_LOGGER);
    public static final Logger ADMIN_PORTAL_LOGGER = LogManager.getLogger(Constant.ADMIN_PORTAL_LOGGER);
}
