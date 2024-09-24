package decimal.apigateway.commons;

public class RouterResponseCode {
  private RouterResponseCode() {}

  public static final String SERVICE_NAME_NOTNULL = "ROUTER_VALIDATION_SERVICE_NAME";
  public static final String ROUTER_RECORD_EXECUTION = "ROUTER_RECORD_EXECUTION";
  public static final String ROUTER_RECORD_EXECUTION_TIMEOUT = "ROUTER_RECORD_TIMEOUT_EXECUTION";
  public static final String ROUTER_API_EXECUTION = "ROUTER_API_EXECUTION";
  public static final String ROUTER_SERVICE_EXECUTION_NULLPOINTER= "ROUTER_SERVICE_NULL_ERROR";
  public static final String ROUTER_SERVICE_EXECUTION = "ROUTER_SERVICE_EXECUTION_ERROR";
  public static final String ROUTER_SERVICE_NOT_FOUND = "ROUTER_SERVICE_NAME_NOT_FOUND";
  public static final String ROUTER_SERVICE_NOT_FOUND_FOR_VERSION = "ROUTER_SERVICE_NOT_FOUND_FOR_VERSION";
  public static final String ROUTER_SERVICE_API_GROUP_NOT_FOUND = "ROUTER_SERVICE_APIGROUP_EXECUTION";
  public static final String ROUTER_IGW_EXECUTION = "ROUTER_IGW_EXECUTION";
  public static final String ROUTER_TARGETSERVER_EXECUTION = "ROUTER_TARGERSERVER_EXECUTION";
  public static final String ROUTER_SERVICE_NOT_ACTIVE = "ROUTER_SERVICE_NAME_INACTIVE";
  public static final String ROUTER_API_ID_NOT_FOUND = "ROUTER_API_ID_EXECUTION";
  public static final String ROUTER_SERVICE_API_NOT_ACTIVE = "ROUTER_SERVICE_API_INACTIVE";
  public static final String ROUTER_RESPONSE_PREPARE = "ROUTER_RESPONSE_PREPARE_ERROR";
  
  public static final String APPID_DEF_NOT_FOUND = "ROUTER_VALIDATION_APPLICATION_NOT_FOUND";
  public static final String APPID_DEF_NOT_ACTIVE = "ROUTER_VALIDATION_APPLICATION_NOT_ACTIVE";
  public static final String REQUEST_TRANSFORMATION_GENERIC_ERROR = "ROUTER_REQUEST_TRANSFORM_ERROR";
  public static final String RESPONSE_TRANSFORMATION_GENERIC_ERROR = "ROUTER_RESPONSE_TRANSFORM_ERROR";
  public static final String ROUTER_NO_API_FOUND_FOR_SERVICE = "ROUTER_NO_API_FOUND_FOR_SERVICE";
  public static final String ROUTER_TARGET_SERVER_ENDPOINT_URL = "ROUTER_TARGET_SERVER_ENDPOINT_URL";
  public static final String ROUTER_TARGET_SERVER_OUTBOUND_CALL_TYPE = "ROUTER_TARGET_SERVER_OUTBOUND_CALL_TYPE";
  public static final String ROUTER_SERVICENAMES_MISMATCH_ERROR = "ROUTER_SERVICENAMES_MISMATCH_ERROR";
  public static final String ROUTER_SERVICENAMES_MISMATCH_EXCEPTION = "ROUTER_SERVICENAMES_MISMATCH_EXCEPTION";
  public static final String ROUTER_USERNAME_MISSING = "ROUTER_USERNAME_MISSING";
  public static final String ROUTER_TXNKEY_MISSING = "ROUTER_TXNKEY_MISSING";
  public static final String ROUTER_HASH_MISSING = "ROUTER_HASH_MISSING";
  public static final String ROUTER_AUTH_TOKEN_MISSING = "ROUTER_AUTH_TOKEN_MISSING";


  public static final String ROUTER_NOUNCE_MISSING = "ROUTER_NOUNCE_MISSING";
  public static final String ROUTER_CLIENT_ID_MISSING = "ROUTER_CIENT_ID_MISSING";

