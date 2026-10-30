package ru.yandex.practicum.exception;

import java.io.Serial;

public class LogFileException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LogFileException(String fileName, Throwable cause) {
        super("Не удалось создать лог-файл " + fileName + ": " + cause.getMessage(), cause);
    }
}
