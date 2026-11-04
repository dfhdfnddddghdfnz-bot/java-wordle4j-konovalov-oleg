package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {

    private static PrintWriter log;

    @BeforeAll
    static void createLog() {
        log = new PrintWriter(System.out, true);
    }

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource({
            "герой, герой",
            "'  герой\t', герой",
            "ГЕРОЙ, герой",
            "Шёпот, шепот",
            "ЁЖИК, ежик"
    })
    void normalizeStripsLowercasesAndReplacesYo(String rawWord, String expected) {
        assertAll(() -> assertEquals(expected, WordleDictionary.normalize(rawWord)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"герой", "аббат", "ясень", "объем"})
    void fiveRussianLettersAreValid(String word) {
        assertAll(() -> assertTrue(WordleDictionary.isValidWord(word)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "гер",
            "героев",
            "hello",
            "ля-ля",
            "гер0й",
            "ге ой",
            "геpой", // латинская "p" вместо русской "р"
            "Герой",
            "шёпот"
    })
    void otherStringsAreInvalid(String word) {
        assertAll(() -> assertFalse(WordleDictionary.isValidWord(word)));
    }

    @ParameterizedTest(name = "{0} против {1} -> {2}")
    @CsvSource({
            "гонец, герой, +^-^-",
            "герой, герой, +++++",
            "мушка, герой, -----",
            "терка, актер, ^^^^^",
            "молот, герой, ---+-",
            "молот, гроза, -^---",
            "олово, гроза, --+--",
            "бокал, аббат, ^--+-",
            "бабка, аббат, ^^+-^"
    })
    void analyzeFollowsWordleRules(String guess, String answer, String expectedHint) {
        assertAll(() -> assertEquals(expectedHint, WordleDictionary.analyze(guess, answer)));
    }

    @Test
    void analyzeRejectsInvalidWords() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> WordleDictionary.analyze("кот", "герой")),
                () -> assertThrows(IllegalArgumentException.class, () -> WordleDictionary.analyze("герой", "hello"))
        );
    }

    @Test
    void constructorNormalizesFiltersAndRemovesDuplicates() {
        WordleDictionary dictionary = new WordleDictionary(List.of("Герой", " гонец ", "Шёпот", "ля-ля", "кот",
                "героев", "герой", "замок", "замок", "", "hello"), log);

        assertAll(
                () -> assertEquals(List.of("герой", "гонец", "шепот", "замок"), dictionary.getWords()),
                () -> assertEquals(4, dictionary.size()),
                () -> assertTrue(dictionary.contains("шепот")),
                () -> assertFalse(dictionary.contains("кот"))
        );
    }

    @Test
    void wordsCannotBeModifiedFromOutside() {
        List<String> words = new WordleDictionary(List.of("герой"), log).getWords();

        assertAll(() -> assertThrows(UnsupportedOperationException.class, () -> words.add("гонец")));
    }

    @Test
    void randomWordIsTakenFromDictionary() {
        WordleDictionary dictionary = new WordleDictionary(List.of("герой", "гонец", "замок"), log);
        Random random = new Random(1);

        List<String> randomWords = Stream.generate(() -> dictionary.getRandomWord(random)).limit(20).toList();

        assertAll(
                () -> assertTrue(dictionary.getWords().containsAll(randomWords)),
                () -> assertEquals(Set.copyOf(dictionary.getWords()), Set.copyOf(randomWords))
        );
    }

    @Test
    void emptyDictionaryHasNoRandomWord() {
        WordleDictionary dictionary = new WordleDictionary(List.of("кот", "собака"), log);
        Random random = new Random();

        assertAll(
                () -> assertTrue(dictionary.isEmpty()),
                () -> assertThrows(IllegalStateException.class, () -> dictionary.getRandomWord(random))
        );
    }
}
