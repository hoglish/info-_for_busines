package ru.edu.pr07;

/** Экспресс-доставка (R7): вес до 10 кг, стоимость max(1000, distance * 100) + 1% суммы заказа. */
public class ExpressPolicy implements DeliveryPolicy {
    private static final double MAX_WEIGHT_KG = 10.0;
    private static final double MIN_COST = 1000.0;
    private static final double RATE_PER_KM = 100.0;
    private static final double INSURANCE_RATE = 0.01;

    @Override
    public boolean supports(DeliveryRequest req) {
        return req.type() == DeliveryType.EXPRESS && req.weightKg() <= MAX_WEIGHT_KG;
    }

    @Override
    public double calculateCost(DeliveryRequest req) {
        return Math.max(MIN_COST, req.distanceKm() * RATE_PER_KM) + req.orderAmount() * INSURANCE_RATE;
    }
}
