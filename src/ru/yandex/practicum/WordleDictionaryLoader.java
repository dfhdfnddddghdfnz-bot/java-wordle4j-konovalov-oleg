package ru.yandex.practicum;

import ru.yandex.practicum.exception.DictionaryLoadException;
import ru.yandex.practicum.exception.EmptyDictionaryException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary load(String fileName) throws DictionaryLoadException, EmptyDictionaryException {
        log.println("Загрузка словаря из файла " + fileName);
        WordleDictionary dictionary = new WordleDictionary(readLines(fileName), log);
        if (dictionary.isEmpty()) {
            throw new EmptyDictionaryException(fileName);
        }
        return dictionary;
    }

    private List<String> readLines(String fileName) throws DictionaryLoadException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            throw new DictionaryLoadException("Файл словаря не найден: " + fileName, e);
        } catch (IOException e) {
            throw new DictionaryLoadException("Не удалось прочитать файл словаря: " + fileName, e);
        }
        return lines;
    }
}
