package com.sirius.ds.paxos;

public class InvlidMsgException extends RuntimeException {

    public InvlidMsgException() {
    }

    public InvlidMsgException(String message) {
        super(message);
    }

    public InvlidMsgException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvlidMsgException(Throwable cause) {
        super(cause);
    }

    public InvlidMsgException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
