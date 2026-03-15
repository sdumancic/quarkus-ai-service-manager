package com.prevelio.common.lock;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@Slf4j
class DistributedLockTest {

    @Inject
    LockTestingService testingService;

    @Test
    void testConcurrentAccessWithLock() {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        AtomicInteger failures = new AtomicInteger(0);
        AtomicInteger successes = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    testingService.criticalSection();
                    successes.getAndIncrement();
                } catch (Exception e) {
                    failures.getAndIncrement();
                }
            }, executor));
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // With waitTimeMs=1000 and method taking 200ms, and 5 threads:
        // Total duration needed is roughly 1000ms.
        // 5*200 = 1000. It might be tight but most/all should succeed.

        log.info("Successes: " + successes.get());
        log.info("Failures: " + failures.get());
        log.info("Max Concurrency: " + testingService.getMaxConcurrentThreads());

        assertTrue(successes.get() > 0, "At least some calls should succeed");
        assertEquals(1, testingService.getMaxConcurrentThreads(),
                "Only one thread should be in the critical section at a time");
        assertEquals(successes.get(), testingService.getSuccessfulCalls(),
                "Success counter should match successful method executions");
    }
}
