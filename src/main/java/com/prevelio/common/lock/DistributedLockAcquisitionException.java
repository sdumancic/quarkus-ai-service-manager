package com.prevelio.common.lock;

public class DistributedLockAcquisitionException extends RuntimeException {
    public DistributedLockAcquisitionException(String message) {
        super(message);
    }

    public DistributedLockAcquisitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
