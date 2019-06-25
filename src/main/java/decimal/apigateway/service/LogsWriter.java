package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.LogsData;
import decimal.apigateway.service.masking.MaskService;
import decimal.kafka.Service.ProducerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static decimal.apigateway.commons.Loggers.ERROR_LOGGER;
import static decimal.apigateway.commons.Loggers.GENERAL_LOGGER;

@Service
public class LogsWriter
{
    @Value("${kafka.integration.url}")
    String kafkaUrl;

    @Value("${microServiceLogs}")
    String microServiceLogs;

    @Autowired
    MaskService maskService;

    @Autowired
    ObjectMapper objectMapper;

    @Async("myTaskExecutor")
    public void writeLogs(LogsData logsData)
    {
        String finalLogs;

        String requestTime=logsData.getRequestTimeStamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
        logsData.setRequestTimeStamp(LocalDateTime.parse(requestTime));

        String responseTime=logsData.getResponseTimeStamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
        logsData.setResponseTimeStamp(LocalDateTime.parse(responseTime));

        try
        {
            try
            {
                ApiLogFormatter apiLogFormatter = new ApiLogFormatter(logsData);
                apiLogFormatter.setData(objectMapper.convertValue(logsData, ObjectNode.class));
                finalLogs = objectMapper.writeValueAsString(apiLogFormatter);
                finalLogs = maskService.maskMessage(finalLogs);

                GENERAL_LOGGER.info(Thread.currentThread().getName());

            }
            catch (JsonProcessingException e)
            {
                finalLogs="{}";
            }

            if(microServiceLogs.equalsIgnoreCase("ON"))
            {
                GENERAL_LOGGER.info("Send request to push logs to kafka");

                ProducerServiceImpl producerService = new ProducerServiceImpl();

                producerService.executeProducer(finalLogs, kafkaUrl, Constant.LOGS_TOPIC);

                GENERAL_LOGGER.info("Logs has been pushed to kafka");
            }
            else {
                GENERAL_LOGGER.info("Logs is not enabled");
            }
        }
        catch (Exception e)
        {
            ERROR_LOGGER.error("Unable to push logs to Kafka", e);
        }
    }
}
