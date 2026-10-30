package ru.yandex.practicum.exception;

import java.io.Serial;

public class InvalidWordException extends GameException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidWordException(String message) {
        super(message);
    }
}
