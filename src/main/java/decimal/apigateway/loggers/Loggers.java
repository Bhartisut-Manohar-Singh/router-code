package decimal.apigateway.loggers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loggers
{
    public static final Logger GENERAL_LOGGER = LogManager.getLogger("general");
    public static final Logger ERROR_LOGGER = LogManager.getLogger("error");
    public static final Logger AUDIT_LOGGER = LogManager.getLogger("audit");
    public static final Logger ADMIN_PORTAL_LOGGER = LogManager.getLogger("admin-portal");
}
