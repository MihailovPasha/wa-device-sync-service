package org.wa.device.sync.service.exception;

import org.springframework.http.HttpStatusCode;

public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, final HttpStatusCode httpStatusCode) {
        super(message);
    }
}
