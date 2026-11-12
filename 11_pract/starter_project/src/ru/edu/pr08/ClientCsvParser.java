package ru.edu.pr08;

/**
 * Разбор одной строки CSV (R2). Формат: inn;name;email;website (поле website — вариант 26).
 * Только синтаксис: число полей и отсутствие пробелов вокруг полей. Смысл полей проверяет ClientValidator.
 */
public class ClientCsvParser {
    public static final String DELIMITER = ";";
    public static final int FIELD_COUNT = 4;

    public ClientDraft parse(String line, int lineNumber) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("пустая строка (строка " + lineNumber + ")");
        }
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "ожидается " + FIELD_COUNT + " поля, получено " + parts.length);
        }
        for (String p : parts) {
            if (!p.equals(p.strip())) {
                throw new IllegalArgumentException("пробелы вокруг поля не допускаются: '" + p + "'");
            }
        }
        return new ClientDraft(parts[0], parts[1], parts[2], parts[3]);
    }
}
