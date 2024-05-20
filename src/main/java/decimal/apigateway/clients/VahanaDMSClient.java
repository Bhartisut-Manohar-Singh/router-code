package decimal.apigateway.clients;

import decimal.apigateway.commons.Constant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@FeignClient(value = Constant.VAHANA_DMS)
public interface VahanaDMSClient {

    @PostMapping(value = "document-manager/upload/v1/uploadDocument", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<Object> uploadFile(@RequestHeader HttpHeaders httpHeaders, @RequestPart String mediaDataObjects, @RequestPart MultipartFile[] files);

    @PostMapping(value = "document-manager/upload/v1/uploadDocumentAndGetUrl", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<Object> uploadAndGetSignedUrl(@RequestHeader HttpHeaders httpHeaders, @RequestPart String mediaDataObjects, @RequestPart MultipartFile[] files);

    }
