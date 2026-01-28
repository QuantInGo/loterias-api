package com.gutotech.loteriasapi.model.exception;

import java.io.IOException;

/**
 * Exception thrown when CAIXA API is blocked (403 IP blocking or service unavailable)
 */
public class CaixaApiBlockedException extends IOException {
    
    private final boolean isIpBlocking;
    private final int httpStatus;
    
    public CaixaApiBlockedException(String message, int httpStatus, boolean isIpBlocking) {
        super(message);
        this.httpStatus = httpStatus;
        this.isIpBlocking = isIpBlocking;
    }
    
    public boolean isIpBlocking() {
        return isIpBlocking;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}
