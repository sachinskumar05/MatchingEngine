package com.baml.matching.pool;

import com.baml.matching.config.PoolCfg;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingQueue;

@Log4j2
public class DisruptorObjectPool<T> extends ObjectPool<T> {

    public DisruptorObjectPool(PoolCfg poolCfg, ObjectFactory<T> objectFactory) {
        super(poolCfg, objectFactory);
    }

    @Override
    protected BlockingQueue<Poolable<T>> createBlockingQueue(PoolCfg config) {
        return new DisruptorBlockingQueue<>(config.getMaxSize());
    }

}
