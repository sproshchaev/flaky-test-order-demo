package com.shop.order;

import java.math.BigDecimal;

public record Order(String customer, BigDecimal total) {
}
