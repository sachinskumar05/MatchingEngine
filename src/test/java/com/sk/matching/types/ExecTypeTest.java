package com.sk.matching.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.sk.matching.types.ExecType.*;

class ExecTypeTest {
    //Field NEW of type ExecType - was not mocked since Mockito doesn't mock enums
    //Field DFD of type ExecType - was not mocked since Mockito doesn't mock enums
    //Field CANCEL of type ExecType - was not mocked since Mockito doesn't mock enums
    //Field REPLACE of type ExecType - was not mocked since Mockito doesn't mock enums
    //Field PENDING_CAN of type ExecType - was not mocked since Mockito doesn't mock enums
    //Field REJECTED of type ExecType - was not mocked since Mockito doesn't mock enums
    //Field FILL of type ExecType - was not mocked since Mockito doesn't mock enums

    @Test
    void testValues() {
        ExecType []expected = new ExecType[]{NEW, DFD, CANCEL, REPLACE, PENDING_CAN, REJECTED, FILL};
        ExecType[] result = ExecType.values();
        Assertions.assertArrayEquals(expected, result);
    }

    @Test
    void testValueOf() {
        ExecType result = ExecType.valueOf("NEW");
        Assertions.assertEquals(ExecType.NEW, result);
    }

    @Test
    void testGetFixValue() {
        String expected = "0";
        Assertions.assertEquals(expected, NEW.getFixValue());
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme