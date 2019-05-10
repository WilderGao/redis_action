package redis.social;

import lombok.AllArgsConstructor;
import redis.QueueName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.lock.SetnxLock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author WilderGao
 * time 2019-05-09 14:04
 * motto : everything is no in vain
 * description 用redis模拟一个社交网站:用户类
 */
@AllArgsConstructor
public class SocialUser {
    /**
     * redis 客户端
     */
    private Jedis jedis;

    /**
     * 创建新用户方法
     * @param loginAccount  登录账号
     * @param name  登录用户名
     * @return  返回用户的Id
     * @throws IOException 加锁异常
     */
    public long createUser(String loginAccount, String name) throws IOException {
        String uuid = UUID.randomUUID().toString();
        //尝试加锁，锁的过期时间为2s
        boolean acquireLock = SetnxLock.tryGetDistributedLockBySetnx(jedis, QueueName.USER.getQueueName() + loginAccount, uuid, 2);
        if (!acquireLock) {
            return -1;
        }
        if (jedis.sismember(QueueName.USER.getQueueName(), loginAccount)) {
            //判断该名字是不是已经被注册了
            System.out.println("该用户名已经被注册......");
            SetnxLock.tryReleaseLock(jedis, QueueName.USER.getQueueName() + loginAccount, uuid);
            return -1;
        }
        long id = jedis.incr(QueueName.USER_ID.getQueueName());
        Map<String, String> info = packUserInfo(loginAccount, id, name);
        Transaction transaction = jedis.multi();
        transaction.hmset(QueueName.USER.getQueueName()+id, info);
        //将账号保存在user队列，以后别人不能用这个名字注册
        transaction.sadd(QueueName.USER.getQueueName(), loginAccount);
        transaction.exec();
        SetnxLock.tryReleaseLock(jedis, QueueName.USER.getQueueName() + loginAccount, uuid);
        return id;
    }

    private Map<String, String> packUserInfo(String loginAccount, long id, String name) {
        Map<String, String> info = new HashMap<>(8);
        info.put("login", loginAccount);
        info.put("id", Long.toString(id));
        info.put("name", name);
        //粉丝
        info.put("followers", Integer.toString(0));
        info.put("following", Integer.toString(0));
        //初始化发布的朋友圈数量
        info.put("posts", Integer.toString(0));
        //注册时间
        info.put("signTime", Long.toString(System.currentTimeMillis()));
        return info;
    }
}
