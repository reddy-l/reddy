package com.qh.redis.service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.qh.common.utils.ParamUtil;
import com.qh.redis.RedisConstants;
import com.qh.redis.constenum.ConfigParent;
import com.qh.system.domain.ConfigDO;

/**
 * @author chyzh
 * @ClassName: RedisUtil
 * @Description: redis用到的常用操作
 * @date 2017年10月27日 上午10:26:01
 */
public class RedisUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private static RedisTemplate<String, Object> redisTemplate;
    private static RedisTemplate<String, Object> redisNotifyTemplate;

    public static void setValue(String key, Object obj) {
        redisTemplate.opsForValue().set(key, obj);
    }

    public static Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

	public static Object getHashValue(String key,Object obj){
		return redisTemplate.opsForHash().get(key,obj);
	}

	public static List<Object> getHashValueList(String key){

    	return redisTemplate.opsForHash().values(key);
	}

	public static int getHashValueCount(String key){
    	return redisTemplate.opsForHash().size(key).intValue();
	}

	public static List<Object> getHashValueListForStringObjBlur(String key,String patten){
    	Set<Object> keySet = redisTemplate.opsForHash().keys(key);

    	List<Object> keyList = new ArrayList<>();
    	for(Object obj:keySet){
    		String tmpKey = obj.toString();
    		if(tmpKey.matches(".*"+patten+".*")){
				keyList.add(tmpKey);
			}
		}
    	return redisTemplate.opsForHash().multiGet(key,keyList);
	}

    /**
     * @Description 设置聚富支付网管最近连接时间
     */
    public static void setQrGatewayLastSyncTime(String merchNo, String outChannel, Object obj) {
        redisTemplate.opsForHash().put(RedisConstants.cache_qr_last_login_time, merchNo + RedisConstants.link_symbol + outChannel, obj);
    }

    /**
     * @Description 获取聚富支付网管最近连接时间
     */
    public static Object getQrGatewayLastSyncTime(String merchNo, String outChannel) {
        return redisTemplate.opsForHash().get(RedisConstants.cache_qr_last_login_time, merchNo + RedisConstants.link_symbol + outChannel);
    }
    
    /**
     * @param order
     * @Description 支付订单
     *//*
    public static void setOrder(Order order) {
        redisTemplate.opsForHash().put(RedisConstants.cache_order + order.getMerchNo(), order.getOrderNo(), order);
        redisTemplate.opsForZSet().add(RedisConstants.cache_sort_order + order.getMerchNo(), order.getOrderNo(), order.getCrtDate());
    }

    *//**
     * @param merchNo
     * @param orderNo
     * @return
     * @Description 获取支付订单
     *//*
    public static Order getOrder(String merchNo, String orderNo) {
        return (Order) redisTemplate.opsForHash().get(RedisConstants.cache_order + merchNo, orderNo);
    }*/
    /**
     * @Description 删除支付订单
     */
    public static void removeOrder(String merchNo, String orderNo) {
        redisTemplate.opsForHash().delete(RedisConstants.cache_order + merchNo, orderNo);
        redisTemplate.opsForZSet().remove(RedisConstants.cache_sort_order + merchNo, orderNo);
    }

	
	/**
	 * @Description 删除代付订单
	 * @param msgKey
	 */
	public static void removeOrderAcp(String merchNo, String orderNo) {
		redisTemplate.opsForHash().delete(RedisConstants.cache_order_acp + merchNo, orderNo);
		redisTemplate.opsForZSet().remove(RedisConstants.cache_sort_acp_order + merchNo, orderNo);
	}

	/**
	 * @param orderKey 
	 * @param merchNo 
	 * @Description 
	 * @param orderNo
	 * @return
	 */
	public static boolean setKeyEventExpired(String orderKey, String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getEventLock(orderKey + RedisConstants.link_symbol + merchNo + RedisConstants.link_symbol +  orderNo);
		if (lock.tryLock()) {
			try {
				Integer minute = (Integer) redisNotifyTemplate.opsForHash().get(orderKey + merchNo, orderNo);
				logger.info("新的订单过期时间：{}，{}，{}", merchNo,orderNo,minute);
				if(minute == null){
					minute = RedisConstants.keyevent_10;
				}else if(minute == 0){
					redisNotifyTemplate.opsForHash().delete(orderKey + merchNo, orderNo);
					return false;
				}
				Long timeLive = redisNotifyTemplate.opsForValue().getOperations().getExpire(orderKey + merchNo + RedisConstants.link_symbol +  orderNo);
				if(timeLive == null || timeLive < 30){
					redisNotifyTemplate.opsForHash().put(orderKey + merchNo, orderNo, RedisConstants.evtMinuteMap.get(minute));
					redisNotifyTemplate.opsForValue().set(orderKey + merchNo + RedisConstants.link_symbol +  orderNo, minute, minute, TimeUnit.MINUTES);
				}
				return true;
			} finally {
				lock.unlock();
			}
		}
		return false;
	}
	
	/**
	 * @param orderKey 
	 * @param merchNo 
	 * @Description 
	 * @param orderNo
	 * @return
	 */
	public static void delKeyEventExpired(String orderKey, String merchNo, String orderNo) {
		redisNotifyTemplate.opsForHash().delete(orderKey + merchNo, orderNo);
	}
	
	
	/**
	 * 
	 * @Description 获取银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getBanks(Integer cardType, String payCompany){
		return (List<String>) redisTemplate.opsForHash().get(RedisConstants.cache_banks + cardType, payCompany);
	}
	
	/**
	 * 
	 * @Description 设置银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @param banks
	 */
	public static void setBanks(Integer cardType, String payCompany,List<String> banks){
		redisTemplate.opsForHash().put(RedisConstants.cache_banks + cardType, payCompany,banks);
	}
	
	/**
	 * 
	 * @Description 获取银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getBanks(Integer cardType, String payCompany,String payMerch){
		return (List<String>) redisTemplate.opsForHash().get(RedisConstants.cache_banks + cardType, payCompany + RedisConstants.link_symbol + payMerch);
	}
	
	/**
	 * 
	 * @Description 设置银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @param banks
	 */
	public static void setBanks(Integer cardType, String payCompany,String payMerch,List<String> banks){
		redisTemplate.opsForHash().put(RedisConstants.cache_banks + cardType, payCompany + RedisConstants.link_symbol + payMerch,banks);
	}
	
    public static void syncConfig(ConfigDO config, boolean delateFlag) {
        if (config == null) {
            return;
        }
        if (ParamUtil.isNotEmpty(config.getParentItem())) {
            if (delateFlag) {
                redisTemplate.boundHashOps(RedisConstants.cache_config_parent + config.getParentItem()).delete(config.getConfigItem());
            } else {
                redisTemplate.boundHashOps(RedisConstants.cache_config_parent + config.getParentItem()).put(config.getConfigItem(), config.getConfigValue());
            }
        }
    }

    public static String getConfigValue(String configItem, String parentItem) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_config_parent + parentItem).get(configItem);
    }

    public static String getSysConfigValue(String configItem) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_config_parent + ConfigParent.sysConfig.name()).get(configItem);
    }

    public static void delConfig(String configItem, String parentItem) {
        if (ParamUtil.isNotEmpty(parentItem)) {
            redisTemplate.boundHashOps(RedisConstants.cache_config_parent + parentItem).delete(configItem);
        }
    }

    public static String getSMSConfigValue(String configItem) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_config_parent + ConfigParent.smsConfig.name()).get(configItem);
    }



    public static String getPayCommonValue(String key) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_payConfig).get(key);
    }


    public static Map<Object, Object> getCacheMap(String key) {
        return redisTemplate.boundHashOps(key).entries();
    }

    public static ConfigDO getCacheConfig(String key) {
        return (ConfigDO) redisTemplate.boundValueOps(RedisConstants.cache_config).
                getOperations().boundValueOps(key).get();
    }

    public static Map<String, Object> getCacheMapDesc(String key) {
        Map<Object, Object> cacheMap = getCacheMap(key);
        Map<String, Object> descMap = new HashMap<String, Object>();
        if (!cacheMap.isEmpty()) {
            ConfigDO configDO = null;
            for (Entry<Object, Object> entry : cacheMap.entrySet()) {
                configDO = getCacheConfig((String) entry.getKey());
                if (configDO != null) {
                    descMap.put((String) entry.getValue(), configDO.getConfigName());
                }
            }
            return descMap;
        } else {
            return descMap;
        }
    }

    public static void setRedisTemplate(RedisTemplate<String, Object> template) {
        redisTemplate = template;
    }

    public static RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public static RedisTemplate<String, Object> getRedisNotifyTemplate() {
        return redisNotifyTemplate;
    }

    public static void setRedisNotifyTemplate(RedisTemplate<String, Object> template) {
        redisNotifyTemplate = template;
    }


}
