package ru.edu.pr09;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/** Тестовый раннер: типовые случаи, границы, ошибки, неизменяемость источника, data-driven по sample.csv. */
public class SelfCheck {
    private static final double EPS = 0.001;
    private static int passed = 0;
    private static int total = 0;
    private static final SalesAnalyticsService SVC = new SalesAnalyticsService();

    public static void main(String[] args) throws IOException {
        System.out.println("--- Запуск Unit-тестов ---");
        topSalesChecks();
        maxSaleChecks();
        totalsChecks();
        averageChecks();
        streamRuleChecks();
        variantChecks();
        loaderChecks();
        System.out.println();
        System.out.println("--- Запуск Data-Driven тестов (data/sample.csv) ---");
        dataDriven();
        System.out.println();
        System.out.println("--- Итог ---");
        System.out.println("Passed: " + passed + "/" + total);
        System.out.println(passed == total ? "PASS" : "FAIL");
        if (passed != total) {
            System.exit(1);
        }
    }

    private static List<Sale> fixture() {
        return List.of(
                new Sale("S1", "Ivanov", "Moscow", 150000),
                new Sale("S2", "Petrova", "SPb", 92000),
                new Sale("S3", "Ivanov", "Moscow", 240000),
                new Sale("S4", "Lee", "Kazan", 45000));
    }

    // ---- topSales
    private static void topSalesChecks() {
        List<Sale> s = fixture();
        check("topSales: типовой случай (min 100000, limit 5) -> S3, S1", () -> {
            List<Sale> r = SVC.topSales(s, 100000, 5);
            return r.size() == 2 && r.get(0).id().equals("S3") && r.get(1).id().equals("S1");
        });
        check("topSales: нижняя граница min включается (150000 -> S1 есть)", () ->
                SVC.topSales(s, 150000, 10).size() == 2);
        check("topSales: min чуть выше суммы исключает (150000.01 -> только S3)", () ->
                SVC.topSales(s, 150000.01, 10).size() == 1);
        check("topSales: limit ограничивает результат (limit 1 -> S3)", () ->
                SVC.topSales(s, 0, 1).get(0).id().equals("S3") && SVC.topSales(s, 0, 1).size() == 1);
        check("topSales: limit 0 -> пустой список", () -> SVC.topSales(s, 0, 0).isEmpty());
        check("topSales: limit < 0 -> IAE", () -> throwsIae(() -> SVC.topSales(s, 0, -1)));
        check("topSales: null-список -> IAE", () -> throwsIae(() -> SVC.topSales(null, 0, 1)));
        check("topSales: пустой список -> пустой результат", () -> SVC.topSales(List.of(), 0, 5).isEmpty());
        check("topSales: равные суммы -> порядок по id (S3 раньше S5)", () -> {
            List<Sale> t = List.of(new Sale("S5", "A", "R", 10), new Sale("S3", "A", "R", 10));
            List<Sale> r = SVC.topSales(t, 0, 5);
            return r.get(0).id().equals("S3") && r.get(1).id().equals("S5");
        });
    }

    // ---- maxSale
    private static void maxSaleChecks() {
        check("maxSale: типовой случай -> S3", () -> SVC.maxSale(fixture()).get().id().equals("S3"));
        check("maxSale: пустой список -> Optional.empty", () -> SVC.maxSale(List.of()).isEmpty());
        check("maxSale: ничья по сумме -> меньший id", () -> {
            List<Sale> t = List.of(new Sale("S5", "A", "R", 10), new Sale("S3", "A", "R", 10));
            Optional<Sale> r = SVC.maxSale(t);
            return r.isPresent() && r.get().id().equals("S3");
        });
        check("maxSale: один элемент", () ->
                SVC.maxSale(List.of(new Sale("X", "A", "R", 0))).get().id().equals("X"));
        check("maxSale: null -> IAE", () -> throwsIae(() -> SVC.maxSale(null)));
    }

