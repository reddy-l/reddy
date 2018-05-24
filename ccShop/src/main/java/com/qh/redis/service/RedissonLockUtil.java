package com.qh.redis.service;

import org.redisson.api.RLock;

import com.qh.redis.RedisConstants;

public class RedissonLockUtil {
	
    private static RedissonLocker redissLock;
    
    public static void setLocker(RedissonLocker locker) {
        redissLock = locker;
    }
    
    public static RLock getLock(String lockKey){
		return redissLock.getLock(lockKey);
	}
    
    public static RLock getClearLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_clear + lockKey);
	}
    
    public static RLock getOrderLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_order + lockKey);
	}
    
    public static RLock getOrderLock(String merchNo, String orderNo){
		return redissLock.getLock(RedisConstants.lock_order + merchNo + RedisConstants.link_symbol + orderNo);
	}
    
    public static RLock getChargeLock(String merchNo, String businessNo){
		return redissLock.getLock(RedisConstants.lock_charge + merchNo + RedisConstants.link_symbol + businessNo);
	}
    
    public static RLock getEventOrderLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_event_order + lockKey);
	}
    
    public static RLock getEventLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_event + lockKey);
	}
    
    public static RLock getEventOrderLock(String merchNo, String orderNo){
		return redissLock.getLock(RedisConstants.lock_event_order + merchNo + RedisConstants.link_symbol + orderNo);
	}
    
}