package com.prevelio.common.lock;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.UUID;

@DistributedLock
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class DistributedLockInterceptor {

    private final RedisDataSource redisDataSource;

    @Inject
    public DistributedLockInterceptor(RedisDataSource redisDataSource) {
        this.redisDataSource = redisDataSource;
    }

    @AroundInvoke
    public Object manageLock(InvocationContext context) throws Exception {
        DistributedLock lockAnnotation = context.getMethod().getAnnotation(DistributedLock.class);
        if (lockAnnotation == null) {
            lockAnnotation = context.getTarget().getClass().getAnnotation(DistributedLock.class);
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
                // This ensures the lock and expiration are set in a single command
                try {
                    valueCommands.set(lockKey, lockValue, new SetArgs().nx().px(leaseTimeMs));
                    locked = true;
                    break;
                } catch (Exception e) {
                    // If set fails (usually returns null or throws if NX is not met), we retry
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
