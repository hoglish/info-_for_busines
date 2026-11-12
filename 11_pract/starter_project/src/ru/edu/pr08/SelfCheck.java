package ru.edu.pr08;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Тестовый раннер и точка входа.
 *   (без аргументов)   — unit-проверки + data-driven режим по data/sample.csv
 *   --interactive      — ввод строки inn;name;email;website из System.in
 *   --import <файл>    — импорт файла (без колонки expected) и вывод ImportResult
 */
public class SelfCheck {
    private static int passed = 0;
    private static int total = 0;

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("--interactive")) {
            interactive();
            return;
        }
        if (args.length > 1 && args[0].equals("--import")) {
            importMode(Path.of(args[1]));
            return;
        }
        System.out.println("--- Запуск Unit-тестов ---");
        unitChecks();
        System.out.println();
        System.out.println("--- Запуск Data-Driven тестов (data/sample.csv) ---");
        dataDriven(Path.of("data", "sample.csv"));
        System.out.println();
        System.out.println("--- Итог Unit/Data тестов ---");
        System.out.println("Passed: " + passed + "/" + total);
        System.out.println(passed == total ? "PASS" : "FAIL");
        if (passed != total) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ unit

    private static void unitChecks() throws IOException {
        ClientCsvParser parser = new ClientCsvParser();
        ClientValidator validator = new ClientValidator();

        // 1-3 ClientCsvParser
        ClientDraft d = parser.parse("1234567890;Иван;ivan@mail.ru;https://mail.ru", 2);
        check("1. Parser: корректная строка", d.inn().equals("1234567890") && d.website().equals("https://mail.ru"));
        check("2. Parser: 2 поля вместо 4 -> IAE «ожидается 4 поля»",
                parseFails(parser, "1234567890;Иван", "ожидается 4 поля"));
        check("3. Parser: пробел после «;» -> IAE", parseFails(parser, "1234567890; Иван;a@b.ru;http://x.ru", "пробелы"));

        // 4-7 ИНН
        check("4. Validator: ИНН 10 цифр OK", valid(validator, "1234567890", "Иван", "a@b.ru", "https://x.ru"));
        check("5. Validator: ИНН 12 цифр OK", valid(validator, "123456789012", "Иван", "a@b.ru", "https://x.ru"));
        check("6. Validator: ИНН 11 цифр -> «10 или 12 цифр»",
                invalid(validator, "12345678901", "Иван", "a@b.ru", "https://x.ru", "10 или 12 цифр"));
        check("7. Validator: ИНН с буквами -> «только цифры»",
                invalid(validator, "12345abc90", "Иван", "a@b.ru", "https://x.ru", "только цифры"));
        // 8-10 email
        check("8. Validator: email с @ OK", valid(validator, "1234567890", "Иван", "ivan@mail.ru", "https://x.ru"));
        check("9. Validator: email без @ -> «должен содержать @»",
                invalid(validator, "1234567890", "Иван", "ivan.mail.ru", "https://x.ru", "должен содержать @"));
        check("10. Validator: пустая левая часть email",
                invalid(validator, "1234567890", "Иван", "@mail.ru", "https://x.ru", "пустая левая часть"));

        // 11-16 ClientImporter
        Path f11 = temp("inn;name;email;website\n1234567890;A;a@a.ru;http://a.ru\n"
                + "123456789012;B;b@b.ru;https://b.ru\n9876543210;C;c@c.ru;https://c.ru\n");
        ImportResult r11 = new ClientImporter(new ClientRepository()).importFile(f11);
        check("11. Importer: корректный файл (3 строки) accepted=3, rejected=0",
                r11.accepted() == 3 && r11.rejected() == 0);

        Path f12 = temp("inn;name;email;website\n1234567890;A;a@a.ru;http://a.ru\n"
                + "123;Bad;bad@x.ru;http://x.ru\n7700000000;NoMail;wrong;http://x.ru\n");
        ImportResult r12 = new ClientImporter(new ClientRepository()).importFile(f12);
        check("12. Importer: 2 плохие строки accepted=1, rejected=2, номера строк 3 и 4",
                r12.accepted() == 1 && r12.rejected() == 2
                        && r12.errors().get(0).lineNumber() == 3 && r12.errors().get(1).lineNumber() == 4);

        boolean ioThrown = false;
        try {
            new ClientImporter(new ClientRepository()).importFile(Path.of("data", "no_such_file.csv"));
        } catch (IOException e) {
            ioThrown = true;
        }
        check("13. Importer: несуществующий файл -> IOException (R7)", ioThrown);

        Path f14 = temp("inn;name;email;website\n1234567890;A;a@a.ru;http://a.ru\n"
                + "123456789012;B;b@b.ru;https://b.ru\n");
        check("14. Importer: заголовок пропускается, accepted=2",
                new ClientImporter(new ClientRepository()).importFile(f14).accepted() == 2);

        Path f15 = temp("inn;name;email;website\n1234567890;A;a@a.ru;http://a.ru\n"
                + "1234567890;Dup;d@d.ru;http://d.ru\n");
        ImportResult r15 = new ClientImporter(new ClientRepository()).importFile(f15);
        check("15. Importer: дубликат ИНН отклонён (R8), в errors строка 3",
                r15.accepted() == 1 && r15.rejected() == 1 && r15.errors().get(0).lineNumber() == 3
                        && r15.errors().get(0).message().contains("дубликат"));

        Path f16 = temp("inn;name;email;website\n\n1234567890;A;a@a.ru;http://a.ru\n\n# комментарий\n");
        ImportResult r16 = new ClientImporter(new ClientRepository()).importFile(f16);
        check("16. Importer: пустые строки и комментарии не считаются",
                r16.accepted() == 1 && r16.rejected() == 0);

        // 17-18 ImportResult
        ImportResult res = new ImportResult();
        for (int i = 0; i < 5; i++) {
            res.addAccepted();
        }
        res.addError(new ImportError(3, "ИНН слишком короткий"));
        res.addError(new ImportError(7, "email без @"));
        check("17. ImportResult: accepted=5, rejected=2", res.accepted() == 5 && res.rejected() == 2);
        check("18. ImportResult: формат «строка N: причина» (R5)",
                res.errorMessages().get(0).equals("строка 3: ИНН слишком короткий")
                        && res.errorMessages().get(1).startsWith("строка 7: "));

        // --- вариант 26, часть А: website ---
        check("V26-A1. website https:// OK", valid(validator, "1234567890", "Иван", "a@b.ru", "https://example.com"));
        check("V26-A2. website http:// OK", valid(validator, "1234567890", "Иван", "a@b.ru", "http://example.com/x"));
        check("V26-A3. website без схемы -> ошибка",
                invalid(validator, "1234567890", "Иван", "a@b.ru", "example.com", "http:// или https://"));
        check("V26-A4. website пустой -> ошибка",
                invalid(validator, "1234567890", "Иван", "a@b.ru", "", "website не должен быть пустым"));
        check("V26-A5. website «https://» без домена -> ошибка",
                invalid(validator, "1234567890", "Иван", "a@b.ru", "https://", "не указан домен"));
        check("V26-A6. Parser: 3 поля (нет website) -> IAE", parseFails(parser, "1234567890;Иван;a@b.ru", "ожидается 4 поля"));
        // --- вариант 26, часть Б: addWebsiteDomain ---
        check("V26-B1. addWebsiteDomain(https://example.com/a?b=1) = example.com",
                validator.addWebsiteDomain("https://example.com/a?b=1").equals("example.com"));
        check("V26-B2. addWebsiteDomain(http://host.ru:8080/x) = host.ru",
                validator.addWebsiteDomain("http://host.ru:8080/x").equals("host.ru"));
        check("V26-B3. addWebsiteDomain(ftp://x.ru) = пустая строка",
                validator.addWebsiteDomain("ftp://x.ru").isEmpty());
        check("V26-B4. addWebsiteDomain(null) и addWebsiteDomain(\"https://\") = пустая строка",
                validator.addWebsiteDomain(null).isEmpty() && validator.addWebsiteDomain("https://").isEmpty());

        // --- граничные проверки ---
        check("B1. ИНН 9 цифр (нижняя граница) -> ошибка",
                invalid(validator, "123456789", "Иван", "a@b.ru", "https://x.ru", "10 или 12 цифр"));
        check("B2. ИНН 13 цифр (верхняя граница) -> ошибка",
                invalid(validator, "1234567890123", "Иван", "a@b.ru", "https://x.ru", "10 или 12 цифр"));
        check("B3. email с двумя @ -> ошибка",
                invalid(validator, "1234567890", "Иван", "a@@b.ru", "https://x.ru", "ровно один @"));
        check("B4. email с пустой правой частью -> ошибка",
                invalid(validator, "1234567890", "Иван", "a@", "https://x.ru", "пустая правая часть"));
        check("B5. пустое имя -> ошибка",
                invalid(validator, "1234567890", "", "a@b.ru", "https://x.ru", "имя"));
        check("B6. null-строка в Parser -> IAE", parseFails(parser, null, "пустая строка"));
        Path empty = temp("");
        check("B7. пустой файл -> accepted=0, rejected=0 без исключения",
                new ClientImporter(new ClientRepository()).importFile(empty).accepted() == 0);
    }

    private static boolean parseFails(ClientCsvParser p, String line, String fragment) {
        try {
            p.parse(line, 2);
            return false;
        } catch (IllegalArgumentException e) {
            return e.getMessage().contains(fragment);
        }
    }

    private static boolean valid(ClientValidator v, String inn, String name, String email, String site) {
        return v.validate(new ClientDraft(inn, name, email, site)).isEmpty();
    }

    private static boolean invalid(ClientValidator v, String inn, String name, String email, String site,
                                   String fragment) {
        List<String> problems = v.validate(new ClientDraft(inn, name, email, site));
        for (String p : problems) {
            if (p.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private static Path temp(String content) throws IOException {
        Path p = Files.createTempFile("pr11-", ".csv");
        Files.writeString(p, content, StandardCharsets.UTF_8);
        p.toFile().deleteOnExit();
        return p;
    }

    private static void check(String name, boolean ok) {
        total++;
        if (ok) {
            passed++;
        }
        System.out.println((ok ? "PASS: " : "FAIL: ") + name);
    }

    // ----------------------------------------------------------- data-driven

    /** Каждая строка sample.csv: данные + последняя колонка expected (OK или ERR:фрагмент причины). */
    private static void dataDriven(Path file) throws IOException {
        ClientImporter importer = new ClientImporter(new ClientRepository());
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int n = i + 1;
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            int cut = line.lastIndexOf(';');
            String data = line.substring(0, cut);
            String expected = line.substring(cut + 1);
            ImportResult r = new ImportResult();
            importer.processLine(data, n, r);
            boolean ok;
            String detail = "";
            if (expected.equals("OK")) {
                ok = r.accepted() == 1 && r.rejected() == 0;
                detail = ": ожидалась успешная запись, получено " + r.errorMessages();
            } else if (expected.startsWith("ERR:")) {
                String fragment = expected.substring(4);
                ok = r.accepted() == 0 && r.rejected() == 1 && r.errors().get(0).message().contains(fragment);
                detail = ": ожидалась ошибка «" + fragment + "», получено " + r;
            } else {
                ok = false;
                detail = ": неизвестное значение expected «" + expected + "»";
            }
            total++;
            if (ok) {
                passed++;
            }
            System.out.println(ok ? "PASS data/" + n + (expected.equals("OK") ? "" : " (Error caught)")
                    : "FAIL data/" + n + detail);
        }
    }

    // ------------------------------------------------------ interactive/import

    private static void interactive() throws IOException {
        ClientImporter importer = new ClientImporter(new ClientRepository());
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.println("Введите строку inn;name;email;website (exit или пустая строка — выход)");
        String line;
        int n = 1;
        while ((line = in.readLine()) != null) {
            if (line.isBlank() || line.equalsIgnoreCase("exit")) {
                break;
            }
            n++;
            ImportResult r = new ImportResult();
            importer.processLine(line, n, r);
            if (r.accepted() == 1) {
                System.out.println("OK: запись принята");
            } else {
                System.out.println("ОШИБКА " + r.errorMessages().get(0));
            }
        }
    }

    private static void importMode(Path file) {
        try {
            ImportResult r = new ClientImporter(new ClientRepository()).importFile(file);
            System.out.println("accepted=" + r.accepted() + ", rejected=" + r.rejected());
            for (String m : r.errorMessages()) {
                System.out.println("  " + m);
            }
        } catch (IOException e) {
            System.out.println("ОШИБКА ФАЙЛА: " + e.getMessage());
        }
    }
}
