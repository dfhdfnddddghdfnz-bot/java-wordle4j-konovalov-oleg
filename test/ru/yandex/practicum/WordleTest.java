package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.exception.LogFileException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.WordleConstants.MAX_ATTEMPTS;

class WordleTest {

    private static PrintWriter log;
    private static WordleDictionary dictionary;

    private WordleGame game;

    @BeforeAll
    static void createDictionary() {
        log = new PrintWriter(System.out, true);
        dictionary = new WordleDictionary(List.of("герой", "гонец", "геоид", "гетто", "гроза", "город", "молот"), log);
    }

    @BeforeEach
    void createGame() {
        game = new WordleGame(dictionary, "герой", new Random(42), log);
    }

    @Test
    void playerWinsAndSeesHintUnderEachWord() {
        String output = playWithInput("гонец\nгерой\n");

        assertAll(
                () -> assertEquals(GameStatus.WON, game.getStatus()),
                () -> assertTrue(output.contains("> +^-^-")),
                () -> assertTrue(output.contains("> +++++")),
                () -> assertTrue(output.contains("Победа! Слово угадано за 2 из 6 попыток.")),
                () -> assertTrue(output.contains("Загаданное слово: герой"))
        );
    }

    @Test
    void wrongInputIsExplainedAndDoesNotSpendAttempts() {
        String output = playWithInput("кот\nhello\nкошка\nгонец\nгонец\n");

        assertAll(
                () -> assertEquals(MAX_ATTEMPTS - 1, game.getAttemptsLeft()),
                () -> assertTrue(output.contains("Нужно слово из 5 букв, попытка не засчитана")),
                () -> assertTrue(output.contains("Используйте только русские буквы, попытка не засчитана")),
                () -> assertTrue(output.contains("Слова \"кошка\" нет в словаре, попытка не засчитана")),
                () -> assertTrue(output.contains("Слово \"гонец\" уже было, попытка не засчитана"))
        );
    }

    @Test
    void emptyLinesLetComputerPlayWholeGame() {
        String output = playWithInput("   \n" + "\n".repeat(MAX_ATTEMPTS - 1));

        assertAll(
                () -> assertTrue(game.isOver()),
                () -> assertTrue(output.contains("(ход компьютера)")),
                () -> assertTrue(output.contains("Загаданное слово: герой"))
        );
    }

    @Test
    void sixWrongWordsLoseGame() {
        String output = playWithInput("гонец\nгеоид\nгетто\nгроза\nгород\nмолот\n");

        assertAll(
                () -> assertEquals(GameStatus.LOST, game.getStatus()),
                () -> assertTrue(output.contains("Попытки закончились, слово не угадано.")),
                () -> assertTrue(output.contains("Загаданное слово: герой"))
        );
    }

    @Test
    void endOfInputStopsLoopWithoutHanging() {
        String output = playWithInput("гонец\n");

        assertAll(
                () -> assertFalse(game.isOver()),
                () -> assertEquals(MAX_ATTEMPTS - 1, game.getAttemptsLeft()),
                () -> assertTrue(output.contains("Партия прервана.")),
                () -> assertTrue(output.contains("Загаданное слово: герой"))
        );
    }

    @Test
    @SuppressWarnings("resource")
    void logThatCannotBeCreatedCausesLogFileException(@TempDir Path tempDir) {
        String directoryName = tempDir.toString();

        assertAll(
                () -> assertThrows(LogFileException.class, () -> Wordle.createLog(directoryName))
        );
    }

    private String playWithInput(String input) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        Wordle.play(game, new Scanner(input), out, log);
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
