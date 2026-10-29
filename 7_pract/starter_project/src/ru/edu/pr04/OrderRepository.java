package ru.edu.pr04;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    public void save(Order order) {
        orders.add(order);
    }

    public List<Order> findAll() {
        return Collections.unmodifiableList(orders);
    }
}