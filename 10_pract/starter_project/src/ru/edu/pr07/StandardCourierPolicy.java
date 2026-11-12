package ru.edu.pr07;

/** Стандартный курьер (R6): вес до 20 кг, стоимость max(500, distance * 50). */
public class StandardCourierPolicy implements DeliveryPolicy {
    private static final double MAX_WEIGHT_KG = 20.0;
    private static final double MIN_COST = 500.0;
    private static final double RATE_PER_KM = 50.0;

    @Override
    public boolean supports(DeliveryRequest req) {
        return req.type() == DeliveryType.STANDARD && req.weightKg() <= MAX_WEIGHT_KG;
    }

    @Override
    public double calculateCost(DeliveryRequest req) {
        return Math.max(MIN_COST, req.distanceKm() * RATE_PER_KM);
    }
}
