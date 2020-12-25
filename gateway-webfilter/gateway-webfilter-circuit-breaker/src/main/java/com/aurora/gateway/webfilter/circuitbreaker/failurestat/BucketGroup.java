package com.aurora.gateway.webfilter.circuitbreaker.failurestat;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class BucketGroup {

    /**
     * 桶集合
     */
    private Bucket[] buckets;

    /**
     * 桶数量
     */
    private int bucketNums;

    /**
     * 取模的桶数量
     */
    private int modBucketNums;

    /**
     * 变更时间
     */
    private volatile long changeTimestamp = 0L;

    /**
     * 游标
     */
    private AtomicInteger cursor = new AtomicInteger(0);

    /**
     * 竞争锁
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * 时间间隔
     */
    private int timeInterval;

    public BucketGroup(int bucketNums, int timeInterval) {
        Assert.isTrue(bucketNums % 2 == 0, "桶数量为" + bucketNums + "，需要是2的整数倍！");
        Assert.isTrue(bucketNums <= 64 && bucketNums >= 16, "桶数量为" + bucketNums + "，需要在 16 ~ 64 之间！");
        Assert.isTrue(timeInterval > 10 && timeInterval < 1000, "时间间隔在 10ms ~ 1000ms 之间！but timeInterval=" + timeInterval);

        this.bucketNums = bucketNums;
        this.modBucketNums = bucketNums - 1;
        this.timeInterval = timeInterval;

        buckets = new Bucket[bucketNums];
        for (int index = 0; index < bucketNums; index++) {
            buckets[index] = new Bucket();
        }
    }

    /**
     * 计算成功率
     * @return
     */
    public int calculationSuccess() {
        long success = Arrays.stream(buckets).mapToLong(Bucket::getSuccess).sum();
        long failure = Arrays.stream(buckets).mapToLong(Bucket::getFailure).sum();

        if ((success + failure) <= 0) {
            return 100;
        }

        return (int) (success * 100 / (success + failure));
    }

    /**
     * 计算失败率
     * @return
     */
    public int calculationFailure() {
        long success = Arrays.stream(buckets).mapToLong(Bucket::getSuccess).sum();
        long failure = Arrays.stream(buckets).mapToLong(Bucket::getFailure).sum();

        if ((success + failure) <= 0) {
            return 0;
        }

        return (int) (failure * 100 / (success + failure));
    }

    /**
     * 记录请求
     * @param success true：成功；false：失败
     */
    public void recordRequest(boolean success) {
        long nowTimestamp = System.currentTimeMillis() / 1000;
        int bucketPos = (int) (nowTimestamp & modBucketNums);

        if ((changeTimestamp + timeInterval) <= nowTimestamp) {
            if (lock.tryLock()) {
                try {
                    if ((changeTimestamp + timeInterval) <= nowTimestamp) {
                        //变更计时
                        changeTimestamp = nowTimestamp;

                        //得到当前重置的位置
                        int bucketIndex = cursor.accumulateAndGet(1, (left, right) -> (left + right) & modBucketNums);

                        //清空指定位置的桶计数
                        buckets[bucketIndex].reset();
                    } else {
                        //此次获取没有意义了，放弃
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                //竞争失败，放弃变更游标，直接累计即可
            }
        } else {
            //还未到需要变更的时刻
        }

        if (success) {
            buckets[bucketPos].addSuccess();
        } else {
            buckets[bucketPos].addFailure();
        }
    }
}
