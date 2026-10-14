package com.shop;

import com.shop.order.OrderRepository;
import com.shop.order.OrderService;

/**
 * Stand-in for the Spring TestContext cache.
 *
 * Spring keeps one application context (and one in-memory H2) in a static
 * cache for all test classes with the same configuration. A static field
 * gives the same effect without Spring: every test class in the JVM fork
 * sees the same "database".
 */
public final class SharedContext {

    public static final OrderRepository ORDERS = new OrderRepository();
    public static final OrderService ORDER_SERVICE = new OrderService(ORDERS);

    private SharedContext() {
    }
}
