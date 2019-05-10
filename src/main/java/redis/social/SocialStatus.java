package redis.social;

import lombok.AllArgsConstructor;
import redis.QueueName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WilderGao
 * time 2019-05-09 15:16
 * motto : everything is no in vain
 * description 用redis模拟一个社交网站:动态类
 */
@AllArgsConstructor
public class SocialStatus {
    /**
     * redis 客户端
     */
    private Jedis jedis;
    /**
     * 发布新动态
     *
     * @param uid     用户Id
     * @param message 动态内容
     * @return 返回动态Id
     */
    public long createStatus(long uid, String message) {
        //获得用户的登录账号
        String loginAccount = jedis.hget(QueueName.USER.getQueueName() + uid, "login");
        if (loginAccount == null) {
            //说明账号不存在
            System.out.println("账号不存在...");
            return -1;
        }
        long statusId = jedis.incr(QueueName.STATUS_ID.getQueueName());
        //开始post动态
        Transaction transaction = jedis.multi();
        Map<String, String> info = packStatusInfo(uid, statusId, loginAccount, message);
        transaction.hmset(QueueName.STATUS.getQueueName() + statusId, info);
        //然后修改用户队列，将自己的动态数+1
        transaction.hincrBy(QueueName.USER.getQueueName() + uid, "posts", 1);

        //最后要在用户主页上和个人主页上添加动态记录
        transaction.zadd(QueueName.HOME.getQueueName() + uid, Long.parseLong(info.get("time")),
                Long.toString(statusId));
        transaction.zadd(QueueName.PROFILE.getQueueName()+uid, Long.parseLong(info.get("time")),
                Long.toString(statusId));
        transaction.exec();
        return statusId;
    }

    private Map<String, String> packStatusInfo(long uid, long statusId, String loginAccount, String message) {
        Map<String, String> info = new HashMap<>(8);
        info.put("id", Long.toString(statusId));
        info.put("uid", Long.toString(uid));
        info.put("message", message);
        info.put("time", Long.toString(System.currentTimeMillis()));
        info.put("login", loginAccount);
        return info;
    }
}
