package ru.edu.pr07;

/**
 * Постамат (вариант 26, часть А): поддерживает любой запрос,
 * стоимость фиксированная — 200 руб. независимо от расстояния, веса и суммы.
 */
public class LockerPolicy implements DeliveryPolicy {
    /** Единственное место, где задана фиксированная стоимость постамата. */
    public static final double FIXED_COST = 200.0;

    @Override
    public boolean supports(DeliveryRequest req) {
        return true;
    }

    @Override
    public double calculateCost(DeliveryRequest req) {
        return FIXED_COST;
    }
}
