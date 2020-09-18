package decimal.apigateway.errorhandler;

import decimal.apigateway.exception.RouterException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ErrorHandler implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {



        Object responseBody = null;

        try
        {
            responseBody = IOUtils.toString(response.body().asInputStream());


            System.out.println("Error received from target server with body: " + responseBody);

            return new RouterException(responseBody);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;

    }
}
