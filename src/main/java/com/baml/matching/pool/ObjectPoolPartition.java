package com.baml.matching.pool;

import com.baml.matching.config.PoolCfg;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
public class ObjectPoolPartition<T> {

    private final ObjectPool<T> pool;
    private final PoolCfg config;
    private final int partition;
    private final BlockingQueue<Poolable<T>> objectQueue;
    private final ObjectFactory<T> objectFactory;
    private int totalCount;

    public ObjectPoolPartition(ObjectPool<T> pool, int partition, PoolCfg config,
                               ObjectFactory<T> objectFactory, BlockingQueue<Poolable<T>> queue) {
        this.pool = pool;
        this.config = config;
        this.objectFactory = objectFactory;
        this.partition = partition;
        this.objectQueue = queue;
        for (int i = 0; i < config.getMinSize(); i++) {
            objectQueue.add(new Poolable<>(objectFactory.create(), pool, partition));
        }
        totalCount = config.getMinSize();
    }

    public BlockingQueue<Poolable<T>> getObjectQueue() {
        return objectQueue;
    }

    /**
     * @param delta the number to increase
     * @return the actual number of increased objects
     */
    @SuppressWarnings({"java:S112", "java:S2142"})
    public synchronized int increaseObjects(int delta) {
        if (delta + totalCount > config.getMaxSize()) {
            delta = config.getMaxSize() - totalCount;
        }
        try {
            for (int i = 0; i < delta; i++) {
                objectQueue.put(new Poolable<>(objectFactory.create(), pool, partition));
            }
            totalCount += delta;
            log.debug("increase objects: count={}, queue size={}", totalCount , objectQueue.size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return delta;
    }

    public synchronized void decreaseObject(Poolable<T> obj) {
        objectFactory.destroy(obj.getObject());
        totalCount--;
    }

    public synchronized int getTotalCount() {
        return totalCount;
    }

    // set the scavenge interval carefully
    public synchronized void scavenge() throws InterruptedException {
        int delta = this.totalCount - config.getMinSize();
        if (delta <= 0) return;
        int removed = 0;
        long now = System.currentTimeMillis();
        Poolable<T> obj;
        while (delta-- > 0 && (obj = objectQueue.poll()) != null) {
            // performance trade off: delta always decrease even if the queue is empty,
            // so it could take several intervals to shrink the pool to the configured min value.
                log.debug("obj={} , now-last={}, , max idle={}" ,
                        obj , + (now - obj.getLastAccessTs()), config.getMaxIdleMilliseconds());
            if (now - obj.getLastAccessTs() > config.getMaxIdleMilliseconds() &&
                    ThreadLocalRandom.current().nextDouble(1) < config.getScavengeRatio()) {
                decreaseObject(obj); // shrink the pool size if the object reaches max idle time
                removed++;
            } else {
                objectQueue.put(obj); //put it back
            }
        }
        if (removed > 0 )
            log.debug( "Scavenged objects {}", removed);
    }

    public synchronized int shutdown() {
        int removed = 0;
        long startTs = System.currentTimeMillis();
        while (this.totalCount > 0 && System.currentTimeMillis() - startTs < config.getShutdownWaitMilliseconds()) {
            Poolable<T> obj = objectQueue.poll();
            if (obj != null) {
                decreaseObject(obj);
                removed++;
            }
        }
        return removed;
    }
}
