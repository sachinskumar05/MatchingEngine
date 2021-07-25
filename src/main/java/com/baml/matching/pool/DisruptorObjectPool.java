package com.baml.matching.pool;

import com.baml.matching.config.PoolCfg;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;

import java.util.concurrent.BlockingQueue;

public class DisruptorObjectPool<T> extends ObjectPool<T> {

    public DisruptorObjectPool(PoolCfg poolCfg, ObjectFactory<T> objectFactory) {
        super(poolCfg, objectFactory);
    }

    @Override
    protected BlockingQueue<Poolable<T>> createBlockingQueue(PoolCfg config) {
        return new DisruptorBlockingQueue<>(config.getMaxSize());
    }

}
