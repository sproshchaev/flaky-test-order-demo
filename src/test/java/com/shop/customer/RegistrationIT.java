package com.shop.customer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.AbstractIT;
import com.shop.TestCustomers;
import org.junit.jupiter.api.Test;

class RegistrationIT extends AbstractIT {

    @Test
    void scenario1() {
        orderService.place(TestCustomers.BOB, "100.00");

        assertTrue(orders.hasOrders(TestCustomers.BOB));
    }
}
