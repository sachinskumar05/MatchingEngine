package com.sk.matching.exchange.order;

import com.sk.matching.config.PoolCfg;
import com.sk.matching.pool.ObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class GenOrderPool {

    @Autowired
    private PoolCfg poolCfg;


    private ObjectPool<GenOrder> pool = null;

    @PostConstruct
    public void init(){

        OrderFactory orderFactory = new OrderFactory();

        pool = new ObjectPool<>(poolCfg, orderFactory);
    }


}
