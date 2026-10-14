package com.shop.loyalty;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shop.SharedContext;
import com.shop.TestCustomers;
import org.junit.jupiter.api.Test;

/**
 * The leaking neighbour. It does not extend AbstractIT, so nothing cleans
 * the order it places.
 *
 * In the Spring version of the article this class is marked @Transactional
 * and runs with RANDOM_PORT. The author expects a rollback, but the request
 * is served by another thread in its own transaction, so the server-side
 * insert is committed and stays in the shared H2. Here the same outcome is
 * modelled directly: the order is saved and left behind.
 *
 * Fix: extend AbstractIT (or clean up in its own @AfterEach).
 */
class LoyaltyPointsIT {

    @Test
    void pointsAreCreditedAfterOrder() {
        SharedContext.ORDER_SERVICE.place(TestCustomers.ALICE, "1000.00");

        assertTrue(SharedContext.ORDERS.hasOrders(TestCustomers.ALICE));
    }
}
