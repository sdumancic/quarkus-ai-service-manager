package com.prevelio.common.lock;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Distributed Lock annotation to prevent multiple pods from executing the method simultaneously.
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * The Redis key prefix to use for this lock.
     */
    @Nonbinding
    String key() default "default";

    /**
     * How long to wait to acquire the lock (in milliseconds).
     */
    @Nonbinding
    long waitTimeMs() default 5000;

    /**
     * How long to hold the lock before it auto-expires (in milliseconds).
     */
    @Nonbinding
    long leaseTimeMs() default 10000;
}
