package ru.yandex.practicum;

import ru.yandex.practicum.exception.ApplicationException;
import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.LogFileException;

import java.io.Console;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.Scanner;

import static ru.yandex.practicum.WordleConstants.DICTIONARY_FILE;
import static ru.yandex.practicum.WordleConstants.LOG_FILE;
import static ru.yandex.practicum.WordleConstants.MAX_ATTEMPTS;
import static ru.yandex.practicum.WordleConstants.WORD_LENGTH;

public class Wordle {

    private static final String PROMPT = "> ";
    private static final String RULES = """
            Угадайте слово из %d букв за %d попыток.
            После каждого слова появится подсказка:
              + буква стоит на своём месте
              ^ буква есть в слове, но в другом месте
              - такой буквы в слове нет
            Нажмите Enter на пустой строке, и ход за вас сделает компьютер."""
            .formatted(WORD_LENGTH, MAX_ATTEMPTS);

    @SuppressWarnings("java:S106")
    public static void main(String[] args) {
        PrintStream out = System.out;
        try (PrintWriter log = createLog(LOG_FILE)) {
            runAndReportErrors(out, log);
        } catch (LogFileException e) {
            out.println("Не удалось запустить игру: " + e.getMessage());
        }
    }

    static PrintWriter createLog(String fileName) throws LogFileException {
        try {
            return new PrintWriter(new FileWriter(fileName, StandardCharsets.UTF_8), true);
        } catch (IOException e) {
            throw new LogFileException(fileName, e);
        }
    }

    private static void runAndReportErrors(PrintStream out, PrintWriter log) {
        try {
            run(out, log);
        } catch (ApplicationException e) {
            log.println("Игра не запущена: " + e.getMessage());
            e.printStackTrace(log);
            out.println("Не удалось запустить игру: " + e.getMessage());
        } catch (RuntimeException e) {
            log.println("Внутренняя ошибка программы");
            e.printStackTrace(log);
            out.println("Внутренняя ошибка программы, подробности в файле " + LOG_FILE);
        }
    }

    private static void run(PrintStream out, PrintWriter log) throws ApplicationException {
        log.println("Запуск игры: " + LocalDateTime.now(ZoneId.systemDefault()));
        WordleDictionary dictionary = new WordleDictionaryLoader(log).load(DICTIONARY_FILE);
        WordleGame game = new WordleGame(dictionary, new Random(), log);
        try (Scanner in = new Scanner(System.in, consoleCharset())) {
            play(game, in, out, log);
        }
        log.println("Игра завершена со статусом " + game.getStatus());
    }

    static void play(WordleGame game, Scanner in, PrintStream out, PrintWriter log) {
        out.println(RULES);
        while (!game.isOver()) {
            out.printf("%nПопытка %d из %d%n", game.getAttemptsUsed() + 1, MAX_ATTEMPTS);
            out.print(PROMPT);
            if (!in.hasNextLine()) {
                out.println();
                log.println("Ввод закончился, партия прервана");
                break;
            }
            String word = in.nextLine();
            if (word.isBlank()) {
                word = game.suggestWord();
                out.println(PROMPT + word + "   (ход компьютера)");
            }
            try {
                String hint = game.makeGuess(word);
                out.println(PROMPT + hint);
            } catch (GameException e) {
                log.println("Ход не принят: " + e.getMessage());
                out.println(e.getMessage() + ", попытка не засчитана");
            }
        }
        out.println();
        out.println(describeOutcome(game));
        out.println("Загаданное слово: " + game.getAnswer());
    }

    private static String describeOutcome(WordleGame game) {
        return switch (game.getStatus()) {
            case WON -> "Победа! Слово угадано за " + game.getAttemptsUsed() + " из " + MAX_ATTEMPTS
                    + " попыток.";
            case LOST -> "Попытки закончились, слово не угадано.";
            case IN_PROGRESS -> "Партия прервана.";
        };
    }

    private static Charset consoleCharset() {
        // в терминале Windows кодировка консоли бывает не UTF-8 (например, cp866)
        // при запуске из IDEA консоли нет, и ввод приходит в UTF-8
        Console console = System.console();
        return console != null ? console.charset() : StandardCharsets.UTF_8;
    }
}
