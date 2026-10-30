package ru.yandex.practicum.exception;

import java.io.Serial;

public class EmptyDictionaryException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmptyDictionaryException(String fileName) {
        super("В файле словаря " + fileName + " нет ни одного подходящего слова");
    }
}
