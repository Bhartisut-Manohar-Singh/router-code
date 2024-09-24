package decimal.apigateway.commons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import decimal.apigateway.model.*;
import decimal.apigateway.exception.RouterError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static decimal.apigateway.commons.Loggers.*;

@Service
@SuppressWarnings("WeakerAccess")
public class ResponseOperations {

  private static final String ERROR = "error";
  private static final String FAILURE_STATUS = "FAILURE";


  @Autowired
  RouterResponse response;

  public RouterResponse parseServicesOutputList(RouterOutput routerOutput, String requestId) throws IOException {

    GENERAL_LOGGER.info("Processing response to Response Operations", requestId);
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode servicesNode = mapper.createObjectNode();
    ObjectNode finalResponse = mapper.createObjectNode();

    response.setStatus(Constant.SUCCESS_STATUS);
    if (routerOutput.getRouterError() == null) {
      for (ServiceOutput serviceOutput : routerOutput.getServiceOutputList()) {
        try {
          parseServiceOutputResponse(serviceOutput, servicesNode);
        } catch (Exception e) {
          ObjectNode o = mapper.createObjectNode();
          o.set(ERROR,
              Jackson.objectToJson(new RouterError("ROUTER_RESPONSE_PREPARE", e)));
          servicesNode.set(serviceOutput.getServiceName(), o);
          response.setStatus(FAILURE_STATUS);
        }
      }
      finalResponse.set("services", servicesNode);
    } else {
      finalResponse = generateErrorObject(routerOutput.getRouterError());
      response.setStatus(FAILURE_STATUS);
    }

    AUDIT_LOGGER.info( "Final Response:" + finalResponse, requestId);
    response.setResponse(finalResponse);
    return response;
  }

  private ObjectNode generateErrorObject(RouterError routerException) throws IOException {
    response.setStatus(FAILURE_STATUS);
    return (ObjectNode) Jackson.objectToJson(routerException);
  }

  private ObjectNode generateAPIObject(List<ApiOutput> listApiOutput, ObjectNode apisObject)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    for (ApiOutput apiOutput : listApiOutput) {
      ObjectNode apiObject = mapper.createObjectNode();
      String apiName = apiOutput.getApiName();
      RouterError apiLevelError = apiOutput.getApiError();
      if (apiLevelError == null) {
        List<RecordOutput> recordsOutputList = apiOutput.getRecordOutput();
        ArrayNode recordsArray = generateRecordsArrayObject(recordsOutputList);
        apiObject.set("records", recordsArray);
        apiObject.put("responseHandledBy", apiOutput.getResponseHandledBy());
        if (apiOutput.getResponseHandledBy() != null
            && apiOutput.getResponseHandledBy().equalsIgnoreCase("PWC")) {
          apiObject.put("tableName", apiOutput.getPwcTableName());
          apiObject.put("responseHandleType", apiOutput.getResponseHandleType());
          apiObject.put("whereCondition", apiOutput.getWhereCondition());
        }
        apiObject.put("isSuccessRuleValidated", apiOutput.getSuccessRuleValidation());

      } else {
        // Handle Record level Data
        apiObject.set(ERROR, Jackson.objectToJson(apiLevelError));
        response.setStatus(Constant.FAILURE_STATUS);
      }
      apisObject.set(apiName, apiObject);
    }

    return apisObject;
  }

  private ObjectNode parseServiceObjectError(ObjectNode object, ServiceOutput serviceOutput,
                                             RouterError routerError) throws IOException {
    // Handle Record level Data
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode o = mapper.createObjectNode();
    o.set(ERROR, Jackson.objectToJson(routerError));
    object.set(serviceOutput.getServiceName(), o);
    response.setStatus(Constant.FAILURE_STATUS);
    return object;
  }

  private ObjectNode parseServiceOutputResponse(ServiceOutput serviceOutput,
                                                ObjectNode servicesNode) throws IOException {

    if (serviceOutput.getServiceError() == null) {
      List<ApiOutput> listApiOutput = serviceOutput.getServiceOutput();
      generateAPIObject(listApiOutput, servicesNode);
    } else {
      parseServiceObjectError(servicesNode, serviceOutput, serviceOutput.getServiceError());
      response.setStatus(Constant.FAILURE_STATUS);
    }
    return servicesNode;

  }

  private ArrayNode generateRecordsArrayObject(List<RecordOutput> recordsOutputList)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    ArrayNode recordsArray = mapper.createArrayNode();
    for (RecordOutput recordOutput : recordsOutputList) {
      ObjectNode oneRecordData = mapper.createObjectNode();
      Object primaryKeyValue = recordOutput.getPrimaryKeyValue();

      oneRecordData.put("primary_key", primaryKeyValue.toString());
      if (recordOutput.getRecordError() == null) {
        JsonNode recordsOutput = Jackson
                .stringToJsonNode(recordOutput.getRecordOutputArray());

        if (recordsOutput != null && recordsOutput.getNodeType() == JsonNodeType.ARRAY) {
          oneRecordData.set("data", recordsOutput);
        } else {
          oneRecordData.set("data", Jackson.objectNodeToArrayNode(recordsOutput));
        }

      } else {
        // Handle Record level Data
        response.setStatus(Constant.FAILURE_STATUS);
        oneRecordData.set(ERROR, Jackson.stringToJsonNode(recordOutput.getRecordError()));
      }
      recordsArray.add(oneRecordData);
    }

    return recordsArray;
  }

  public static String getPrimaryKeyValueForRecord(ObjectNode singleRecordObject,
                                                   String recordPrimaryKeyKeyName, String requestId) {
    StringBuffer value = new StringBuffer();
    String primaryKey="";

    if(recordPrimaryKeyKeyName!=null && !recordPrimaryKeyKeyName.isEmpty())
      try {
        ObjectMapper mapper=new ObjectMapper();
        String objectData=mapper.writeValueAsString(singleRecordObject);
        List<String> priKeysList= RouterOperationsV1.getStringArray(recordPrimaryKeyKeyName,Constant.TILD_SPLITTER);
        for(String priKey:priKeysList){
          value.append(JsonPath.read(objectData,priKey)+Constant.TILD_SPLITTER);
        }
        if(value.toString().endsWith(Constant.TILD_SPLITTER))
        {
          primaryKey=value.substring(0,value.toString().length()-1);
        }
      } catch (Exception e) {

        ERROR_LOGGER.error("Primary key not found", requestId);
      }
    return primaryKey;
  }


  public ObjectNode prepareResponseObject(String requestId, String serviceName, String jsonString) throws IOException {

    RecordOutput recordOutput = new RecordOutput("");
    recordOutput.setRecordOutputArray(jsonString);
    List<RecordOutput> recordOutputList = new ArrayList<>();
    recordOutputList.add(recordOutput);

    ApiOutput apiOutput = new ApiOutput();
    apiOutput.setRecordOutput(recordOutputList);
    apiOutput.setApiName(serviceName);
    List<ApiOutput> apiOutputList = new ArrayList<>();
    apiOutputList.add(apiOutput);

    ServiceOutput serviceOutput = new ServiceOutput(serviceName);
    serviceOutput.setServiceOutput(apiOutputList);
    List<ServiceOutput> serviceOutputList = new ArrayList<>();
    serviceOutputList.add(serviceOutput);

    RouterOutput routerOutput = new RouterOutput();
    routerOutput.setServiceOutputList(serviceOutputList);

    return parseServicesOutputList(routerOutput, requestId).getResponse();
  }

}
