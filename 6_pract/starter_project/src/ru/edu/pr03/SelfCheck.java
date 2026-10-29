package ru.edu.pr03;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

        System.out.println("\n--- Интерактивный режим (введите параметры или 'exit') ---");
        runInteractiveMode();
    }

    private static void runUnitTests() {
        ApprovalRouter router = new ApprovalRouter();

        // Базовые тесты (1-8)
        testRoute(router, 10000, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK, "Базовый FAST_TRACK");
        testRoute(router, 500000, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW, "Граница FINANCE");
        testRoute(router, 10000, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD, "Базовый STANDARD");
        testRoute(router, 10000, false, RiskLevel.MEDIUM, false, ApprovalRoute.STANDARD, "MEDIUM нейтрален");
        testRoute(router, 10000, false, RiskLevel.LOW, true, ApprovalRoute.SECURITY_REVIEW, "Базовый SECURITY");
        testRoute(router, 499999, true, RiskLevel.LOW, false, ApprovalRoute.FAST_TRACK, "Граница 499999");
        testRoute(router, 500001, false, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW, "Граница 500001");
        testRoute(router, 0, false, RiskLevel.LOW, false, ApprovalRoute.STANDARD, "Нулевая сумма");

        // Тесты пересечений (9-12)
        testRouteError(router, 10000, true, RiskLevel.LOW, true, "Urgent personal data is not allowed", "Пересечение: Personal > Urgent (V26 запрет)");
        testRoute(router, 10000, true, RiskLevel.HIGH, false, ApprovalRoute.DIRECTOR_REVIEW, "Пересечение: HIGH > Urgent");
        testRoute(router, 10000, false, RiskLevel.HIGH, true, ApprovalRoute.SECURITY_REVIEW, "Пересечение: Personal > HIGH");
        testRoute(router, 600000, true, RiskLevel.LOW, false, ApprovalRoute.FINANCE_REVIEW, "Пересечение: Sum > Urgent");
                // Защитные условия (13)
        testRouteError(router, -1, true, RiskLevel.LOW, false, "amount must be >= 0", "Защита: amount < 0");
        testRouteError(router, 10000, true, null, false, "risk is null", "Защита: risk == null");

        // ВАРИАНТ 26, Часть А: Запрет срочных персональных данных
        testRouteError(router, 10000, true, RiskLevel.LOW, true, "Urgent personal data is not allowed", "V26-A: Запрет Urgent+Personal");
        testRoute(router, 10000, false, RiskLevel.LOW, true, ApprovalRoute.SECURITY_REVIEW, "V26-A: Не-Urgent Personal OK");

        // ВАРИАНТ 26, Часть Б: Метод addUrgentPersonalCheck
        testCheck(router, true, true, false, "V26-B: Urgent+Personal = недопустимо");
        testCheck(router, true, false, true, "V26-B: Urgent без Personal = допустимо");
        testCheck(router, false, true, true, "V26-B: Не-Urgent Personal = допустимо");
        testCheck(router, false, false, true, "V26-B: Обычная заявка = допустимо");
    }

    private static void testRoute(ApprovalRouter router, long amount, boolean urgent, RiskLevel risk, boolean personal, ApprovalRoute expected, String desc) {
        totalTests++;
        try {
            ApprovalRoute result = router.route(amount, urgent, risk, personal);
            if (result == expected) {
                passedTests++;
                System.out.println("PASS: " + desc);
            } else {
                System.out.println("FAIL: " + desc + " (Expected " + expected + ", got " + result + ")");
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + desc + " (Unexpected Exception: " + e.getMessage() + ")");
        }
    }

    private static void testRouteError(ApprovalRouter router, long amount, boolean urgent, RiskLevel risk, boolean personal, String msgPart, String desc) {
        totalTests++;
        try {
            router.route(amount, urgent, risk, personal);
            System.out.println("FAIL: " + desc + " (Expected Exception)");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains(msgPart)) {
                passedTests++;
                System.out.println("PASS: " + desc);
            } else {
                System.out.println("FAIL: " + desc + " (Wrong message: " + e.getMessage() + ")");
            }
        }
    }

    private static void testCheck(ApprovalRouter router, boolean urgent, boolean personal, boolean expected, String desc) {
        totalTests++;
        boolean result = router.addUrgentPersonalCheck(urgent, personal);
        if (result == expected) {
            passedTests++;
            System.out.println("PASS: " + desc);
        } else {
            System.out.println("FAIL: " + desc + " (Expected " + expected + ", got " + result + ")");
        }
    }

    private static void runDataDrivenTests() {
        String fileName = "data/sample.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 5) continue;

                String amountStr = parts[0].trim();
                String urgentStr = parts[1].trim();
                String riskStr = parts[2].trim();
                String personalStr = parts[3].trim();
                String expectedStr = parts[4].trim();

                totalTests++;
                try {
                    long amount = Long.parseLong(amountStr);
                    boolean urgent = Boolean.parseBoolean(urgentStr);
                    RiskLevel risk = "null".equals(riskStr) ? null : RiskLevel.valueOf(riskStr);
                    boolean personal = Boolean.parseBoolean(personalStr);

                    ApprovalRouter router = new ApprovalRouter();
                    ApprovalRoute result = router.route(amount, urgent, risk, personal);

                    if (expectedStr.equals(result.name())) {
                        passedTests++;
                        System.out.println("PASS data/" + lineNum);
                    } else {
                        System.out.println("FAIL data/" + lineNum + ": Expected " + expectedStr + ", got " + result.name());
                    }
                } catch (IllegalArgumentException e) {
                    if ("ERROR".equals(expectedStr)) {
                        passedTests++;
                        System.out.println("PASS data/" + lineNum + " (Error caught)");
                    } else {
                        System.out.println("FAIL data/" + lineNum + ": Unexpected error " + e.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("FAIL data/" + lineNum + ": Exception " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("File not found: " + fileName);
        }
    }

    private static void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty() || input.equals("exit")) break;

            String[] parts = input.split(";");
            if (parts.length != 4) {
                System.out.println("Ошибка формата. Введите: amount;urgent;risk;personal");
                continue;
            }

            try {
                long amount = Long.parseLong(parts[0].trim());
                boolean urgent = Boolean.parseBoolean(parts[1].trim());
                RiskLevel risk = "null".equals(parts[2].trim()) ? null : RiskLevel.valueOf(parts[2].trim().toUpperCase());
                boolean personal = Boolean.parseBoolean(parts[3].trim());

                ApprovalRouter router = new ApprovalRouter();
                
                // Демонстрация работы метода из Части Б
                boolean isValid = router.addUrgentPersonalCheck(urgent, personal);
                System.out.println("Предпроверка (Часть Б): " + (isValid ? "ДОПУСТИМО" : "ЗАПРЕЩЕНО"));

                ApprovalRoute route = router.route(amount, urgent, risk, personal);
                System.out.println("Маршрут: " + route.name());

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
            }
        }
        scanner.close();
    }
}