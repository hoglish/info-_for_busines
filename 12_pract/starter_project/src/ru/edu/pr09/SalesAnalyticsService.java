package ru.edu.pr09;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Аналитика по списку продаж. Ни один метод не изменяет исходную коллекцию,
 * внутри stream нет побочных эффектов (результат собирается терминальной операцией).
 */
public class SalesAnalyticsService {

    /** Порог крупной сделки (включительно). Единственное место, где он задан. */
    public static final double LARGE_DEAL_THRESHOLD = 100_000.0;

    /** Вариант 3: комиссия менеджера в процентах от суммы его продаж. Единственное место, где она задана. */
    public static final double COMMISSION_PERCENT = 20.0;

    /** Основной порядок: сумма по убыванию, при равенстве — id по возрастанию (два поля сортировки). */
    public static final Comparator<Sale> BY_AMOUNT_DESC_THEN_ID =
            Comparator.comparingDouble(Sale::amount).reversed().thenComparing(Sale::id);

    /** Продажи с суммой не меньше min, лучшие первыми, не более limit штук. */
    public List<Sale> topSales(List<Sale> sales, double min, int limit) {
        requireSales(sales);
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0: " + limit);
        }
        return sales.stream()
                .filter(s -> s.amount() >= min)
                .sorted(BY_AMOUNT_DESC_THEN_ID)
                .limit(limit)
                .toList();
    }

    /** Самая крупная продажа; при равенстве сумм — с меньшим id. Пустой список -> Optional.empty(). */
    public Optional<Sale> maxSale(List<Sale> sales) {
        requireSales(sales);
        return sales.stream().min(BY_AMOUNT_DESC_THEN_ID);
    }

    /** Сумма продаж по менеджерам. Всегда новый Map, ключи отсортированы по имени. */
    public Map<String, Double> totalsByManager(List<Sale> sales) {
        requireSales(sales);
        return sales.stream().collect(Collectors.groupingBy(
                Sale::manager, TreeMap::new, Collectors.summingDouble(Sale::amount)));
    }

    /** Средняя сумма продаж региона; 0.0, если продаж региона нет. */
    public double averageByRegion(List<Sale> sales, String region) {
        requireSales(sales);
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("region must not be empty");
        }
        return sales.stream()
                .filter(s -> s.region().equals(region))
                .mapToDouble(Sale::amount)
                .average()
                .orElse(0.0);
    }

    /** Императивная версия averageByRegion — для сравнения читаемости и проверки эквивалентности. */
    double averageByRegionImperative(List<Sale> sales, String region) {
        requireSales(sales);
        double sum = 0;
        int count = 0;
        for (Sale s : sales) {
            if (s.region().equals(region)) {
                sum += s.amount();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    /** Крупные сделки: исправленный вариант фрагмента с forEach(result::add) — собираем через toList(). */
    public List<Sale> largeSales(List<Sale> sales) {
        requireSales(sales);
        return sales.stream().filter(this::isLarge).sorted(BY_AMOUNT_DESC_THEN_ID).toList();
    }

    private boolean isLarge(Sale s) {
        return s.amount() >= LARGE_DEAL_THRESHOLD;
    }

    /** Вариант 3, дополнительный агрегат: комиссия менеджера = COMMISSION_PERCENT % от суммы его продаж. */
    public Map<String, Double> commissionByManager(List<Sale> sales) {
        requireSales(sales);
        return sales.stream().collect(Collectors.groupingBy(
                Sale::manager, TreeMap::new,
                Collectors.summingDouble(s -> s.amount() * COMMISSION_PERCENT / 100.0)));
    }

    private static void requireSales(List<Sale> sales) {
        if (sales == null) {
            throw new IllegalArgumentException("sales must not be null");
        }
    }
}
