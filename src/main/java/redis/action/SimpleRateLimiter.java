package redis.action;

import lombok.AllArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.IOException;
import java.util.UUID;

/**
 * @author WilderGao
 * time 2019-05-11 17:48
 * motto : everything is no in vain
 * description redis应用：做一个简单的限流工具demo
 */
@AllArgsConstructor
public class SimpleRateLimiter {
    private Jedis jedis;

    /**
     * 判断在一个时间段内请求次数是否过多（如果period内请求次数超过maxAccount则会执行失败）可以使用有序集合zset来实现
     *
     * @param uid        用户id
     * @param actionKey  行为
     * @param period     时间段
     * @param maxAccount 次数
     * @return
     */
    public boolean isActionAllowed(String uid, String actionKey, int period, int maxAccount) throws IOException {
        String key = String.format("hist:%S:%S", uid, actionKey);
        long now = System.currentTimeMillis();
        //使用pipeLine一次提交，避免多次连接redis
        Pipeline pipeline = jedis.pipelined();
        pipeline.multi();
        //就每一次访问就加上一条记录，score的值是当前之间，value就随便了，能唯一标识就好
        pipeline.zadd(key, now, UUID.randomUUID().toString());
        //移除 (now ~ now-period)这段时间之外的键值对，因为是限制在period时间段内的请求次数，不把之前的删除会影响计数
        pipeline.zremrangeByScore(key, 0, now - period * 1000);
        //获取当前集合中该键的数量
        Response<Long> count = pipeline.zcard(key);
        //设置过期时间
        pipeline.expire(key, period + 1);
        pipeline.exec();
        pipeline.close();
        return count.get() <= maxAccount;
    }
}
