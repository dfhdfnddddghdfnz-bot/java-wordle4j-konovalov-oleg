package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exception.DictionaryLoadException;
import ru.yandex.practicum.exception.EmptyDictionaryException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.WordleConstants.DICTIONARY_FILE;

class WordleDictionaryLoaderTest {

    private static PrintWriter log;

    private WordleDictionaryLoader loader;

    @BeforeAll
    static void createLog() {
        log = new PrintWriter(System.out, true);
    }

    @BeforeEach
    void createLoader() {
        loader = new WordleDictionaryLoader(log);
    }

    @Test
    void loadsUtf8FileWithWindowsLineEndings(@TempDir Path tempDir) throws Exception {
        String fileName = writeDictionary(tempDir,
                "а-ля фуршет\r\nабажур\r\nГЕРОЙ\r\n\r\nшёпот\r\nля-ля\r\nзамок\r\nзамок\r\nгонец");

        WordleDictionary dictionary = loader.load(fileName);

        assertAll(() -> assertEquals(List.of("герой", "шепот", "замок", "гонец"), dictionary.getWords()));
    }

    @Test
    void missingFileCausesDictionaryLoadException(@TempDir Path tempDir) {
        String missingFile = tempDir.resolve("missing.txt").toString();

        DictionaryLoadException e = assertThrows(DictionaryLoadException.class, () -> loader.load(missingFile));

        assertAll(
                () -> assertEquals("Файл словаря не найден: " + missingFile, e.getMessage()),
                () -> assertTrue(e.getCause() instanceof FileNotFoundException)
        );
    }

    @Test
    void fileWithoutSuitableWordsCausesEmptyDictionaryException(@TempDir Path tempDir) throws IOException {
        String fileName = writeDictionary(tempDir, "кот\nсобака\nля-ля\nhello\n");

        assertAll(() -> assertThrows(EmptyDictionaryException.class, () -> loader.load(fileName)));
    }

    @Test
    void emptyFileCausesEmptyDictionaryException(@TempDir Path tempDir) throws IOException {
        String fileName = writeDictionary(tempDir, "");

        assertAll(() -> assertThrows(EmptyDictionaryException.class, () -> loader.load(fileName)));
    }

    @Test
    void loadsGameDictionary() throws Exception {
        WordleDictionary dictionary = loader.load(DICTIONARY_FILE);

        assertAll(
                () -> assertEquals(4137, dictionary.size()),
                () -> assertTrue(dictionary.contains("герой")),
                () -> assertTrue(dictionary.getWords().stream().allMatch(WordleDictionary::isValidWord))
        );
    }

    private static String writeDictionary(Path directory, String content) throws IOException {
        Path file = directory.resolve("words.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file.toString();
    }
}
