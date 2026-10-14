package com.shop.order;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shop.AbstractIT;
import com.shop.TestCustomers;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FirstOrderDiscountIT extends AbstractIT {

    @Test
    void newCustomerGetsTenPercentOff() {
        var order = orderService.place(TestCustomers.ALICE, "1000.00");

        assertEquals(new BigDecimal("900.00"), order.total());
    }
}
