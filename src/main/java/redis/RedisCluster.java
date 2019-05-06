package redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @author WilderGao
 * time 2019-04-25 20:45
 * motto : everything is no in vain
 * description 获得集群信息
 */
public class RedisCluster {
    private static JedisCluster jedisCluster;

    public static JedisCluster getCluster() {
        if (jedisCluster == null) {
            synchronized (JedisCluster.class) {
                if (jedisCluster == null) {
                    init();
                }
            }
        }
        return jedisCluster;
    }

    private static void init() {
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("10.21.56.109", 7000));
        nodes.add(new HostAndPort("10.21.56.109", 7001));
        nodes.add(new HostAndPort("10.21.56.109", 7002));
        nodes.add(new HostAndPort("10.21.56.116", 7003));
        jedisCluster = new JedisCluster(nodes);

    }
}
