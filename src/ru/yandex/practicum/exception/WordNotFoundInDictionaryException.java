package ru.yandex.practicum.exception;

import java.io.Serial;

public class WordNotFoundInDictionaryException extends GameException {

    @Serial
    private static final long serialVersionUID = 1L;

    public WordNotFoundInDictionaryException(String word) {
        super("Слова \"" + word + "\" нет в словаре");
    }
}
