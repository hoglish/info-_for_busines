package ru.edu.pr07;

/** Самовывоз (R5): поддерживает любой запрос, стоимость всегда 0. */
public class PickupPolicy implements DeliveryPolicy {
    @Override
    public boolean supports(DeliveryRequest r) {
        return true;
    }

    @Override
    public double calculateCost(DeliveryRequest r) {
        return 0;
    }
}
