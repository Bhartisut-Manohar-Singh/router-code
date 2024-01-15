package decimal.apigateway.commons;

import java.util.Arrays;
import java.util.List;

public class Constants {


    public static final String ROUTER_HEADER_SESSION_TYPE = "session";
    public static final String ROUTER_ERROR_TYPE_SECURITY = "SECURITY";
    public static final String ROUTER_ERROR_TYPE_VALIDATION = "VALIDATION";
    public static final String ROUTER_ERROR_TYPE_EXECUTION = "EXECUTION";
    public static final String ROUTER_ERROR_TYPE_SYSTEM = "SYSTEM";
    public static final String GENERAL_LOGGER = "general";
    public static final String ERROR_LOGGER = "error";
    public static final String AUDIT_LOGGER = "audit";
    public static final String ADMIN_PORTAL_LOGGER = "adminPortal";
    public static final String INVALID_VALIDATION_TYPE = "INVALID_VALIDATION_TYPE";
    public static final String ACTIVE = "ACTIVE";
    public static final String NO = "N";
    public static final String ERROR_CODE = "400";
    public static final String KEYS_TO_MASK = "keys_to_mask";


    private Constants() {
    }

    public static final String ROUTER_KEYWORD = "router";
    public static final String ROUTER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TILD_SPLITTER = "~";

    public static final String COLON = ":";
    public static final String ROUTER_HEADER_USERNAME = "username";
    public static final String ROUTER_HEADER_PASSWRD = "password";
    public static final String ROUTER_HEADER_REQUEST_ID = "requestid";
    public static final String ROUTER_HEADER_SERVICENAME = "servicename";
    public static final String ROUTER_HEADER_SERVICE_VERSION = "version";
    public static final String ROUTER_HEADER_TXN_KEY = "txnkey";
    public static final String ROUTER_HEADER_JWT = "Authorization";
    public static final String ROUTER_HEADER_APP_JWT = "appJWT";
    public static final String ROUTER_REQUEST = "request";
    public static final String ROUTER_HEADER_ORG_ID = "orgid";
    public static final String ROUTER_HEADER_APP_ID = "appid";
    public static final String ROUTER_HEADER_IMEI_ID = "deviceid";
    public static final String ROUTER_HEADER_LOGIN_ID = "loginid";
    public static final String ROUTER_HEADER_USER_SESSION = "USER";
    public static final String ROUTER_HEADER_APP_SESSION = "APP";

    public static final String RSA_KEYS = "rsa";
    public static final String HTTP_HEADER_INPUT_REQUEST = "header";
    public static final String INTERFACEOBJ_INPUT_REQUEST = "interface";
    public static final String IS_FORCE_LOGIN = "isforcelogin";
    public static final String ROUTER_HEADER_PLATFORM = "platform";
    public static final String ROUTER_HEADER_CLIENT_DETAILS = "user-agent";
    public static final String ROUTER_HEADER_SOURCE_IP = "X-FORWARDED-FOR";
    public static final String ROUTER_HEADER_SECURITY_VERSION = "security-version";
    public static final String ROUTER_HEADER_CUSTOM_HEADER = "custom_headers";
    public static final String API_CATEGORY_ADMIN = "ADMIN";
    public static final String CLIENT_ID="clientid";
    public static final String CLIENT_SECRET="clientsecret";

    public static final String AUTHORIZATION_HEADER = "authorization";

    public static final String ROUTER_HEADER_APPLICATION_VERSION = "APPLICATION_VERSION";
    public static final String ROUTER_HEADER_SIM_ID = "SIM_ID";
    public static final String ROUTER_HEADER_OS_VERSION = "OS_VERSION";
    public static final String ROUTER_HEADER_IMEI_NO = "IMEI_NO";
    public static final String ROUTER_HEADER_DEVICE_MAKE = "DEVICE_MAKE";
    public static final String ROUTER_HEADER_DEVICE_MODEL = "DEVICE_MODEL";
    public static final String ROUTER_HEADER_DEVICE_LATITUDE = "DEVICE_LATITUDE";
    public static final String ROUTER_HEADER_DEVICE_LONGITUDE = "DEVICE_LONGITUDE";

    public static final int MAX_NOOF_CONCURRENCY_IN_SERVICE_EXECUTION = 3;
    public static final int MAX_NOOF_CONCURRENCY_IN_RECORD_EXECUTION = 5;
    public static final int DEFAULT_CACHING_TIME_IN_REDIS_MINUTES = 60;
    public static final int DEFAULT_CACHING_TIME_IN_DB_MINUTES = 1440;


    public static final String PARALLEL_EXECUTION_MODE = "PARALLEL";

    // Database related
    public static final String SERVICE_DEF_TABLE_NAME = "router_service_def";
    public static final String IS_PAYLOAD_ENCRYPTED = "ispayloadencrypted";
    public static final String IS_DIGITALLY_SIGNED = "isdigitallysigned";
    public static final String FAILURE_STATUS = "FAILURE";
    public static final String SUCCESS_STATUS = "SUCCESS";
    public static final String INACTIVE = "INACTIVE";


    public static final List<String> getSecuredKeys() {
        return Arrays.asList(ROUTER_HEADER_PASSWRD, ROUTER_HEADER_JWT, "private-exponent", "private-exponent");
    }

}