    // ---- totalsByManager
    private static void totalsChecks() {
        check("totalsByManager: Ivanov 390000, Petrova 92000, Lee 45000", () -> {
            Map<String, Double> m = SVC.totalsByManager(fixture());
            return m.size() == 3 && near(m.get("Ivanov"), 390000) && near(m.get("Petrova"), 92000)
                    && near(m.get("Lee"), 45000);
        });
        check("totalsByManager: пустой список -> пустой Map", () -> SVC.totalsByManager(List.of()).isEmpty());
        check("totalsByManager: возвращает НОВЫЙ изменяемый Map, повторный вызов независим", () -> {
            List<Sale> s = fixture();
            Map<String, Double> a = SVC.totalsByManager(s);
            a.put("Hacker", 1.0);
            return !SVC.totalsByManager(s).containsKey("Hacker");
        });
        check("totalsByManager: ключи по алфавиту", () ->
                SVC.totalsByManager(fixture()).keySet().iterator().next().equals("Ivanov"));
    }

    // ---- averageByRegion
    private static void averageChecks() {
        check("averageByRegion: Moscow = 195000", () -> near(SVC.averageByRegion(fixture(), "Moscow"), 195000));
        check("averageByRegion: нет продаж региона -> 0.0", () -> near(SVC.averageByRegion(fixture(), "Omsk"), 0.0));
        check("averageByRegion: пустой регион -> IAE", () -> throwsIae(() -> SVC.averageByRegion(fixture(), " ")));
        check("averageByRegion: null регион -> IAE", () -> throwsIae(() -> SVC.averageByRegion(fixture(), null)));
        check("averageByRegion: stream и цикл дают одинаковый результат", () -> {
            List<Sale> s = fixture();
            return near(SVC.averageByRegion(s, "SPb"), SVC.averageByRegionImperative(s, "SPb"))
                    && near(SVC.averageByRegion(s, "Moscow"), SVC.averageByRegionImperative(s, "Moscow"))
                    && near(SVC.averageByRegion(s, "Omsk"), SVC.averageByRegionImperative(s, "Omsk"));
        });
    }

    // ---- обязательные правила: источник не меняется, нет побочных эффектов
    private static void streamRuleChecks() {
        check("Исходный список не изменяется (копия до == копия после)", () -> {
            List<Sale> s = new ArrayList<>(fixture());
            List<Sale> before = new ArrayList<>(s);
            SVC.topSales(s, 0, 10);
            SVC.maxSale(s);
            SVC.totalsByManager(s);
            SVC.averageByRegion(s, "Moscow");
            SVC.largeSales(s);
            SVC.commissionByManager(s);
            return s.equals(before);
        });
        check("Неизменяемый List.of не вызывает UnsupportedOperationException", () -> {
            try {
                SVC.topSales(fixture(), 0, 10);
                SVC.largeSales(fixture());
                return true;
            } catch (UnsupportedOperationException e) {
                return false;
            }
        });
        check("largeSales: порог 100000 включительно (S3, S1)", () -> {
            List<Sale> r = SVC.largeSales(fixture());
            return r.size() == 2 && r.get(0).id().equals("S3") && r.get(1).id().equals("S1");
        });
        check("largeSales: сумма ровно на пороге попадает в результат", () ->
                SVC.largeSales(List.of(new Sale("Z", "A", "R", SalesAnalyticsService.LARGE_DEAL_THRESHOLD))).size() == 1);
        check("largeSales: сумма чуть ниже порога не попадает", () ->
                SVC.largeSales(List.of(new Sale("Z", "A", "R", 99999.99))).isEmpty());
    }

