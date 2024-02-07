package decimal.apigateway.repository;

import decimal.apigateway.domain.TxnKey;
import org.springframework.data.repository.CrudRepository;

public interface TxnKeyRepo extends CrudRepository<TxnKey, String> {
}
