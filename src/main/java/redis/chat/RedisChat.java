package redis.chat;

import com.alibaba.fastjson.JSON;
import redis.QueueName;
import redis.chat.model.Message;
import redis.chat.model.Sender;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.lock.SetnxLock;

import java.io.IOException;
import java.util.*;

/**
 * @author WilderGao
 * time 2019-05-06 21:57
 * motto : everything is no in vain
 * description 利用 Redis 实现群聊的功能
 */
public class RedisChat {
    private static final String IDS_CHAT = "ids:chat";

    /**
     * 创建聊天室
     *
     * @param jedis   redis客户端
     * @param senders 聊天室成员
     * @return 返回聊天室Id
     */
    public Long createChat(Jedis jedis, List<Sender> senders) {
        Long chatId = jedis.incr(IDS_CHAT);
        Map<String, Double> content = new HashMap<>(16);
        senders.forEach(v -> content.put(v.getName(), 0D));
        jedis.zadd(QueueName.CHAT_QUEUE.getQueueName() + chatId, content);
        return chatId;
    }

    /**
     * 发送消息
     *
     * @param jedis   redis客户端
     * @param chatId  聊天室Id
     * @param sender  发送者
     * @param message 消息内容
     * @return 聊天室Id
     */
    public Long sendMessage(Jedis jedis, long chatId, Sender sender, String message) throws IOException {
        String uuid = UUID.randomUUID().toString();
        boolean acquireLock = SetnxLock.tryGetDistributedLockBySetnx(jedis, QueueName.CHAT_QUEUE.getQueueName() + chatId, uuid, 10);
        try {
            if (acquireLock) {
                //成功获得锁
                Long messageId = jedis.incr("ids:" + chatId);
                Long nowTime = System.currentTimeMillis();
                //要发送的消息打包
                String packed = JSON.toJSONString(new Message(messageId, nowTime, sender, message));
                jedis.zadd(QueueName.CHAT_MESSAGE.getQueueName() + chatId, messageId, packed);
            } else {
                //没有获得锁
                chatId = -1;
            }
        } finally {
            SetnxLock.tryReleaseLock(jedis, QueueName.CHAT_QUEUE.getQueueName() + chatId, uuid);
        }
        return chatId;
    }

}
