package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.exception.GameException;
import ru.yandex.practicum.exception.InvalidWordException;
import ru.yandex.practicum.exception.WordAlreadyUsedException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.WordleConstants.DICTIONARY_FILE;
import static ru.yandex.practicum.WordleConstants.MAX_ATTEMPTS;

class WordleGameTest {

    private static final String ANSWER = "герой";

    private static PrintWriter log;
    private static WordleDictionary dictionary;

    private WordleGame game;

    @BeforeAll
    static void createDictionary() {
        log = new PrintWriter(System.out, true);
        dictionary = new WordleDictionary(List.of("герой", "гонец", "геоид", "гетто", "гроза", "город", "молот",
                "мушка", "аббат", "бабка", "бокал", "олово", "терка", "актер"), log);
    }

    @BeforeEach
    void createGame() {
        game = new WordleGame(dictionary, ANSWER, new Random(42), log);
    }

    @Test
    void newGameIsInProgressWithAllAttempts() {
        assertAll(
                () -> assertEquals(GameStatus.IN_PROGRESS, game.getStatus()),
                () -> assertFalse(game.isOver()),
                () -> assertEquals(MAX_ATTEMPTS, game.getAttemptsLeft()),
                () -> assertTrue(game.getGuesses().isEmpty())
        );
    }

    @Test
    void validGuessReturnsHintAndSpendsAttempt() throws Exception {
        String hint = game.makeGuess("гонец");

        assertAll(
                () -> assertEquals("+^-^-", hint),
                () -> assertEquals(MAX_ATTEMPTS - 1, game.getAttemptsLeft()),
                () -> assertEquals(Map.of("гонец", "+^-^-"), game.getGuesses()),
                () -> assertEquals(GameStatus.IN_PROGRESS, game.getStatus())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"ГЕРОЙ", "  герой ", "гЁрой"})
    void guessIsNormalizedBeforeCheck(String input) throws Exception {
        String hint = game.makeGuess(input);

        assertAll(
                () -> assertEquals("+++++", hint),
                () -> assertEquals(GameStatus.WON, game.getStatus())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "кот", "героев", "hello", "гер0й", "ге-ой"})
    void malformedWordIsRejectedWithoutSpendingAttempt(String input) {
        assertAll(
                () -> assertThrows(InvalidWordException.class, () -> game.makeGuess(input)),
                () -> assertEquals(MAX_ATTEMPTS, game.getAttemptsLeft())
        );
    }

    @Test
    void unknownWordIsRejectedWithoutSpendingAttempt() {
        assertAll(
                () -> assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeGuess("кошка")),
                () -> assertEquals(MAX_ATTEMPTS, game.getAttemptsLeft())
        );
    }

    @Test
    void repeatedWordIsRejectedWithoutSpendingAttempt() throws Exception {
        game.makeGuess("гонец");

        assertAll(
                () -> assertThrows(WordAlreadyUsedException.class, () -> game.makeGuess(" ГОНЕЦ")),
                () -> assertEquals(MAX_ATTEMPTS - 1, game.getAttemptsLeft())
        );
    }

    @Test
    void guessingAnswerWins() throws Exception {
        game.makeGuess("гонец");

        String hint = game.makeGuess("герой");

        assertAll(
                () -> assertEquals("+++++", hint),
                () -> assertEquals(GameStatus.WON, game.getStatus()),
                () -> assertTrue(game.isOver()),
                () -> assertEquals(2, game.getAttemptsUsed())
        );
    }

    @Test
    void sixWrongGuessesLose() throws Exception {
        for (String word : List.of("гонец", "геоид", "гетто", "гроза", "город", "молот")) {
            game.makeGuess(word);
        }

        assertAll(
                () -> assertEquals(GameStatus.LOST, game.getStatus()),
                () -> assertTrue(game.isOver()),
                () -> assertEquals(0, game.getAttemptsLeft())
        );
    }

    @Test
    void noMovesAfterGameOver() throws Exception {
        game.makeGuess("герой");

        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> game.makeGuess("гонец")),
                () -> assertThrows(IllegalStateException.class, game::suggestWord)
        );
    }

    @Test
    void answerMustBeInDictionary() {
        Random random = new Random();

        assertAll(() -> assertThrows(IllegalArgumentException.class,
                () -> new WordleGame(dictionary, "кошка", random, log)));
    }

    @Test
    void randomAnswerIsTakenFromDictionary() {
        WordleGame randomGame = new WordleGame(dictionary, new Random(7), log);

        assertAll(() -> assertTrue(dictionary.contains(randomGame.getAnswer())));
    }

    @Test
    void candidatesKeepOnlyWordsConsistentWithHints() throws Exception {
        List<String> initialCandidates = List.copyOf(game.getCandidates());
        game.makeGuess("гонец");
        List<String> candidatesAfterFirstGuess = List.copyOf(game.getCandidates());
        game.makeGuess("молот");

        assertAll(
                () -> assertEquals(dictionary.getWords(), initialCandidates),
                () -> assertEquals(List.of("герой", "геоид", "гетто"), candidatesAfterFirstGuess),
                () -> assertEquals(List.of("герой"), game.getCandidates())
        );
    }

    @Test
    void suggestionIsConsistentWithAllHintsAndNeverRepeats() throws Exception {
        game.makeGuess("гонец");
        List<String> wrongSuggestions = new ArrayList<>();

        while (!game.isOver()) {
            String suggestion = game.suggestWord();
            if (game.getGuesses().containsKey(suggestion) || !matchesAllHints(suggestion, game.getGuesses())) {
                wrongSuggestions.add(suggestion);
            }
            game.makeGuess(suggestion);
        }

        assertAll(
                () -> assertEquals(List.of(), wrongSuggestions),
                () -> assertEquals(GameStatus.WON, game.getStatus())
        );
    }

    @Test
    void computerWinsAloneForEveryAnswer() throws Exception {
        List<String> notGuessedAnswers = new ArrayList<>();

        for (String answer : dictionary.getWords()) {
            WordleGame autoGame = new WordleGame(dictionary, answer, new Random(answer.hashCode()), log);
            playByHints(autoGame);
            if (autoGame.getStatus() != GameStatus.WON) {
                notGuessedAnswers.add(answer);
            }
        }

        assertAll(() -> assertEquals(List.of(), notGuessedAnswers));
    }

    @Test
    void computerRarelyLosesOnGameDictionary() throws Exception {
        // лог отключён: 4137 партий дали бы около 50 тысяч строк в System.out
        PrintWriter quietLog = new PrintWriter(Writer.nullWriter());
        WordleDictionary gameDictionary = new WordleDictionaryLoader(quietLog).load(DICTIONARY_FILE);
        Random random = new Random(42);
        int wins = 0;

        for (String answer : gameDictionary.getWords()) {
            WordleGame autoGame = new WordleGame(gameDictionary, answer, random, quietLog);
            playByHints(autoGame);
            if (autoGame.getStatus() == GameStatus.WON) {
                wins++;
            }
        }

        double winRate = (double) wins / gameDictionary.size();
        log.printf("Компьютер выиграл %d из %d партий (%.1f%%)%n", wins, gameDictionary.size(), winRate * 100);
        assertAll(() -> assertTrue(winRate > 0.9, "доля побед " + winRate));
    }

    private static void playByHints(WordleGame game) throws GameException {
        while (!game.isOver()) {
            game.makeGuess(game.suggestWord());
        }
    }

    private static boolean matchesAllHints(String word, Map<String, String> guesses) {
        return guesses.entrySet().stream()
                .allMatch(guess -> WordleDictionary.analyze(guess.getKey(), word).equals(guess.getValue()));
    }
}
