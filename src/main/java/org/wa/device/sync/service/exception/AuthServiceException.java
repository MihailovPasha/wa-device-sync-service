package org.wa.device.sync.service.exception;

import org.springframework.http.HttpStatusCode;

public class AuthServiceException extends RuntimeException {

    public AuthServiceException(String message) {
        super(message);
    }

    public AuthServiceException(String message, final HttpStatusCode httpStatusCode) {
        super(message);
    }
}
