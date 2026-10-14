package com.shop.payment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.AbstractIT;
import com.shop.TestCustomers;
import org.junit.jupiter.api.Test;

class RefundIT extends AbstractIT {

    @Test
    void scenario1() {
        orderService.place(TestCustomers.BOB, "100.00");

        assertTrue(orders.hasOrders(TestCustomers.BOB));
    }

    @Test
    void scenario2() {
        orderService.place(TestCustomers.BOB, "200.00");

        assertTrue(orders.hasOrders(TestCustomers.BOB));
    }
}
