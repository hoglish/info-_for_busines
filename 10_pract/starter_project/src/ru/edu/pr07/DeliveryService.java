package ru.edu.pr07;

/** Сервис расчёта: фабрика + полиморфный вызов, без if-else по типам (R2, R3, R8). */
public class DeliveryService {
    private final DeliveryPolicyFactory factory;

    public DeliveryService(DeliveryPolicyFactory f) {
        factory = f;
    }

    public double calculate(DeliveryRequest r) {
        DeliveryPolicy p = factory.get(r.type());
        if (!p.supports(r)) {
            throw new IllegalArgumentException("Unsupported delivery");
        }
        return p.calculateCost(r);
    }

    /**
     * Вариант 26, часть Б: стоимость доставки типа LOCKER для параметров запроса.
     * Тип исходного запроса подменяется на LOCKER, остальные поля сохраняются.
     */
    public double addLockerCost(DeliveryRequest request) {
        DeliveryRequest locker = new DeliveryRequest(DeliveryType.LOCKER,
                request.distanceKm(), request.weightKg(), request.orderAmount());
        return calculate(locker);
    }

    /** Форма из задания: addLockerCost(DeliveryService, DeliveryRequest) -> double. */
    public static double addLockerCost(DeliveryService service, DeliveryRequest request) {
        return service.addLockerCost(request);
    }
}