  public static final String ROUTER_INVALID_USERNAME = "ROUTER_INVALID_USERNAME";
  public static final String ROUTER_CLIENT_SECRET_MISSING = "ROUTER_CLIENT_SECRET_MISSING";
  public static final String ROUTER_REQUEST_ID_MISSING = "ROUTER_REQUEST_ID_MISSING";
  public static final String ROUTER_ENVIRONMENT_PROPERTY_MISSING = "ROUTER_ENVIRONMENT_PROPERTY_MISSING";
  public static final String APPLICATION_DEF_NOT_FOUND = "APPLICATION_DEF_NOT_FOUND";



  // Security checks related response codes
  public static final int HTTP_SUCCESS_CODE = 200;
  public static final String DECODE_HEX_STRING = "520";
  public static final String AUTH_HEADER_DATA_DECRYPTION_ERROR = "521";
  public static final String REQUEST_DATA_DECRYPTION_ERROR = "522";
  public static final String INVALID_HTTP_HEADER_HASH = "523";
  public static final String ERROR_GENERATING_RSA_KEYS = "527";
  public static final String INVALID_REQUEST_SCOPE = "528";
  public static final String INVALID_NOUNCE = "529";
  public static final String INVALID_NOUNCE_TIME = "530";
  public static final String BLANK_HTTP_HEADER_HASH = "531";
  public static final String REQUEST_HASH_MISMATCH = "532";
  public static final String REQUEST_HASH_MISMATCH_EXCEPTION = "533";
  public static final String DUPLICATE_TXN_ID="534";
  public static final String INVALID_TXN_ID="535";
  public static final String VALIDATING_TXN_ID_EXCEPTION="536";
  public static final String TXN_ID_DECRYPTION_ERROR = "537";
  public static final String JWT_PARSING_ERROR = "538";
  public static final String JWT_DECRYPTION_ERROR = "539";
  public static final String NOUNCE_VALIDATION_ERROR = "540";
  public static final String HEADER_SECRET_VALIDATION_ERROR = "541";
  public static final String INVALID_SESSION_PARAMETERS= "542";
  public static final String INVALID_SESSION_EXPIRY_PARAMETERS= "543";
  public static final String JWT_CREATION_ERROR= "544";
  public static final String HMAC_DECRYPTION_ERROR= "545";
  public static final String INVALID_SIGNATURE= "549";

  public static final String NON_AUTHENTICATED_REMOTE_ADDRESS= "546";
  public static final String INVALID_CLIENT_SECRET= "547";

  // Session related response codes
  public static final String ROUTER_KILL_SESSION_FAIL = "620";
  public static final String ROUTER_MULTIPLE_SESSION="621";
  public static final String INVALID_USER_SESSION = "622";
  public static final String ROUTER_KILL_SESSION_SUCCESS = "625";
  public static final String INVALID_APPLICATION_SESSION="627";
  public static final String SESSION_VALIDATION_GENERIC_ERROR="628";
  public static final String APPLICATION_SESSION_CREATION_ERROR= "629";
  public static final String USER_SESSION_CREATION_ERROR= "630";
  public static final String USER_SESSION_DELETE_ERROR= "631";
  public static final String INACTIVE_USER_SESSION= "622";
  
  // Authentication related
  public static final String ROUTER_AUTH_FAIL = "ROUTER_AUTH_FAIL";
  public static final String ROUTER_AUTH_FAIL_EXCEPTION = "ROUTER_AUTH_FAIL_EXCEPTION";

  // Authentication related response codes
  public static final int HTTP_BAD_REQUEST=400;
  public static final String APP_AUTHENTICATION_FAILURE = "401";
  public static final String USER_AUTHENTICATION_FAILURE = "402";
  public static final String ERROR_IN_PROCESSING_REQUEST = "500";
  public static final String SOURCE_APP_AUTHENTICATION_FAILURE = "401";


  //Generic Error
  
  public static final String ORGID_APPID_ERROR= "700";
  public static final String SET_ACCOUNT_DATA_ERROR= "701";
  public static final String DEVICE_DETAILS_REGISTRATION_ERROR= "703";

  public static final String LAST_LOGIN_DETAILS_STORE_DB_ERROR= "705";
  
}
