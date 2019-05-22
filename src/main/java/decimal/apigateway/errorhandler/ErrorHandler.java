package decimal.apigateway.errorhandler;

import exception.RouterException;
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
            return new RouterException(responseBody);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;

    }
}
