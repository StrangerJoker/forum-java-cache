package pub.developers.forum.infrastructure.dal.redis;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pub.developers.forum.common.enums.CacheBizTypeEn;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * redis 的操作抽象出一个工具类
 */
@Component
public class RedisOperation {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 自增1
     * @param redisKey
     * @param eleKey
     */
    public void stringIncr(CacheBizTypeEn redisKey, String eleKey) {
        String key = buildKey(redisKey, eleKey);
        redisTemplate.opsForValue().increment(key);
    }

    public void stringDecr(CacheBizTypeEn redisKey, String eleKey) {
        String key = buildKey(redisKey, eleKey);
        redisTemplate.opsForValue().decrement(key);
    }


    /**
     * 设置string类型的
     * @param redisKey Redis的key
     * @param eleKey 本身的key token id 等等
     * @param eleVal key对应的val
     */
    public void stringOperaSet(CacheBizTypeEn redisKey, String eleKey, String eleVal ) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String key = buildKey(redisKey, eleKey);
        ops.set(key, eleVal);
    }

    /**
     * 设置Long类型的数据
     * @param redisKey
     * @param eleKey
     * @param eleVal
     */
    public void stringOperaSet(CacheBizTypeEn redisKey, String eleKey, Long eleVal) {
        redisTemplate.opsForValue().set(buildKey(redisKey, eleKey), eleVal);
    }

    public void stringOperaSetAndExpired(CacheBizTypeEn redisKey, String eleKey, Long eleVal, Long expired) {
        redisTemplate.opsForValue().set(buildKey(redisKey, eleKey), eleVal, expired, TimeUnit.SECONDS);
    }

    /**
     *
     * 设置string类型的
     * @param redisKey Redis的key
     * @param eleKey 本身的key
     * @param expired 过期时间
     */
    public void stringOperaSetAndExpire(CacheBizTypeEn redisKey, String eleKey, Object eleVal, Long expired) {
        redisTemplate.opsForValue().set(buildKey(redisKey, eleKey), eleVal, expired, TimeUnit.SECONDS);
    }

    /**
     * 设置 及过期时间
     * @param redisKey
     * @param eleKey
     * @param eleVal
     * @param expired
     */
    public void stringOperaSetAndExpire(CacheBizTypeEn redisKey, String eleKey, String eleVal, Long expired) {
        redisTemplate.opsForValue().set(buildKey(redisKey, eleKey), eleVal, expired, TimeUnit.SECONDS);
    }

    /**
     * 删除
     * @param redisKey
     * @param eleKey
     */
    public Boolean stringOperaDelete(CacheBizTypeEn redisKey, String eleKey) {
        return redisTemplate.delete(buildKey(redisKey, eleKey));
    }

    /**
     * 查
     * @param redisKey
     * @param eleKey
     * @return
     */
    public String stringOperaGet(CacheBizTypeEn redisKey, String eleKey) {
        Object o = objectOperaGet(redisKey, eleKey);
        return o == null ? null : String.valueOf(o);
    }

    public Integer integerOperaGet(CacheBizTypeEn redisKey, String eleKey) {
        String s = stringOperaGet(redisKey, eleKey);
        return s == null ? null : Integer.valueOf(s);
    }

    public Long longOperaGet(CacheBizTypeEn redisKey, String eleKey) {
        String s = stringOperaGet(redisKey, eleKey);
        return s == null ? null : Long.valueOf(s);
    }

    public Object objectOperaGet(CacheBizTypeEn redisKey, String eleKey) {
        return redisTemplate.opsForValue().get(buildKey(redisKey, eleKey));
    }

    /**
     * 判断是否存在
     * @param redisKey
     * @param eleKey
     * @return
     */
    public Boolean exits(CacheBizTypeEn redisKey, String eleKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(redisKey, eleKey)));
    }

    /**
     * 判断是否过期
     * @param redisKey
     * @param eleKey
     * @return
     */
    public Boolean expire(CacheBizTypeEn redisKey, String eleKey) {
        return redisTemplate.opsForValue().getOperations().getExpire(buildKey(redisKey, eleKey)).compareTo(0L) > 0;
    }

    /**
     *
     * @param redisKey
     * @param ids
     * @param refCnts
     */
    public void stringOperaBatchSet(CacheBizTypeEn redisKey, List<Long> ids, List<Long> refCnts) {
//        Map<String, String> map = new HashMap<>();
//        for (int i = 0; i < ids.size(); i++) {
//            String key = buildKey(redisKey, "" +  ids.get(i));
//            map.put(key, String.valueOf(refCnts.get(i)));
//        }
//        stringOperaBatchSet(map);
        Map<String, Long> map = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            String key = buildKey(redisKey, "" +  ids.get(i));
            map.put(key, refCnts.get(i));
        }
        stringOperaBatchSet1(map);
    }

    /**
     * 批量操作
     * @param redisKey redisKey
     * @param idsToRefCnt id-refcnt
     */
    public void stringOperaBatchSet(CacheBizTypeEn redisKey, Map<Long, Long> idsToRefCnt) {
        for (Map.Entry<Long, Long> entry : idsToRefCnt.entrySet()) {
            Long id = entry.getKey();
            Long refCnt = entry.getKey();
            stringOperaSet(redisKey, id.toString(), refCnt.toString());
        }
    }


    /**
     * 存储多条数据
     * @param map
     */
    public void stringOperaBatchSet(Map<String, String> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    public void stringOperaBatchSet1(Map<String, Long> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     * 构造新的redis可以
     * @param redisKey Redis的key
     * @param eleKey 本身的key
     * @return
     */
    private String buildKey(String redisKey, String eleKey) {
        return redisKey + ":" + eleKey;
    }

    private String buildKey(CacheBizTypeEn bizTypeEn, String eleKey) {
        return buildKey(bizTypeEn.getValue(), eleKey);
    }
}