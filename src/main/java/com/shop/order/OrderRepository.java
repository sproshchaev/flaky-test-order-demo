package com.shop.order;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory table "orders". */
public class OrderRepository {

    private final List<Order> rows = new CopyOnWriteArrayList<>();

    public void save(Order order) {
        rows.add(order);
    }

    public boolean hasOrders(String customer) {
        return rows.stream().anyMatch(o -> o.customer().equals(customer));
    }

    public int count() {
        return rows.size();
    }

    public void truncate() {
        rows.clear();
    }
}
