package com.prevelio.common.lock;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Set;

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
    private KeyCommands<String> keyCommands;

    @InjectMocks
    private DistributedLockInterceptor interceptor;

    @Test
    void testSuccessfulLockAcquisition() throws Exception {
        // Setup mock method with annotation
        Method method = MockService.class.getMethod("lockMethod");
        DistributedLock lockAnnotation = method.getAnnotation(DistributedLock.class);
        when(invocationContext.getInterceptorBindings()).thenReturn(Set.of(lockAnnotation));
        
        // Mock Redis SET NX PX
        io.vertx.mutiny.redis.client.Response okResponse = mock(io.vertx.mutiny.redis.client.Response.class);
        when(okResponse.toString()).thenReturn("OK");
        when(redisDataSource.execute(eq("SET"), anyString(), anyString(), eq("NX"), eq("PX"), anyString()))
                .thenReturn(okResponse);
        
        // Mock Redis GET for unlock check - using lenient since it might not match the random value
        io.vertx.mutiny.redis.client.Response getResponse = mock(io.vertx.mutiny.redis.client.Response.class);
        lenient().when(redisDataSource.execute(eq("GET"), anyString())).thenReturn(getResponse);
        
        // Mock redisDataSource.key() - lenient because if GET value doesn't match random UUID, it won't be called
        lenient().when(redisDataSource.key()).thenReturn(keyCommands);

        // Mock proceed()
        when(invocationContext.proceed()).thenReturn("Method Result");

        // Act
        Object result = interceptor.manageLock(invocationContext);

        // Assert
        assertEquals("Method Result", result);
        verify(invocationContext, times(1)).proceed();
        verify(redisDataSource, atLeastOnce()).execute(eq("SET"), anyString(), anyString(), eq("NX"), eq("PX"), anyString());
    }

    @Test
    void testFailedLockAcquisitionThrowsException() throws Exception {
        // Setup mock method
        Method method = MockService.class.getMethod("lockMethodWithShortTimeout");
        DistributedLock lockAnnotation = method.getAnnotation(DistributedLock.class);
        when(invocationContext.getInterceptorBindings()).thenReturn(Set.of(lockAnnotation));
        
        // Mock Redis failure (returning null meaning lock not acquired)
        when(redisDataSource.execute(eq("SET"), anyString(), anyString(), eq("NX"), eq("PX"), anyString()))
                .thenReturn(null);

        // Act & Assert
        DistributedLockAcquisitionException exception = assertThrows(DistributedLockAcquisitionException.class, 
            () -> interceptor.manageLock(invocationContext));
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
