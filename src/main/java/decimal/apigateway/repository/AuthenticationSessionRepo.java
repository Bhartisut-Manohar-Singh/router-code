package decimal.apigateway.repository;

import decimal.apigateway.domain.Session;
import org.springframework.data.repository.CrudRepository;

public interface AuthenticationSessionRepo extends CrudRepository<Session, String> {
}
