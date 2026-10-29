package ru.edu.pr04;

public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order createOrder(Employee e) {
        Order order = new Order(e);
        repository.save(order);
        return order;
    }
}