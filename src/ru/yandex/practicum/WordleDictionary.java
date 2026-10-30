package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static ru.yandex.practicum.WordleConstants.ABSENT;
import static ru.yandex.practicum.WordleConstants.CORRECT;
import static ru.yandex.practicum.WordleConstants.PRESENT;
import static ru.yandex.practicum.WordleConstants.WORD_LENGTH;

public class WordleDictionary {

    private static final char FIRST_LETTER = 'а';
    private static final char LAST_LETTER = 'я';

    // список даёт доступ по индексу для случайного выбора, множество - проверку наличия за O(1)
    private final List<String> words;
    private final Set<String> wordSet;

    /**
     * Строит словарь из «сырых» строк: нормализует их, отбрасывает неподходящие и повторы.
     * Порядок слов сохраняется.
     */
    public WordleDictionary(Collection<String> rawWords, PrintWriter log) {
        Set<String> unique = new LinkedHashSet<>();
        int rejected = 0;
        for (String rawWord : rawWords) {
            String word = normalize(rawWord);
            if (isValidWord(word)) {
                unique.add(word);
            } else {
                rejected++;
            }
        }
        words = List.copyOf(unique);
        wordSet = Set.copyOf(unique);
        log.printf("Словарь: строк %d, слов для игры %d, не подошло %d, повторов %d%n",
                rawWords.size(), words.size(), rejected, rawWords.size() - rejected - words.size());
    }

    public int size() {
        return words.size();
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    public boolean contains(String word) {
        return wordSet.contains(word);
    }

    public List<String> getWords() {
        return words;
    }

    public String getRandomWord(Random random) {
        if (words.isEmpty()) {
            throw new IllegalStateException("Словарь пуст, выбрать слово невозможно");
        }
        return words.get(random.nextInt(words.size()));
    }

    public static String normalize(String word) {
        return word.strip().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }

    public static boolean isValidWord(String word) {
        return word != null && hasValidLength(word) && hasOnlyRussianLetters(word);
    }

    public static boolean hasValidLength(String word) {
        return word.length() == WORD_LENGTH;
    }

    public static boolean hasOnlyRussianLetters(String word) {
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            if (letter < FIRST_LETTER || letter > LAST_LETTER) {
                return false;
            }
        }
        return true;
    }

    public static String analyze(String guess, String answer) {
        if (!isValidWord(guess) || !isValidWord(answer)) {
            throw new IllegalArgumentException("Сравнивать можно только слова из " + WORD_LENGTH
                    + " русских букв: \"" + guess + "\" и \"" + answer + "\"");
        }
        StringBuilder hint = new StringBuilder(WORD_LENGTH);
        // сколько раз каждая буква ответа, не совпавшая по месту, ещё может дать "^"
        int[] unmatched = new int[LAST_LETTER - FIRST_LETTER + 1];
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                hint.append(CORRECT);
            } else {
                hint.append(ABSENT);
                unmatched[answer.charAt(i) - FIRST_LETTER]++;
            }
        }
        for (int i = 0; i < WORD_LENGTH; i++) {
            int letter = guess.charAt(i) - FIRST_LETTER;
            if (hint.charAt(i) == ABSENT && unmatched[letter] > 0) {
                hint.setCharAt(i, PRESENT);
                unmatched[letter]--;
            }
        }
        return hint.toString();
    }
}
