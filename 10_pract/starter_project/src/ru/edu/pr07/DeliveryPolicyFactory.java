package ru.edu.pr07;

/** Фабрика политик (R4): только выбор реализации по типу, без формул. */
public class DeliveryPolicyFactory {
    public DeliveryPolicy get(DeliveryType type) {
        if (type == null) {
            throw new IllegalArgumentException("Delivery type must not be null");
        }
        return switch (type) {
            case STANDARD -> new StandardCourierPolicy();
            case EXPRESS -> new ExpressPolicy();
            case PICKUP -> new PickupPolicy();
            case LOCKER -> new LockerPolicy();
        };
    }
}
