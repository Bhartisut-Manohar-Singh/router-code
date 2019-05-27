package decimal.apigateway.service;

import decimal.apigateway.model.LogsData;
import org.springframework.stereotype.Service;

@Service
public class LogsWriter
{
    public void writeLogs(LogsData logsData)
    {

       /* try
        {
            try {
                finalLogs = objectMapper.writeValueAsString(logsData);
            } catch (JsonProcessingException e) {
                finalLogs="{}";
            }

            ProducerServiceImpl producerService = new ProducerServiceImpl();
            producerService.executeProducer(finalLogs, kafkaUrl);

            System.out.println("Final logsData object: " + Jackson.objectToJsonString(logsData));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
