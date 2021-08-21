package com.sk.matching.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
class AppConstantsTest {

    @Test
    void testUSD(){
        Assertions.assertEquals( "USD", AppConstants.USD);
    }
}

