package ru.yandex.practicum;

public final class WordleConstants {

    public static final String DICTIONARY_FILE = "words_ru.txt";
    public static final String LOG_FILE = "wordle.log";

    public static final int WORD_LENGTH = 5;
    public static final int MAX_ATTEMPTS = 6;

    private WordleConstants() {
        throw new UnsupportedOperationException("WordleConstants хранит только константы, создавать его объекты нельзя");
    }

    public static final class HintLetter {

        public static final char IN_PLACE = '+';
        public static final char IN_WRONG_PLACE = '^';
        public static final char UNMATCHED = '-';

        private HintLetter() {
            throw new UnsupportedOperationException("HintLetter хранит только константы, создавать его объекты нельзя");
        }
    }
}
