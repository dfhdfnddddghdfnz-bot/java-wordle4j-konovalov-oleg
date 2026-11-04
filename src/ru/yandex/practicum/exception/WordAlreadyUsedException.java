package ru.yandex.practicum.exception;

import java.io.Serial;

public class WordAlreadyUsedException extends GameException {

    @Serial
    private static final long serialVersionUID = 1L;

    public WordAlreadyUsedException(String word) {
        super("Слово \"" + word + "\" уже было");
    }
}
