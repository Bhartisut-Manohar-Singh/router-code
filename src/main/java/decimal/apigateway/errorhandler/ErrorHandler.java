package decimal.apigateway.errorhandler;

import decimal.apigateway.exception.RouterExceptionV1;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log
public class ErrorHandler implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {

        Object responseBody = null;

        try
        {
            responseBody = IOUtils.toString(response.body().asInputStream());


            log.info("Error received from target server with body: " + responseBody);

            return new RouterExceptionV1(responseBody);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;

    }
}
