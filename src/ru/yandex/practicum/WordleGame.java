package ru.yandex.practicum;

import ru.yandex.practicum.exception.InvalidWordException;
import ru.yandex.practicum.exception.WordAlreadyUsedException;
import ru.yandex.practicum.exception.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static ru.yandex.practicum.WordleConstants.MAX_ATTEMPTS;
import static ru.yandex.practicum.WordleConstants.WORD_LENGTH;

public class WordleGame {

    private final WordleDictionary dictionary;
    private final String answer;
    private final Random random;
    private final PrintWriter log;
    private final Map<String, String> guesses = new LinkedHashMap<>();
    private final List<String> candidates;
    private int attemptsLeft = MAX_ATTEMPTS;
    private GameStatus status = GameStatus.IN_PROGRESS;

    public WordleGame(WordleDictionary dictionary, Random random, PrintWriter log) {
        this(dictionary, dictionary.getRandomWord(random), random, log);
    }

    WordleGame(WordleDictionary dictionary, String answer, Random random, PrintWriter log) {
        if (!dictionary.contains(answer)) {
            throw new IllegalArgumentException("Загаданного слова \"" + answer + "\" нет в словаре");
        }
        this.dictionary = dictionary;
        this.answer = answer;
        this.random = random;
        this.log = log;
        candidates = new ArrayList<>(dictionary.getWords());
        log.printf("Новая партия: загадано \"%s\", слов в словаре %d, попыток %d%n",
                answer, dictionary.size(), attemptsLeft);
    }

    public String makeGuess(String input)
            throws InvalidWordException, WordNotFoundInDictionaryException, WordAlreadyUsedException {
        checkInProgress();
        String word = WordleDictionary.normalize(input);
        log.printf("Ввод: \"%s\", после нормализации \"%s\"%n", input, word);
        if (!WordleDictionary.hasOnlyRussianLetters(word)) {
            throw new InvalidWordException("Используйте только русские буквы");
        }
        if (!WordleDictionary.hasValidLength(word)) {
            throw new InvalidWordException("Нужно слово из " + WORD_LENGTH + " букв");
        }

        if (!word.equals(answer)) {
            if (!dictionary.contains(word)) {
                throw new WordNotFoundInDictionaryException(word);
            }
            if (guesses.containsKey(word)) {
                throw new WordAlreadyUsedException(word);
            }
        }
        return applyGuess(word);
    }

    public String suggestWord() {
        checkInProgress();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Не осталось кандидатов, хотя загаданное слово должно быть среди них");
        }
        String suggestion = candidates.get(random.nextInt(candidates.size()));
        log.printf("Подсказка: \"%s\", кандидатов %d%n", suggestion, candidates.size());
        return suggestion;
    }

    public GameStatus getStatus() {
        return status;
    }

    public boolean isOver() {
        return status != GameStatus.IN_PROGRESS;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public int getAttemptsUsed() {
        return MAX_ATTEMPTS - attemptsLeft;
    }

    public String getAnswer() {
        return answer;
    }

    public Map<String, String> getGuesses() {
        return Collections.unmodifiableMap(guesses);
    }

    List<String> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    private String applyGuess(String word) {
        attemptsLeft--;
        String hint = WordleDictionary.analyze(word, answer);
        guesses.put(word, hint);
        candidates.removeIf(candidate -> !WordleDictionary.analyze(word, candidate).equals(hint));
        if (!candidates.contains(answer)) {
            throw new IllegalStateException("Загаданное слово выпало из кандидатов после хода \"" + word + "\"");
        }
        if (word.equals(answer)) {
            status = GameStatus.WON;
        } else if (attemptsLeft == 0) {
            status = GameStatus.LOST;
        }
        log.printf("Ход %d: \"%s\" -> %s, осталось попыток %d, кандидатов %d, статус %s%n",
                getAttemptsUsed(), word, hint, attemptsLeft, candidates.size(), status);
        return hint;
    }

    private void checkInProgress() {
        if (isOver()) {
            throw new IllegalStateException("Партия уже окончена со статусом " + status);
        }
    }
}
