package com.prevelio.common.lock;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class LockTestingService {

    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final AtomicInteger maxConcurrentThreads = new AtomicInteger(0);
    private final AtomicLong successfulCalls = new AtomicLong(0);

    @DistributedLock(key = "test-lock", waitTimeMs = 1000, leaseTimeMs = 5000)
    public void criticalSection() {
        int current = activeThreads.incrementAndGet();
        
        // Update max observed concurrency
        int max;
        do {
            max = maxConcurrentThreads.get();
            if (current <= max) break;
        } while (!maxConcurrentThreads.compareAndSet(max, current));

        try {
            // Simulate some work
            Thread.sleep(200);
            successfulCalls.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            activeThreads.decrementAndGet();
        }
    }

    public int getMaxConcurrentThreads() {
        return maxConcurrentThreads.get();
    }

    public long getSuccessfulCalls() {
        return successfulCalls.get();
    }
}
