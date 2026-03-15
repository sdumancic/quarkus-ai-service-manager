package com.prevelio.common.lock;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedLockInterceptorTest {

    @Mock
    private RedisDataSource redisDataSource;

    @Mock
    private InvocationContext invocationContext;

    @Mock
    private ValueCommands<String, String> valueCommands;

    @InjectMocks
    private DistributedLockInterceptor interceptor;

    @Test
    void testSuccessfulLockAcquisition() throws Exception {
        // Setup mock method with annotation
        Method method = MockService.class.getMethod("lockMethod");
        when(invocationContext.getMethod()).thenReturn(method);
        lenient().when(invocationContext.getTarget()).thenReturn(new MockService());
        
        // Mock Redis SET NX PX
        io.vertx.mutiny.redis.client.Response okResponse = mock(io.vertx.mutiny.redis.client.Response.class);
        when(okResponse.toString()).thenReturn("OK");
        when(redisDataSource.execute(eq("SET"), anyString(), anyString(), eq("NX"), eq("PX"), anyString()))
                .thenReturn(okResponse);
        
        // Mock redisDataSource.value(String.class) for the unlock check
        when(redisDataSource.value(String.class)).thenReturn(valueCommands);
        
        // Mock proceed()
        when(invocationContext.proceed()).thenReturn("Method Result");

        // Act
        Object result = interceptor.manageLock(invocationContext);

        // Assert
        assertEquals("Method Result", result);
        verify(invocationContext, times(1)).proceed();
        // Verify we tried to SET at least once
        verify(redisDataSource, atLeastOnce()).execute(eq("SET"), anyString(), anyString(), eq("NX"), eq("PX"), anyString());
    }

    @Test
    void testFailedLockAcquisitionThrowsException() throws Exception {
        // Setup mock method
        Method method = MockService.class.getMethod("lockMethodWithShortTimeout");
        when(invocationContext.getMethod()).thenReturn(method);
        lenient().when(invocationContext.getTarget()).thenReturn(new MockService());
        
        // Mock Redis failure (returning null meaning lock not acquired)
        when(redisDataSource.execute(eq("SET"), anyString(), anyString(), eq("NX"), eq("PX"), anyString()))
                .thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> interceptor.manageLock(invocationContext));
        assertTrue(exception.getMessage().contains("Could not acquire distributed lock"));
        
        verify(invocationContext, never()).proceed();
    }

    static class MockService {
        @DistributedLock(key = "test-key", waitTimeMs = 500)
        public void lockMethod() {}

        @DistributedLock(key = "timeout-key", waitTimeMs = 100)
        public void lockMethodWithShortTimeout() {}
    }

    // Helper to allow assertTrue
    private void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
