package ru.yandex.practicum.exception;

import java.io.Serial;

public abstract class GameException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    protected GameException(String message) {
        super(message);
    }
}
