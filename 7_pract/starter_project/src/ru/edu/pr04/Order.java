package ru.edu.pr04;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private final Employee employee;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.DRAFT;

    public Order(Employee employee) {
        this.employee = Objects.requireNonNull(employee, "employee is null");
    }

    public void addProduct(Product product, int quantity) {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("order is confirmed");
        }
        
        // R7: Поиск существующей позиции по ID товара
        for (OrderItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                // Объединяем количество вместо создания новой позиции
                item.increase(quantity);
                return;
            }
        }
        // Если товара нет в заказе — добавляем новую позицию
        items.add(new OrderItem(product, quantity));
    }

    public double total() {
        // R9: Пересчитываем сумму при каждом вызове, поле total не храним
        double sum = 0.0;
        for (OrderItem item : items) {
            sum += item.getLineTotal();
        }
        return sum;
    }

    public void removeProduct(String productId) {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("order is confirmed");
        }
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void confirm() {
        if (items.isEmpty()) {
            throw new IllegalStateException("order is empty");
        }
        status = OrderStatus.CONFIRMED;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Employee getEmployee() {
        return employee;
    }
}