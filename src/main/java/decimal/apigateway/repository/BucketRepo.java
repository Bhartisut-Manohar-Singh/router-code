package decimal.apigateway.repository;

import decimal.apigateway.entity.BucketConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BucketRepo extends CrudRepository<BucketConfig, String> {

}
