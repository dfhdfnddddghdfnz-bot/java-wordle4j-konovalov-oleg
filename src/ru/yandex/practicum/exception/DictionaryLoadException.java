package ru.yandex.practicum.exception;

import java.io.Serial;

public class DictionaryLoadException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DictionaryLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
