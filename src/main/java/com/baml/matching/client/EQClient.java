package com.baml.matching.client;

import com.baml.matching.engine.EquityMatchingEngine;

public interface EQClient extends Client {
    EquityMatchingEngine EQUITY_MATCHING_ENGINE = EquityMatchingEngine.getInstance();

}
