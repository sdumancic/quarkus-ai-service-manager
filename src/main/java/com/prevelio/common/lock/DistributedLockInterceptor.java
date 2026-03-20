package com.prevelio.common.lock;

import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.UUID;

@DistributedLock
@Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
@io.quarkus.arc.Unremovable
@Interceptor
public class DistributedLockInterceptor {

    private final RedisDataSource redisDataSource;

    public DistributedLockInterceptor(RedisDataSource redisDataSource) {
        this.redisDataSource = redisDataSource;
    }

    @AroundInvoke
    public Object manageLock(InvocationContext context) throws Exception {
        DistributedLock lockAnnotation = null;
        
        // Arc provides the bindings directly
        for (java.lang.annotation.Annotation binding : context.getInterceptorBindings()) {
            if (binding instanceof DistributedLock lock) {
                lockAnnotation = lock;
                break;
            }
        }

        if (lockAnnotation == null) {
            return context.proceed();
        }

        String lockKey = "dlock:" + lockAnnotation.key();
        long waitTimeMs = lockAnnotation.waitTimeMs();
        long leaseTimeMs = lockAnnotation.leaseTimeMs();
        String lockValue = UUID.randomUUID().toString();

        long start = System.currentTimeMillis();
        boolean locked = false;

        try {
            while (System.currentTimeMillis() - start < waitTimeMs) {
                try {
                    var response = redisDataSource.execute("SET", lockKey, lockValue, "NX", "PX", String.valueOf(leaseTimeMs));
                    if (response != null && "OK".equalsIgnoreCase(response.toString())) {
                        locked = true;
                        break;
                    }
                } catch (Exception e) {
                    // Fail-safe: continue loop
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new DistributedLockAcquisitionException("Thread interrupted while waiting for lock", e);
                }
            }

            if (!locked) {
                throw new DistributedLockAcquisitionException("Could not acquire distributed lock for key: " + lockKey);
            }

            return context.proceed();

        } finally {
            if (locked) {
                // Ensure we only delete our own lock
                var currentValue = redisDataSource.execute("GET", lockKey);
                if (currentValue != null && lockValue.equals(currentValue.toString())) {
                    redisDataSource.key().del(lockKey);
                }
            }
        }
    }
}
