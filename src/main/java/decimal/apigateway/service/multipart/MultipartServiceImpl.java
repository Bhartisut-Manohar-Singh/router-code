package decimal.apigateway.service.multipart;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
public class MultipartServiceImpl implements MultipartSerive
{

    @Value("${dms.upload.url}")
    private String dmsUploadUrl;


    @Bean
    public RestTemplate restTemplate() {
//                ArrayList<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(
//                Arrays.asList(new ByteArrayHttpMessageConverter(),new MappingJackson2HttpMessageConverter(), new ResourceHttpMessageConverter()));
        return new RestTemplate();
    }




    @Override
    public Object uploadGateway(String data, MultipartFile[] files) throws IOException {

        ResponseEntity<String> out=null;
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for(MultipartFile file :files){
            body.add(Constant.MULTIPART_FILES, new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        }
        body.add("uploadRequest",data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body,headers);

        try {
            out=restTemplate().postForEntity(dmsUploadUrl,requestEntity, String.class);
        }catch (Exception e) {
            e.printStackTrace();
         }
        MicroserviceResponse microserviceResponse = new MicroserviceResponse();
        if(null!=out && out.getStatusCode().value()==200) {
            microserviceResponse.setStatus("SUCCESS");
            microserviceResponse.setMessage("Service Executed successfully");
            microserviceResponse.setResponse(out.getBody());
        }
        else{
            microserviceResponse.setStatus("FAILURE");
            microserviceResponse.setMessage("Upload Service Failure");
        }

        return microserviceResponse;
    }
}
