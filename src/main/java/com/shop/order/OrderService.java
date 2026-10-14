package com.shop.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Business rule from the article: a new customer gets 10% off the first order. */
public class OrderService {

    private static final BigDecimal FIRST_ORDER_FACTOR = new BigDecimal("0.90");

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public Order place(String customer, String amount) {
        BigDecimal total = new BigDecimal(amount);
        if (!orders.hasOrders(customer)) {
            total = total.multiply(FIRST_ORDER_FACTOR);
        }
        Order order = new Order(customer, total.setScale(2, RoundingMode.HALF_UP));
        orders.save(order);
        return order;
    }
}
