package com.sk.matching.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sk.matching.types.MsgType.*;

class MsgTypeTest {
    //Field NOS of type MsgType - was not mocked since Mockito doesn't mock enums
    //Field EXEC_REPORT of type MsgType - was not mocked since Mockito doesn't mock enums
    //Field CANCEL_REQ of type MsgType - was not mocked since Mockito doesn't mock enums
    //Field CANCEL_REPLACE of type MsgType - was not mocked since Mockito doesn't mock enums
    //Field REJECT of type MsgType - was not mocked since Mockito doesn't mock enums
    //Field ORDER_CANCEL_REJECT of type MsgType - was not mocked since Mockito doesn't mock enums

    @Test
    void testValues() {
        MsgType []expectedResult = new MsgType[] {NOS, EXEC_REPORT, CANCEL_REQ, CANCEL_REPLACE, REJECT, ORDER_CANCEL_REJECT};
        MsgType[] result = MsgType.values();
        Assertions.assertArrayEquals(expectedResult, result);
    }

    @Test
    void testValueOf() {
        MsgType result = MsgType.valueOf("NOS");
        Assertions.assertEquals(MsgType.NOS, result);
    }

    @Test
    void testGetFixValue() {
        String expected = "D";
        Assertions.assertEquals(expected, MsgType.NOS.getFixValue());
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme