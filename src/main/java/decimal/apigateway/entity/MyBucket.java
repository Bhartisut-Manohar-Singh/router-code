//package decimal.apigateway.entity;
//
//import io.github.bucket4j.*;
//import io.github.bucket4j.local.LockFreeBucket;
//import lombok.NoArgsConstructor;
//
//import java.io.IOException;
//import java.io.ObjectInput;
//import java.io.ObjectOutput;
//import java.io.Serializable;
//import java.time.Clock;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicReference;
//
//
//public class MyBucket extends LockFreeBucket implements Serializable {
//    public MyBucket(BucketConfiguration configuration, MathType mathType) {
//        super(configuration,mathType, TimeMeter.SYSTEM_MILLISECONDS);
//    }
//
//    // Add custom methods or behavior specific to MyBucket if needed
//
//    // Custom serialization
//    private void writeObject(ObjectOutput out) throws IOException {
////        out.defaultWriteObject();
//        // Add any additional custom serialization logic if needed
//    }
//
//    // Custom deserialization
//    private void readObject(ObjectInput in) throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
//        // Add any additional custom deserialization logic if needed
//    }
//
//}
