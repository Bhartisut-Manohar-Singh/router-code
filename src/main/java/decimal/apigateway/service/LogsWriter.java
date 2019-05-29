package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import decimal.apigateway.commons.Jackson;
import decimal.apigateway.model.LogsData;
import decimal.apigateway.service.masking.MaskService;
import decimal.kafka.Service.ProducerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static decimal.apigateway.commons.Loggers.ERROR_LOGGER;
import static decimal.apigateway.commons.Loggers.GENERAL_LOGGER;

@Service
public class LogsWriter
{
    @Value("${kafka.integration.url}")
    String kafkaUrl;

    @Autowired
    MaskService maskService;

    @Async
    public void writeLogs(LogsData logsData)
    {
        String finalLogs;
        try
        {
            try
            {
                finalLogs = Jackson.objectToJsonString(logsData);

                finalLogs = maskService.maskMessage(finalLogs);
            }
            catch (JsonProcessingException e)
            {
                finalLogs="{}";
            }

            GENERAL_LOGGER.info("Send request to push logs to kafka");

            ProducerServiceImpl producerService = new ProducerServiceImpl();

            producerService.executeProducer(finalLogs, kafkaUrl);

            GENERAL_LOGGER.info("Logs has been pushed to kafka");

        }
        catch (IOException e)
        {
            ERROR_LOGGER.error("Unable to push logs to Kafka", e);
        }
    }
}