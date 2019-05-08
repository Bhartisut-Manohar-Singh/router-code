package decimal.apigateway.errorhandler;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ErrorHandler implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response)
    {
        System.out.println("Error while executing an api");

        try {
            String responseBody = IOUtils.toString(response.body().asInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}
