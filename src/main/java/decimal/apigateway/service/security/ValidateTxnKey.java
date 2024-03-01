/**
 * 
 */
package decimal.apigateway.service.security;

import decimal.apigateway.exception.RouterException;

/**
 * @author vikas
 *
 */
public interface ValidateTxnKey {
	void validateTxnKey(String requestId, String txnId, String securityVersion) throws RouterException;
}
