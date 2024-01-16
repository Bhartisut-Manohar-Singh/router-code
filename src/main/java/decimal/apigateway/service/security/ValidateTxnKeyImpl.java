package decimal.apigateway.service.security;

import decimal.apigateway.commons.Constants;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.TxnKey;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.Request;
import decimal.apigateway.repository.TxnKeyRepo;
import decimal.apigateway.repository.redis.RedisKeyValuePairRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/**
 * @author vikas
 */

@Log
@Service
@RefreshScope
public class ValidateTxnKeyImpl implements ValidateTxnKey {

    @Value("${retainTxnIdInRedisForMinutes}")
    int retainTxnIdInRedisForMinutes;

    @Autowired
    RedisKeyValuePairRepository redisKeyValuePairRepository;

    @Autowired
    TxnKeyRepo txnKeyRepo;

    @Autowired
    Request auditTraceFilter;
    
    public void validateTxnKey(String requestId, String txnId, String securityVersion) throws RouterException {

        log.info("Validating TxnId");

        Optional<TxnKey> txnIdFromRedis;
        try {
            //txnIdFromRedis = requestIdValidationRepository.get(txnId);
            txnIdFromRedis = txnKeyRepo.findById(txnId);
        } catch (Exception e) {
            log.info("Error in Validation TxnId module.It seems that Redis is not working.Exception: " +e);
            return;
        }

        if ((txnIdFromRedis == null) || (!txnIdFromRedis.isPresent())) {

            log.info("TxnId not found in redis. lets validate the format now Validating TxnId");
            SimpleDateFormat gmtDateFormat;
            try {
                gmtDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
                //gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date currentDate = null;
                if (securityVersion != null && securityVersion.equalsIgnoreCase("2")) {
                    currentDate = cvtToGmt(new Date());
                } else {
                    currentDate = new Date();
                }
                String timeInPwSessionId = txnId.substring(0, 15);
                Date pwSessionIdDate = gmtDateFormat.parse(timeInPwSessionId);

                long sessionIdTimediff = Math.abs(currentDate.getTime() - pwSessionIdDate.getTime());
                long diffInMinutes = sessionIdTimediff / (60 * 1000);
                log.info(" diffInMinutes " + diffInMinutes + " retainTxnIdInRedisForMinutes " + retainTxnIdInRedisForMinutes);
                if (diffInMinutes > retainTxnIdInRedisForMinutes) {
                    log.info("TxnId recieved from client is:" + txnId
                            + ". This txnId is not valid because difference in minutes with server is "
                            + diffInMinutes + ". Hence its invalid txnId.");
                    log.info(" ==== exception thrown ==== ");
                    throw new RouterException(RouterResponseCode.INVALID_TXN_ID, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Txn Id is not valid.");
                } else {
                    TxnKey txnKey = new TxnKey(txnId);
                    txnKeyRepo.save(txnKey);
                    redisKeyValuePairRepository.expireTxnKey(txnKey,retainTxnIdInRedisForMinutes);
                }
            } catch (RouterException ex) {
                throw ex;
            } catch (Exception e) {
                log.info("TxnId is not valid.Error:" + e.getMessage());
                throw new RouterException(RouterResponseCode.VALIDATING_TXN_ID_EXCEPTION, e, Constants.ROUTER_ERROR_TYPE_SECURITY, "Error when validating txn key");
            }

        } else {
            log.info("TxnId is duplicate.");
            throw new RouterException(RouterResponseCode.DUPLICATE_TXN_ID, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Txn Id is duplicate");
        }
    }

    private static Date cvtToGmt(Date date) {
        TimeZone tz = TimeZone.getDefault();
        Date ret = new Date(date.getTime() - tz.getRawOffset());
        // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
        if (tz.inDaylightTime(ret)) {
            Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());
            // check to make sure we have not crossed back into standard time
            // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
            if (tz.inDaylightTime(dstDate)) {
                ret = dstDate;
            }
        }
        return ret;
    }
}