    // ---- вариант 3: дополнительный агрегат (комиссия 20 %)
    private static void variantChecks() {
        check("V3-1. commissionByManager: Ivanov 78000 (20% от 390000)", () ->
                near(SVC.commissionByManager(fixture()).get("Ivanov"), 78000));
        check("V3-2. commissionByManager: Lee 9000 (20% от 45000)", () ->
                near(SVC.commissionByManager(fixture()).get("Lee"), 9000));
        check("V3-3. commissionByManager: пустой список -> пустой Map", () ->
                SVC.commissionByManager(List.of()).isEmpty());
        check("V3-4. commissionByManager: нулевая сумма -> комиссия 0.0", () ->
                near(SVC.commissionByManager(List.of(new Sale("Z", "A", "R", 0))).get("A"), 0.0));
        check("V3-5. комиссия = сумма * COMMISSION_PERCENT / 100 (параметр в одной константе)", () -> {
            double expected = 390000 * SalesAnalyticsService.COMMISSION_PERCENT / 100.0;
            return near(SVC.commissionByManager(fixture()).get("Ivanov"), expected)
                    && SalesAnalyticsService.COMMISSION_PERCENT == 20.0;
        });
    }

    // ---- загрузчик CSV: неверный формат, пустое значение, повтор
    private static void loaderChecks() {
        check("Loader: корректные строки", () ->
                SaleCsvLoader.parse(List.of("h", "A;M;R;10", "B;M;R;20")).size() == 2);
        check("Loader: неверное число полей -> IAE", () ->
                throwsIae(() -> SaleCsvLoader.parse(List.of("h", "A;M;R"))));
        check("Loader: сумма не число -> IAE", () ->
                throwsIae(() -> SaleCsvLoader.parse(List.of("h", "A;M;R;abc"))));
        check("Loader: пустой manager -> IAE", () ->
                throwsIae(() -> SaleCsvLoader.parse(List.of("h", "A;;R;10"))));
        check("Loader: отрицательная сумма -> IAE", () ->
                throwsIae(() -> SaleCsvLoader.parse(List.of("h", "A;M;R;-5"))));
        check("Loader: повторный id -> IAE", () ->
                throwsIae(() -> SaleCsvLoader.parse(List.of("h", "A;M;R;1", "A;N;R;2"))));
    }

    // ---- data-driven: ожидания рассчитаны вручную по data/sample.csv
    private static void dataDriven() throws IOException {
        List<Sale> sales = SaleCsvLoader.load(Path.of("data", "sample.csv"));
        check("data: загружено 7 продаж", () -> sales.size() == 7);
        check("data: maxSale = S3 (ничья S3/S5 разрешена по id)", () -> SVC.maxSale(sales).get().id().equals("S3"));
        check("data: totals Ivanov 390000, Petrova 332000, Lee 145000", () -> {
            Map<String, Double> m = SVC.totalsByManager(sales);
            return near(m.get("Ivanov"), 390000) && near(m.get("Petrova"), 332000) && near(m.get("Lee"), 145000);
        });
        check("data: averageByRegion(SPb) = 110666.67", () ->
                Math.abs(SVC.averageByRegion(sales, "SPb") - 110666.6667) < 0.01);
        check("data: largeSales = S3, S5, S1, S6 (S6 ровно на пороге)", () -> {
            List<Sale> r = SVC.largeSales(sales);
            return r.size() == 4 && r.get(0).id().equals("S3") && r.get(1).id().equals("S5")
                    && r.get(2).id().equals("S1") && r.get(3).id().equals("S6");
        });
        check("data: commission Petrova = 66400", () -> near(SVC.commissionByManager(sales).get("Petrova"), 66400));
    }

    // ---- утилиты
    private static boolean near(double a, double b) {
        return Math.abs(a - b) < EPS;
    }

    private static boolean throwsIae(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void check(String name, BooleanSupplier test) {
        total++;
        boolean ok;
        try {
            ok = test.getAsBoolean();
        } catch (Exception e) {
            System.out.println("FAIL: " + name + " (исключение " + e + ")");
            return;
        }
        if (ok) {
            passed++;
        }
        System.out.println((ok ? "PASS: " : "FAIL: ") + name);
    }
}
