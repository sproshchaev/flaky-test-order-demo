package com.shop;

import com.shop.order.OrderRepository;
import com.shop.order.OrderService;
import org.junit.jupiter.api.AfterEach;

/** Base class: cleans the orders table after each test, as in the article. */
public abstract class AbstractIT {

    protected final OrderRepository orders = SharedContext.ORDERS;
    protected final OrderService orderService = SharedContext.ORDER_SERVICE;

    @AfterEach
    void cleanDatabase() {
        orders.truncate();
    }
}
