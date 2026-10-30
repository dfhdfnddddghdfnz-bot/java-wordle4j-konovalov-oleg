package ru.yandex.practicum;

public final class WordleConstants {

    public static final String DICTIONARY_FILE = "words_ru.txt";
    public static final String LOG_FILE = "wordle.log";

    public static final int WORD_LENGTH = 5;
    public static final int MAX_ATTEMPTS = 6;

    /** Буква стоит на своём месте. */
    public static final char CORRECT = '+';
    /** Буква есть в слове, но в другом месте. */
    public static final char PRESENT = '^';
    /** Буквы в слове нет или все её вхождения уже отмечены. */
    public static final char ABSENT = '-';

    private WordleConstants() {
        throw new UnsupportedOperationException("WordleConstants хранит только константы, создавать его объекты нельзя");
    }
}
