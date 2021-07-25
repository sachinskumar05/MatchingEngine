package com.baml.matching.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("pool-cfg")
public class PoolConfig {

    private int maxWaitMilliseconds; // when pool is full, wait at most given seconds, then throw an exception
    private int maxIdleMilliseconds; // objects idle for 5 minutes will be destroyed to shrink the pool size
    private int minSize;
    private int maxSize;
    private int partitionSize;
    private int scavengeIntervalMilliseconds;
    private double scavengeRatio; // avoid cleaning up all connections in the pool at the same time
    private int shutdownWaitMilliseconds;

    public int getMaxWaitMilliseconds() {
        return maxWaitMilliseconds;
    }

    /**
     * this is only used for blocking call to <code>borrowObject(true)</code>.
     * @param maxWaitMilliseconds how long to block
     * @return the pool config
     */
    public void setMaxWaitMilliseconds(int maxWaitMilliseconds) {
        if (maxWaitMilliseconds <= 0) {
            throw new IllegalArgumentException("Cannot set max wait time to a negative number " + maxWaitMilliseconds);
        }
        this.maxWaitMilliseconds = maxWaitMilliseconds;
    }

    /**
     * @param scavengeIntervalMilliseconds set it to zero if you don't want to automatically shrink your pool.
     *                                     This is useful for fixed-size pool, or pools don't increase too much.
     * @return the pool config
     */
    public void setScavengeIntervalMilliseconds(int scavengeIntervalMilliseconds) {
        if (scavengeIntervalMilliseconds < 5000) {
            throw new IllegalArgumentException("Cannot set interval too short (" + scavengeIntervalMilliseconds +
                    "), must be at least 5 seconds");
        }
        this.scavengeIntervalMilliseconds = scavengeIntervalMilliseconds;
    }

    public double getScavengeRatio() {
        return scavengeRatio;
    }

    /**
     *  Each time we shrink a pool, we only scavenge some of the objects to avoid an empty pool
     * @param scavengeRatio must be a double between (0, 1]
     * @return the pool config
     */
    public void setScavengeRatio(double scavengeRatio) {
        if (scavengeRatio <= 0 || scavengeRatio > 1) {
            throw new IllegalArgumentException("Invalid scavenge ratio: " + scavengeRatio);
        }
        this.scavengeRatio = scavengeRatio;
    }

    public int getShutdownWaitMilliseconds() {
        return shutdownWaitMilliseconds;
    }

    /**
     * If any borrowed objects are leaked and cannot be returned, the pool will be shut down after
     * <code>partitions * shutdownWaitMilliseconds</code> milliseconds.
     * If any borrowed objects are in use and cannot be returned to the pool timely
     * within <code>partitions * shutdownWaitMilliseconds</code> milliseconds,
     * the pool will be shut down and the objects in use will not be returned.
     * @param shutdownWaitMilliseconds default to 30 seconds for each partition
     */
    public void setShutdownWaitMilliseconds(int shutdownWaitMilliseconds) {
        this.shutdownWaitMilliseconds = shutdownWaitMilliseconds;
    }
}