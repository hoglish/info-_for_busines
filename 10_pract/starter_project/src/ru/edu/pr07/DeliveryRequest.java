package ru.edu.pr07;

/**
 * Запрос на расчёт стоимости доставки.
 * Компактный конструктор отсекает заведомо некорректные данные:
 * отсутствующий тип и отрицательные числа.
 */
public record DeliveryRequest(DeliveryType type, double distanceKm, double weightKg, double orderAmount) {
    public DeliveryRequest {
        if (type == null) {
            throw new IllegalArgumentException("Delivery type must not be null");
        }
        if (distanceKm < 0 || Double.isNaN(distanceKm)) {
            throw new IllegalArgumentException("distanceKm must be >= 0: " + distanceKm);
        }
        if (weightKg < 0 || Double.isNaN(weightKg)) {
            throw new IllegalArgumentException("weightKg must be >= 0: " + weightKg);
        }
        if (orderAmount < 0 || Double.isNaN(orderAmount)) {
            throw new IllegalArgumentException("orderAmount must be >= 0: " + orderAmount);
        }
    }
}
