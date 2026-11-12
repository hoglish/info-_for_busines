package ru.edu.pr08;

import java.util.ArrayList;
import java.util.List;

/**
 * Проверка инвариантов полей (R3, R4 и поле website — вариант 26).
 * Возвращает список причин; пустой список означает, что запись корректна.
 */
public class ClientValidator {

    public List<String> validate(ClientDraft d) {
        List<String> problems = new ArrayList<>();
        checkInn(d.inn(), problems);
        if (d.name() == null || d.name().isBlank()) {
            problems.add("имя не должно быть пустым");
        }
        checkEmail(d.email(), problems);
        checkWebsite(d.website(), problems);
        return problems;
    }

    /** R3: ИНН — только цифры, длина 10 или 12. */
    private void checkInn(String inn, List<String> problems) {
        if (inn == null || inn.isEmpty()) {
            problems.add("ИНН не должен быть пустым");
            return;
        }
        for (int i = 0; i < inn.length(); i++) {
            char c = inn.charAt(i);
            if (c < '0' || c > '9') {
                problems.add("ИНН должен содержать только цифры");
                return;
            }
        }
        if (inn.length() != 10 && inn.length() != 12) {
            problems.add("ИНН должен содержать 10 или 12 цифр, получено " + inn.length());
        }
    }

    /** R4: ровно один @, непустые левая и правая части. */
    private void checkEmail(String email, List<String> problems) {
        if (email == null || email.isEmpty()) {
            problems.add("email не должен быть пустым");
            return;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            problems.add("email должен содержать @");
        } else if (email.indexOf('@', at + 1) >= 0) {
            problems.add("email должен содержать ровно один @");
        } else if (at == 0) {
            problems.add("email: пустая левая часть");
        } else if (at == email.length() - 1) {
            problems.add("email: пустая правая часть");
        }
    }

    /** Вариант 26, часть А: website обязателен и начинается с http:// или https://. */
    private void checkWebsite(String website, List<String> problems) {
        if (website == null || website.isEmpty()) {
            problems.add("website не должен быть пустым");
            return;
        }
        if (!website.startsWith("http://") && !website.startsWith("https://")) {
            problems.add("website должен начинаться с http:// или https://");
        } else if (addWebsiteDomain(website).isEmpty()) {
            problems.add("website: не указан домен");
        }
    }

    /**
     * Вариант 26, часть Б: домен из URL или пустая строка, если URL некорректен.
     * Например, https://example.com:8080/a?b=1 -> example.com
     */
    public String addWebsiteDomain(String url) {
        if (url == null) {
            return "";
        }
        String rest;
        if (url.startsWith("https://")) {
            rest = url.substring("https://".length());
        } else if (url.startsWith("http://")) {
            rest = url.substring("http://".length());
        } else {
            return "";
        }
        int end = rest.length();
        for (int i = 0; i < rest.length(); i++) {
            char c = rest.charAt(i);
            if (c == '/' || c == '?' || c == '#' || c == ':') {
                end = i;
                break;
            }
        }
        String host = rest.substring(0, end);
        if (host.isEmpty() || host.indexOf(' ') >= 0) {
            return "";
        }
        return host;
    }
}
