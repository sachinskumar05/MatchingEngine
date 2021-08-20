package com.sk.matching.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SideTest {
    //Field BUY of type Side - was not mocked since Mockito doesn't mock enums
    //Field SELL of type Side - was not mocked since Mockito doesn't mock enums

    @Test
    void testValueOf() {
        Side result = Side.valueOf('B');
        Assertions.assertEquals(Side.BUY, result);
    }

    @Test
    void testValueOf2() {
        Side result = Side.valueOf(1);
        Assertions.assertEquals(Side.BUY, result);
    }

    @Test
    void testValues() {
        Side []expected = new Side[]{Side.BUY, Side.SELL};
        Side[] result = Side.values();
        Assertions.assertArrayEquals(expected, result);
    }

    @Test
    void testValueOf3() {
        Side result = Side.valueOf("BUY");
        Assertions.assertEquals(Side.BUY, result);
    }
    @Test
    void testGetFixVaue(){
        Assertions.assertEquals(2, Side.SELL.getFixSide());
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme