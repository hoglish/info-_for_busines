package ru.edu.pr05;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;

public class SelfCheck {
    private static int passedTests = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("--- Запуск Unit-тестов ---");
        runUnitTests();

        System.out.println("\n--- Запуск Data-Driven тестов (sample.csv) ---");
        runDataDrivenTests();

        System.out.println("\n--- Итог Unit/Data тестов ---");
        System.out.println("Passed: " + passedTests + "/" + totalTests);

        System.out.println("\n--- Интерактивный режим (введите 'help', 'exit' для выхода) ---");
        runInteractiveMode();
    }

    private static void runUnitTests() {
        LocalDate s = LocalDate.of(2026, 10, 10);
        LocalDate e = LocalDate.of(2026, 10, 12);
        LocalDate today = LocalDate.of(2026, 10, 15); // после e
        LocalDate early = LocalDate.of(2026, 10, 11); // до e

        // === 1-5: конструктор ===
        testConstructor("T-01", s, e, true, "1. Конструктор: корректные данные");
        testConstructor(null, s, e, false, "2. Конструктор: id=null");
        testConstructor("", s, e, false, "3. Конструктор: id=\"\"");
        testConstructor("T-02", null, e, false, "4. Конструктор: start=null");
        testConstructor("T-03", e, s, false, "5. Конструктор: start > end");

        // === 6-7: submit ===
        BusinessTrip t6 = new BusinessTrip("T-04", s, e);
        t6.submit();
        testCondition(t6.getStatus() == TripStatus.SUBMITTED, "6. submit из DRAFT (R3)");
        testException(t6::submit, IllegalStateException.class, "7. submit из SUBMITTED (R3)");

        // === 8-9: approve ===
        BusinessTrip t8 = new BusinessTrip("T-05", s, e);
        t8.submit();
        t8.approve();
        testCondition(t8.getStatus() == TripStatus.APPROVED, "8. approve из SUBMITTED (R5)");

        BusinessTrip t9 = new BusinessTrip("T-06", s, e);
        testException(t9::approve, IllegalStateException.class, "9. approve из DRAFT (R5)");

        // === 10-12: reject ===
        BusinessTrip t10 = new BusinessTrip("T-07", s, e);
        t10.submit();
        t10.reject("1234567890"); // ровно 10
        testCondition(t10.getStatus() == TripStatus.REJECTED, "10. reject: причина 10 символов (R6)");
        testCondition("1234567890".equals(t10.getRejectionReason()), "11. reject: причина сохранена");

        BusinessTrip t12 = new BusinessTrip("T-08", s, e);
        t12.submit();
        testException(() -> t12.reject("123"), IllegalArgumentException.class, "12. reject: < 10 символов (R6)");

        // === 13-14: cancel ===
        BusinessTrip t13 = new BusinessTrip("T-09", s, e);
        t13.cancel();
        testCondition(t13.getStatus() == TripStatus.CANCELLED, "13. cancel из DRAFT (R7)");

        BusinessTrip t14 = new BusinessTrip("T-10", s, e);
        t14.submit();
        t14.approve();
        t14.complete(today);
        testException(t14::cancel, IllegalStateException.class, "14. cancel из COMPLETED (R7)");

        // === 15-17: complete ===
        BusinessTrip t15 = new BusinessTrip("T-11", s, e);
        t15.submit();
        t15.approve();
        t15.complete(today);
        testCondition(t15.getStatus() == TripStatus.COMPLETED, "15. complete из APPROVED, today > end (R8, R9)");

        BusinessTrip t16 = new BusinessTrip("T-12", s, e);
        t16.submit();
        testException(() -> t16.complete(today), IllegalStateException.class, "16. complete из SUBMITTED (R8)");

        BusinessTrip t17 = new BusinessTrip("T-13", s, e);
        t17.submit();
        t17.approve();
        testException(() -> t17.complete(early), IllegalStateException.class, "17. complete: today < end (R9)");

        // === 18: getRejectionReason ===
        BusinessTrip t18 = new BusinessTrip("T-14", s, e);
        t18.submit();
        t18.reject("Причина отказа");
        testCondition(t18.getRejectionReason() != null && t18.getRejectionReason().length() >= 10,
                "18. getRejectionReason возвращает сохранённую причину");

        // === ВАРИАНТ 26, Часть А (2 проверки) ===
        BusinessTripService serviceA = new BusinessTripService();
        BusinessTrip active = serviceA.create("EMP-01", s, e);
        active.submit(); // SUBMITTED -> активная
        testException(() -> serviceA.create("EMP-02", s, e),
                IllegalStateException.class,
                "19. V26-A: блокировка второй активной заявки (SUBMITTED)");

        active.approve(); // APPROVED -> всё ещё активная
        testException(() -> serviceA.create("EMP-03", s, e),
                IllegalStateException.class,
                "20. V26-A: блокировка при APPROVED");

        // === ВАРИАНТ 26, Часть Б (2 проверки) ===
        BusinessTripService serviceB = new BusinessTripService();
        BusinessTrip b1 = serviceB.create("B-01", s, e);
        b1.submit();
        testCondition(serviceB.addActiveTripsCount() == 1, "21. V26-B: 1 активная (SUBMITTED)");

        b1.cancel();
        testCondition(serviceB.addActiveTripsCount() == 0, "22. V26-B: 0 активных после отмены");
    }

    private static void testConstructor(String id, LocalDate start, LocalDate end, boolean shouldPass, String desc) {
        totalTests++;
        try {
            new BusinessTrip(id, start, end);
            if (shouldPass) { passedTests++; System.out.println("PASS: " + desc); }
            else System.out.println("FAIL: " + desc + " (ожидалось исключение)");
        } catch (IllegalArgumentException e) {
            if (!shouldPass) { passedTests++; System.out.println("PASS: " + desc); }
            else System.out.println("FAIL: " + desc + " (" + e.getMessage() + ")");
        }
    }

    private static void testCondition(boolean condition, String desc) {
        totalTests++;
        if (condition) { passedTests++; System.out.println("PASS: " + desc); }
        else System.out.println("FAIL: " + desc);
    }

    private static void testException(Runnable action, Class<? extends Exception> expectedType, String desc) {
        totalTests++;
        try {
            action.run();
            System.out.println("FAIL: " + desc + " (ожидалось исключение)");
        } catch (Exception e) {
            if (expectedType.isInstance(e)) { passedTests++; System.out.println("PASS: " + desc); }
            else System.out.println("FAIL: " + desc + " (неверный тип: " + e.getClass().getSimpleName() + ")");
        }
    }

    private static void runDataDrivenTests() {
        String fileName = "data/sample.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNum = 0;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 5) continue;

                String id = parts[0].trim();
                String startStr = parts[1].trim();
                String endStr = parts[2].trim();
                String action = parts[3].trim();
                String expected = parts[4].trim();

                totalTests++;
                try {
                    LocalDate start = LocalDate.parse(startStr);
                    LocalDate end = LocalDate.parse(endStr);

                    BusinessTripService service = new BusinessTripService();
                    BusinessTrip trip = service.create(id, start, end);

                    // Одно действие из CSV (как в задании 1.4)
                    switch (action) {
                        case "SUBMIT" -> trip.submit();
                        case "APPROVE" -> trip.approve();
                        case "REJECT" -> trip.reject("Причина отклонения 123");
                        case "CANCEL" -> trip.cancel();
                        case "COMPLETE" -> trip.complete(LocalDate.now().plusDays(1));
                    }

                    String actual = "OK:status=" + trip.getStatus();
                    if (actual.equals(expected)) {
                        passedTests++;
                        System.out.println("PASS data/" + lineNum);
                    } else {
                        System.out.println("FAIL data/" + lineNum + ": expected " + expected + ", got " + actual);
                    }
                } catch (Exception ex) {
                    if (expected.startsWith("SKIP") || expected.startsWith("ERROR")) {
                        passedTests++;
                        System.out.println("PASS data/" + lineNum + " (поймано: " + ex.getClass().getSimpleName() + ")");
                    } else {
                        System.out.println("FAIL data/" + lineNum + ": неожиданное исключение " + ex.getMessage());
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("File not found: " + fileName);
        }
    }

    private static void runInteractiveMode() {
        Scanner sc = new Scanner(System.in);
        BusinessTripService service = new BusinessTripService();

        while (true) {
            String input = sc.nextLine().trim();
            if (input.isEmpty() || input.equals("exit")) break;
            String[] cmd = input.split(" ");
            try {
                switch (cmd[0]) {
                    case "create":
                        if (cmd.length == 4) {
                            BusinessTrip t = service.create(cmd[1],
                                    LocalDate.parse(cmd[2]), LocalDate.parse(cmd[3]));
                            System.out.println("Created " + t.getId() + " [" + t.getStatus() + "]");
                        } else System.out.println("Usage: create ID YYYY-MM-DD YYYY-MM-DD");
                        break;
                    case "active":
                        System.out.println("Active count: " + service.addActiveTripsCount());
                        break;
                    case "list":
                        for (BusinessTrip t : service.findAll()) {
                            System.out.println(t.getId() + " " + t.getStatus());
                        }
                        break;
                    case "help":
                        System.out.println("create ID START END | active | list | exit");
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help'.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
        sc.close();
    }
}