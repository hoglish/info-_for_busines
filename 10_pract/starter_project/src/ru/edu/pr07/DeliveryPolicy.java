package ru.edu.pr07;

/** Единый контракт политики расчёта доставки (R1). */
public interface DeliveryPolicy {
    boolean supports(DeliveryRequest request);

    double calculateCost(DeliveryRequest request);
}
