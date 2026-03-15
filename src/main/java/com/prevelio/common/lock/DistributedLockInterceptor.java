package com.prevelio.common.lock;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.UUID;

@DistributedLock
@Priority(1)
@io.quarkus.arc.Unremovable
@Interceptor
public class DistributedLockInterceptor {

    @Inject
    RedisDataSource redisDataSource;

    public DistributedLockInterceptor() {
    }

    @AroundInvoke
    public Object manageLock(InvocationContext context) throws Exception {
        DistributedLock lockAnnotation = context.getMethod().getAnnotation(DistributedLock.class);
        if (lockAnnotation == null) {
            lockAnnotation = context.getTarget().getClass().getAnnotation(DistributedLock.class);
        }

        if (lockAnnotation == null) {
            // Check bindings as last resort
            for (java.lang.annotation.Annotation binding : context.getInterceptorBindings()) {
                if (binding instanceof DistributedLock lock) {
                    lockAnnotation = lock;
                    break;
                }
            }
        }

        if (lockAnnotation == null) {
            // Technically unreachable if triggered by the binding, but good for safety
            return context.proceed();
        }

        String lockKey = "dlock:" + lockAnnotation.key();
        long waitTimeMs = lockAnnotation.waitTimeMs();
        long leaseTimeMs = lockAnnotation.leaseTimeMs();

        ValueCommands<String, String> valueCommands = redisDataSource.value(String.class);
        String lockValue = UUID.randomUUID().toString();

        long start = System.currentTimeMillis();
        boolean locked = false;

        try {
            while (System.currentTimeMillis() - start < waitTimeMs) {
                // Try to acquire the lock atomically (SET NX PX)
                // We use execute because ValueCommands.set returns void and doesn't tell us if it succeeded
                try {
                    var response = redisDataSource.execute("SET", lockKey, lockValue, "NX", "PX", String.valueOf(leaseTimeMs));
                    if (response != null && "OK".equalsIgnoreCase(response.toString())) {
                        locked = true;
                        break;
                    }
                } catch (Exception e) {
                    // Log error if needed
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while waiting for lock", e);
                }
            }

            if (!locked) {
                throw new RuntimeException("Could not acquire distributed lock for key: " + lockKey);
            }

            // Proceed with method execution
            return context.proceed();

        } finally {
            if (locked) {
                // Ensure we only delete our own lock
                String currentValue = valueCommands.get(lockKey);
                if (lockValue.equals(currentValue)) {
                    redisDataSource.key().del(lockKey);
                }
            }
        }
    }
}
