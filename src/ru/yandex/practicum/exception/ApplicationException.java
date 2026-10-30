package ru.yandex.practicum.exception;

import java.io.Serial;

public abstract class ApplicationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
