package ru.edu.pr04;

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

        System.out.println("\n--- Интерактивный режим (введите команду или 'exit') ---");
        runInteractiveMode();
    }

    private static void runUnitTests() {
        // Базовые тесты Product (R1-R3)
        testProductCreation("P1", "Mouse", 1000, true, "Корректный Product");
        testProductCreation(null, "Mouse", 1000, false, "Product: id=null");
        testProductCreation("P1", "", 1000, false, "Product: name=blank");
        testProductCreation("P1", "Mouse", 0, false, "Product: price=0");
        testProductCreation("P1", "Mouse", -100, false, "Product: price<0");

        // Базовые тесты OrderItem (R4-R5)
        Product p = new Product("P1", "Mouse", 1000);
        testOrderItemCreation(p, 2, true, "Корректный OrderItem");
        testOrderItemCreation(p, 0, false, "OrderItem: qty=0");
        testOrderItemCreation(p, 101, false, "OrderItem: qty=101");
        testOrderItemCreation(p, 1, true, "OrderItem: qty=1 (граница)");
        testOrderItemCreation(p, 100, true, "OrderItem: qty=100 (граница)");

        // Тесты Order (R6-R11)
        Employee emp = new Employee("E1", "Ivanov");
        Order order = new Order(emp);
        Product m = new Product("M01", "Monitor", 30000);
        Product k = new Product("K01", "Keyboard", 5000);

        order.addProduct(m, 2);
        order.addProduct(k, 3);
        testCondition(order.getItems().size() == 2, "Order: 2 разных товара");

        // ИСПРАВЛЕНО: в заказе уже 2 разные позиции (Monitor и Keyboard).
        // После addProduct(m, 1) объединяется только Monitor -> size остаётся 2,
        // а количество Monitor становится 3.
        order.addProduct(m, 1);
        int monitorQty = order.getItems().stream()
                .filter(i -> i.getProduct().getId().equals("M01"))
                .findFirst()
                .orElseThrow()
                .getQuantity();
        testCondition(order.getItems().size() == 2 && monitorQty == 3,
                "Order: объединение повторов R7");

        // ИСПРАВЛЕНО: сумма = Monitor(30000*3) + Keyboard(5000*3) = 90000 + 15000 = 105000
        testCondition(Math.abs(order.total() - 105000) < 0.01, "Order: total() R9");

        order.confirm();
        testCondition(order.getStatus() == OrderStatus.CONFIRMED, "Order: confirm() R8");

        testException(() -> order.addProduct(k, 1), IllegalStateException.class, "Order: add после confirm R8");
        testException(() -> new Order(emp).confirm(), IllegalStateException.class, "Order: confirm пустого R10");

        // Тесты Варианта 26 (Часть А и Б)
        OrderRepository repo = new OrderRepository();
        OrderService service = new OrderService(repo);
        Order o1 = service.createOrder(new Employee("E2", "Petrov"));
        Order o2 = service.createOrder(new Employee("E3", "Sidorov"));

        testCondition(repo.findAll().size() == 2, "V26-A: Repository содержит 2 заказа");
        testCondition(addRepositoryCount(repo) == 2, "V26-B: addRepositoryCount вернул 2");
    }

    private static void testProductCreation(String id, String name, double price, boolean shouldPass, String desc) {
        totalTests++;
        try {
            new Product(id, name, price);
            if (shouldPass) { passedTests++; System.out.println("PASS: " + desc); }
            else System.out.println("FAIL: " + desc + " (Expected Exception)");
        } catch (IllegalArgumentException e) {
            if (!shouldPass) { passedTests++; System.out.println("PASS: " + desc); }
            else System.out.println("FAIL: " + desc + " (" + e.getMessage() + ")");
        }
    }

    private static void testOrderItemCreation(Product p, int qty, boolean shouldPass, String desc) {
        totalTests++;
        try {
            new OrderItem(p, qty);
            if (shouldPass) { passedTests++; System.out.println("PASS: " + desc); }
            else System.out.println("FAIL: " + desc + " (Expected Exception)");
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
            System.out.println("FAIL: " + desc + " (Expected Exception)");
        } catch (Exception e) {
            if (expectedType.isInstance(e)) { passedTests++; System.out.println("PASS: " + desc); }
            else System.out.println("FAIL: " + desc + " (Wrong type: " + e.getClass().getSimpleName() + ")");
        }
    }

    // ВАРИАНТ 26, Часть Б
    public static int addRepositoryCount(OrderRepository repo) {
        return repo.findAll().size();
    }

    private static void runDataDrivenTests() {
        String fileName = "data/sample.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNum = 0;
            boolean isFirstLine = true; // Флаг для пропуска заголовка

            while ((line = br.readLine()) != null) {
                lineNum++;

                // Пропускаем первую строку (заголовок)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.startsWith("#") || line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 5) continue;

                String prodId = parts[0].trim();
                String name = parts[1].trim();
                String priceStr = parts[2].trim();
                String qtyStr = parts[3].trim();
                String expected = parts[4].trim();

                totalTests++;
                try {
                    double price = Double.parseDouble(priceStr);
                    int qty = Integer.parseInt(qtyStr);

                    Product product = new Product(prodId, name, price);
                    OrderItem item = new OrderItem(product, qty);

                    if (expected.startsWith("OK")) {
                        passedTests++;
                        System.out.println("PASS data/" + lineNum);
                    } else {
                        System.out.println("FAIL data/" + lineNum + ": Expected " + expected);
                    }
                } catch (IllegalArgumentException e) {
                    if (expected.startsWith("SKIP")) {
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
        OrderRepository repo = new OrderRepository();
        OrderService service = new OrderService(repo);
        Employee emp = new Employee("E1", "Test User");
        Order currentOrder = service.createOrder(emp);

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty() || input.equals("exit")) break;

            String[] cmd = input.split(" ");
            try {
                switch (cmd[0]) {
                    case "add":
                        if (cmd.length == 4) {
                            Product p = new Product(cmd[1], cmd[2], Double.parseDouble(cmd[3]));
                            currentOrder.addProduct(p, 1);
                            System.out.println("Added " + cmd[2]);
                        } else System.out.println("Usage: add ID NAME PRICE");
                        break;
                    case "total":
                        System.out.println("Total: " + currentOrder.total());
                        break;
                    case "confirm":
                        currentOrder.confirm();
                        System.out.println("Confirmed. Items: " + currentOrder.getItems().size());
                        break;
                    case "repo":
                        System.out.println("Repo size: " + addRepositoryCount(repo));
                        break;
                    default:
                        System.out.println("Unknown command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}