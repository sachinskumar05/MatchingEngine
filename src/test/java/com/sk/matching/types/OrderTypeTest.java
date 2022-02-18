package com.sk.matching.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OrderTypeTest {
    //Field MARKET of type OrderType - was not mocked since Mockito doesn't mock enums
    //Field LIMIT of type OrderType - was not mocked since Mockito doesn't mock enums

    @Test
    void testValues() {
        OrderType []expected = new OrderType[] {OrderType.MARKET, OrderType.LIMIT};
        OrderType[] result = OrderType.values();
        Assertions.assertArrayEquals(expected, result);
    }

    @Test
    void testValueOf() {
        OrderType result = OrderType.valueOf("MARKET");
        Assertions.assertEquals(OrderType.MARKET, result);
    }

    @Test
    void testGetFixValue() {
        int expected = 2;
        Assertions.assertEquals(expected, OrderType.LIMIT.getFixValue());
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme