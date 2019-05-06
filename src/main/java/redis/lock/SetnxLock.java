package redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Collections;

/**
 * @author WilderGao
 * time 2019-04-25 20:49
 * motto : everything is no in vain
 * description redis 实现分布式锁
 */
public class SetnxLock {
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Logger logger = LoggerFactory.getLogger(SetnxLock.class);
    private static final String LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
    private static Long lockReleaseOK = 1L;

    /**
     * 单机模式下可以使用这种分布式锁
     * @param jedisCluster  redis集群
     * @param lockKey   锁
     * @param requestId    用户标识
     * @param expireTime    过期时间设置
     * @return  是否成功加锁
     */
    public static boolean tryGetDistributedLockBySetnx(Jedis jedisCluster, String lockKey, String requestId,
                                                       int expireTime) throws IOException {
        String result = jedisCluster.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if (LOCK_SUCCESS.equals(result)){
            logger.info("["+requestId +"]成功获取锁");
            jedisCluster.close();
            return true;
        }
        logger.debug("["+requestId+"]获取锁失败");
        jedisCluster.close();
        return false;
    }

    public static boolean tryReleaseLock(Jedis cluster, String lockKey, String requestId) throws IOException {
        if (lockKey == null || requestId == null){
            return false;
        }
        Object res = cluster.eval(LUA_SCRIPT, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        cluster.close();
        return res != null && res.equals(lockReleaseOK);
    }

}
