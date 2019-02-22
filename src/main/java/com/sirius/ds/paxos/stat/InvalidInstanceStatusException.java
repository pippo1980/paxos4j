package com.sirius.ds.paxos.stat;

public class InvalidInstanceStatusException extends RuntimeException {

    public InvalidInstanceStatusException() {

    }

    public InvalidInstanceStatusException(String message) {
        super(message);
    }

    public InvalidInstanceStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInstanceStatusException(Throwable cause) {
        super(cause);
    }

    public InvalidInstanceStatusException(String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
