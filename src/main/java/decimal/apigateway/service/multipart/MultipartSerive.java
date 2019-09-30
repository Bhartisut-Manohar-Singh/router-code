package decimal.apigateway.service.multipart;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MultipartSerive {

    Object uploadGateway(String data, MultipartFile[] files) throws IOException;

}
