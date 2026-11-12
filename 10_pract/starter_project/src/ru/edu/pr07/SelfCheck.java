package ru.edu.pr07;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Тестовый раннер и точка входа.
 *   (без аргументов)  — 22 unit-проверки + data-driven режим по data/sample.csv
 *   --interactive     — ввод type distance weight order из System.in
 */
public class SelfCheck {
    private static final double EPS = 0.001;
    private static int passed = 0;
    private static int total = 0;

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("--interactive")) {
            interactive();
            return;
        }
        System.out.println("--- Запуск Unit-тестов ---");
        unitChecks();
        System.out.println();
        System.out.println("--- Запуск Data-Driven тестов (sample.csv) ---");
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

    private static void unitChecks() {
        DeliveryPolicyFactory factory = new DeliveryPolicyFactory();
        DeliveryService service = new DeliveryService(factory);
        PickupPolicy pickup = new PickupPolicy();
        StandardCourierPolicy standard = new StandardCourierPolicy();
        ExpressPolicy express = new ExpressPolicy();

        // 1-2 PickupPolicy
        checkBool("1. PickupPolicy.supports всегда true",
                pickup.supports(req(DeliveryType.PICKUP, 500, 1000, 1)), true);
        checkNum("2. PickupPolicy.calculateCost всегда 0.0",
                pickup.calculateCost(req(DeliveryType.PICKUP, 500, 1000, 1)), 0.0);
        // 3-7 StandardCourierPolicy
        checkBool("3. Standard.supports STANDARD вес 15",
                standard.supports(req(DeliveryType.STANDARD, 1, 15, 1)), true);
        checkBool("4. Standard.supports тип EXPRESS",
                standard.supports(req(DeliveryType.EXPRESS, 1, 5, 1)), false);
        checkBool("5. Standard.supports вес 25",
                standard.supports(req(DeliveryType.STANDARD, 1, 25, 1)), false);
        checkNum("6. Standard.calculateCost 12 км",
                standard.calculateCost(req(DeliveryType.STANDARD, 12, 4.5, 12000)), 600.0);
        checkNum("7. Standard.calculateCost 5 км (минимум 500)",
                standard.calculateCost(req(DeliveryType.STANDARD, 5, 1, 500)), 500.0);
        // 8-11 ExpressPolicy
        checkBool("8. Express.supports EXPRESS вес 5",
                express.supports(req(DeliveryType.EXPRESS, 1, 5, 1)), true);
        checkBool("9. Express.supports EXPRESS вес 15",
                express.supports(req(DeliveryType.EXPRESS, 1, 15, 1)), false);
        checkNum("10. Express.calculateCost 5 км, заказ 4000",
                express.calculateCost(req(DeliveryType.EXPRESS, 5, 1.2, 4000)), 1040.0);
        checkNum("11. Express.calculateCost 20 км, заказ 10000",
                express.calculateCost(req(DeliveryType.EXPRESS, 20, 5, 10000)), 2100.0);
        // 12-14 Factory
        checkBool("12. Factory.get(STANDARD) -> StandardCourierPolicy",
                factory.get(DeliveryType.STANDARD) instanceof StandardCourierPolicy, true);
        checkBool("13. Factory.get(EXPRESS) -> ExpressPolicy",
                factory.get(DeliveryType.EXPRESS) instanceof ExpressPolicy, true);
        checkBool("14. Factory.get(PICKUP) -> PickupPolicy",
                factory.get(DeliveryType.PICKUP) instanceof PickupPolicy, true);
        // 15-18 Service
        checkNum("15. Service.calculate STANDARD 12 км",
                service.calculate(req(DeliveryType.STANDARD, 12, 4.5, 12000)), 600.0);
        checkThrows("16. Service.calculate STANDARD вес 25 -> IAE",
                service, req(DeliveryType.STANDARD, 10, 25, 5000));
        checkNum("17. Service.calculate PICKUP",
                service.calculate(req(DeliveryType.PICKUP, 0, 10, 8000)), 0.0);
        checkNum("18. Service.calculate EXPRESS 5 км",
                service.calculate(req(DeliveryType.EXPRESS, 5, 1.2, 4000)), 1040.0);

        // --- вариант 26, часть А: LOCKER + LockerPolicy ---
        checkBool("V26-A1. Factory.get(LOCKER) -> LockerPolicy",
                factory.get(DeliveryType.LOCKER) instanceof LockerPolicy, true);
        checkBool("V26-A2. LockerPolicy.supports любой запрос (вес 500)",
                new LockerPolicy().supports(req(DeliveryType.LOCKER, 999, 500, 1)), true);
        checkNum("V26-A3. LOCKER стоимость фиксирована 200 (0 км)",
                service.calculate(req(DeliveryType.LOCKER, 0, 1, 100)), 200.0);
        checkNum("V26-A4. LOCKER стоимость фиксирована 200 (300 км)",
                service.calculate(req(DeliveryType.LOCKER, 300, 50, 99999)), 200.0);
        // --- вариант 26, часть Б: addLockerCost ---
        checkNum("V26-B1. addLockerCost для запроса STANDARD",
                service.addLockerCost(req(DeliveryType.STANDARD, 12, 4.5, 12000)), 200.0);
        checkNum("V26-B2. addLockerCost(service, request) для тяжёлого EXPRESS",
                DeliveryService.addLockerCost(service, req(DeliveryType.EXPRESS, 80, 40, 1)), 200.0);

        // --- граничные и защитные проверки ---
        checkBool("B1. вес ровно на лимите STANDARD (20) допустим",
                standard.supports(req(DeliveryType.STANDARD, 1, 20.0, 1)), true);
        checkBool("B2. вес 20.01 STANDARD недопустим",
                standard.supports(req(DeliveryType.STANDARD, 1, 20.01, 1)), false);
        checkBool("B3. вес ровно на лимите EXPRESS (10) допустим",
                express.supports(req(DeliveryType.EXPRESS, 1, 10.0, 1)), true);
        checkNum("B4. 10 км STANDARD = 500 (граница 500 == 10*50)",
                standard.calculateCost(req(DeliveryType.STANDARD, 10, 1, 1)), 500.0);
        checkNum("B5. 10 км EXPRESS = 1000 + 1% (граница 1000 == 10*100)",
                express.calculateCost(req(DeliveryType.EXPRESS, 10, 1, 0)), 1000.0);
        checkCtorThrows("B6. отрицательное расстояние -> IAE при создании запроса",
                DeliveryType.STANDARD, -1, 1, 1);
        checkCtorThrows("B7. отрицательный вес -> IAE",
                DeliveryType.STANDARD, 1, -1, 1);
        checkCtorThrows("B8. отрицательная сумма заказа -> IAE",
                DeliveryType.STANDARD, 1, 1, -5);
        checkCtorThrows("B9. type == null -> IAE",
                null, 1, 1, 1);
    }

    private static DeliveryRequest req(DeliveryType t, double km, double kg, double amount) {
        return new DeliveryRequest(t, km, kg, amount);
    }

    private static void record(String name, boolean ok, String detail) {
        total++;
        if (ok) {
            passed++;
        }
        System.out.println((ok ? "PASS: " : "FAIL: ") + name + (ok ? "" : detail));
    }

    private static void checkBool(String name, boolean actual, boolean expected) {
        record(name, actual == expected, " (ожидалось " + expected + ", получено " + actual + ")");
    }

    private static void checkNum(String name, double actual, double expected) {
        record(name, Math.abs(actual - expected) < EPS, " (ожидалось " + expected + ", получено " + actual + ")");
    }

    private static void checkThrows(String name, DeliveryService service, DeliveryRequest r) {
        boolean thrown = false;
        try {
            service.calculate(r);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        record(name, thrown, " (исключение не выброшено)");
    }

    private static void checkCtorThrows(String name, DeliveryType t, double km, double kg, double amount) {
        boolean thrown = false;
        try {
            new DeliveryRequest(t, km, kg, amount);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        record(name, thrown, " (исключение не выброшено)");
    }

    // ----------------------------------------------------------- data-driven

    private static void dataDriven(Path file) throws IOException {
        DeliveryService service = new DeliveryService(new DeliveryPolicyFactory());
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int n = i + 1;
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split(";", -1);
            String label = "data/" + n;
            if (p.length != 5) {
                record(label, false, ": ожидается 5 полей, получено " + p.length);
                continue;
            }
            try {
                DeliveryRequest r = new DeliveryRequest(DeliveryType.valueOf(p[0]),
                        Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
                String expected = p[4];
                try {
                    double cost = service.calculate(r);
                    if (expected.startsWith("OK:cost=")) {
                        double exp = Double.parseDouble(expected.substring("OK:cost=".length()));
                        record(label, Math.abs(cost - exp) < EPS, ": ожидалось " + exp + ", получено " + cost);
                    } else {
                        record(label, false, ": ожидалась ошибка, получено " + cost);
                    }
                } catch (IllegalArgumentException e) {
                    if (expected.startsWith("SKIP:")) {
                        record(label + " (" + expected + ")", true, "");
                    } else {
                        record(label, false, ": неожиданная ошибка " + e.getMessage());
                    }
                }
            } catch (IllegalArgumentException e) {
                record(label, false, ": некорректная строка (" + e.getMessage() + ")");
            }
        }
    }

    // ----------------------------------------------------------- interactive

    private static void interactive() throws IOException {
        DeliveryService service = new DeliveryService(new DeliveryPolicyFactory());
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.println("Введите: type distanceKm weightKg orderAmount (exit или пустая строка — выход)");
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.equalsIgnoreCase("exit")) {
                break;
            }
            String[] p = line.split("[\\s;]+");
            if (p.length != 4) {
                System.out.println("ОШИБКА: нужно 4 значения, получено " + p.length);
                continue;
            }
            try {
                DeliveryRequest r = new DeliveryRequest(DeliveryType.valueOf(p[0].toUpperCase()),
                        Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
                System.out.println("Стоимость доставки: " + service.calculate(r));
            } catch (IllegalArgumentException e) {
                System.out.println("ОШИБКА: " + e.getMessage());
            }
        }
    }
}
