package ru.edu.pr02;

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

        System.out.println("\n--- Интерактивный режим (введите строку или 'exit') ---");
        runInteractiveMode();
    }

    private static void runUnitTests() {
        // Тесты parseHours для Варианта 26 (6 значений)
        testParseHours("8;9;10;8;8;100", new int[]{8, 9, 10, 8, 8}, "Корректная строка V26");
        testParseHoursError("", "Ошибка: структура", "Пустая строка");
        testParseHoursError("8;8;17;8;8;100", "Ошибка: позиция 3", "Значение 17");
        testParseHoursError("8;abc;8;8;8;100", "Ошибка: позиция 2", "Не число");
        testParseHoursError("8;8;8;8;100", "Ошибка: структура", "5 значений вместо 6");
        testParseHoursError("8;8;8;8;8;;100", "Ошибка: структура", "Лишний разделитель");
        testParseHoursError("08;8;8;8;8;100", "Ошибка: позиция 1", "Ведущий ноль");
        
        // Базовые тесты вычислений
        testTotal(new int[]{8, 9, 10, 8, 8}, 43, "Типовое");
        testTotal(new int[]{0, 0, 0, 0, 0}, 0, "Нули");
        testTotal(new int[]{16, 16, 16, 16, 16}, 80, "Максимум");

        testAverage(new int[]{8, 9, 10, 8, 8}, 8.6, "Типовое");
        testAverage(new int[]{8, 8, 8, 8, 8}, 8.0, "Целое");
        testAverage(new int[]{7, 8, 9, 8, 7}, 7.8, "Дробное");

        testOvertimeDays(new int[]{8, 8, 8, 8, 8}, 0, "Нет переработки");
        testOvertimeDays(new int[]{9, 8, 7, 10, 8}, 2, "Два дня");
        testOvertimeDays(new int[]{8, 9, 8, 8, 8}, 1, "Граница 8");

        testWeeklyOvertime(new int[]{8, 8, 8, 8, 8}, 0, "Сумма 40");
        testWeeklyOvertime(new int[]{10, 10, 10, 10, 10}, 10, "Сумма 50");
        testWeeklyOvertime(new int[]{6, 6, 6, 6, 6}, 0, "Сумма < 40");

        // Тесты для Части Б Варианта 26 (addPayForWeek)
        testPayForWeek(new int[]{8, 8, 8, 8, 8}, 100.0, 4000.0, "V26: Без переработки");
        testPayForWeek(new int[]{10, 10, 10, 10, 10}, 100.0, 6500.0, "V26: С переработкой");
    }

    // --- Методы для Unit-тестов ---

    private static void testParseHours(String input, int[] expected, String desc) {
        totalTests++;
        try {
            int[] result = TimesheetAnalyzer.parseHours(input);
            if (java.util.Arrays.equals(result, expected)) {
                passedTests++;
                System.out.println("PASS: " + desc);
            } else {
                System.out.println("FAIL: " + desc + " (Expected " + java.util.Arrays.toString(expected) + ", got " + java.util.Arrays.toString(result) + ")");
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + desc + " (Exception: " + e.getMessage() + ")");
        }
    }

    private static void testParseHoursError(String input, String expectedMsgPart, String desc) {
        totalTests++;
        try {
            TimesheetAnalyzer.parseHours(input);
            System.out.println("FAIL: " + desc + " (Expected Exception)");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains(expectedMsgPart)) {
                passedTests++;
                System.out.println("PASS: " + desc);
            } else {
                System.out.println("FAIL: " + desc + " (Wrong message: " + e.getMessage() + ")");
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + desc + " (Wrong Exception type: " + e.getClass().getSimpleName() + ")");
        }
    }

    private static void testTotal(int[] input, int expected, String desc) {
        totalTests++;
        if (TimesheetAnalyzer.total(input) == expected) {
            passedTests++;
            System.out.println("PASS: " + desc);
        } else {
            System.out.println("FAIL: " + desc);
        }
    }

    private static void testAverage(int[] input, double expected, String desc) {
        totalTests++;
        if (Math.abs(TimesheetAnalyzer.average(input) - expected) < 0.001) {
            passedTests++;
            System.out.println("PASS: " + desc);
        } else {
            System.out.println("FAIL: " + desc);
        }
    }

    private static void testOvertimeDays(int[] input, int expected, String desc) {
        totalTests++;
        if (TimesheetAnalyzer.overtimeDays(input) == expected) {
            passedTests++;
            System.out.println("PASS: " + desc);
        } else {
            System.out.println("FAIL: " + desc);
        }
    }

    private static void testWeeklyOvertime(int[] input, int expected, String desc) {
        totalTests++;
        if (TimesheetAnalyzer.weeklyOvertime(input) == expected) {
            passedTests++;
            System.out.println("PASS: " + desc);
        } else {
            System.out.println("FAIL: " + desc);
        }
    }

    private static void testPayForWeek(int[] hours, double rate, double expected, String desc) {
        totalTests++;
        try {
            double result = TimesheetAnalyzer.addPayForWeek(hours, rate);
            if (Math.abs(result - expected) < 0.01) {
                passedTests++;
                System.out.println("PASS: " + desc);
            } else {
                System.out.println("FAIL: " + desc + " (Expected " + expected + ", got " + result + ")");
            }
        } catch (Exception e) {
            System.out.println("FAIL: " + desc + " (Exception: " + e.getMessage() + ")");
        }
    }

    // --- Data-Driven тесты (чтение sample.csv) ---

    private static void runDataDrivenTests() {
        String fileName = "data/sample.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;

                String inputStr = parts[0].trim();
                String expectedStr = parts[1].trim();
                
                if ("EMPTY".equals(inputStr)) {
                    inputStr = "";
                }

                totalTests++;
                try {
                    int[] hours = TimesheetAnalyzer.parseHours(inputStr);
                    int sum = TimesheetAnalyzer.total(hours);
                    double avg = TimesheetAnalyzer.average(hours);
                    int od = TimesheetAnalyzer.overtimeDays(hours);
                    int wo = TimesheetAnalyzer.weeklyOvertime(hours);

                    String actualResult = "OK:sum=" + sum + ",avg=" + String.format(java.util.Locale.US, "%.2f", avg) + ",od=" + od + ",wo=" + wo;                    
                    if (actualResult.equals(expectedStr)) {
                        passedTests++;
                        System.out.println("PASS data/" + lineNum);
                    } else {
                        System.out.println("FAIL data/" + lineNum + ": Expected " + expectedStr + ", got " + actualResult);
                    }

                } catch (IllegalArgumentException e) {
                    if (expectedStr.startsWith("ERROR")) {
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

    // --- Интерактивный режим ---

    private static void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if (line == null || line.isEmpty() || line.equals("exit")) {
                break;
            }
            try {
                int[] hours = TimesheetAnalyzer.parseHours(line);
                int sum = TimesheetAnalyzer.total(hours);
                double avg = TimesheetAnalyzer.average(hours);
                int od = TimesheetAnalyzer.overtimeDays(hours);
                int wo = TimesheetAnalyzer.weeklyOvertime(hours);
                
                System.out.println("Sum: " + sum);
                System.out.println("Avg: " + String.format("%.2f", avg));
                System.out.println("Overtime Days: " + od);
                System.out.println("Weekly Overtime: " + wo);
                
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        scanner.close();
    }
}