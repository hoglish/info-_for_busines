package ru.edu.pr08;

/** Ошибка одной строки: номер строки в файле (заголовок — строка 1) и причина (R5). */
public record ImportError(int lineNumber, String message) {
    /** Формат локализации ошибки: «строка N: причина». */
    public String format() {
        return "строка " + lineNumber + ": " + message;
    }
}
