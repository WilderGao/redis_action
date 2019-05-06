package redis.queue;

import com.alibaba.fastjson.JSON;
import redis.QueueName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.lock.SetnxLock;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


/**
 * @author WilderGao
 * time 2019-05-02 20:21
 * motto : everything is no in vain
 * description Redis 在延时队列的应用
 */
public class DelayQueue<T> {
    private static final Long RM_SUCCESS = 1L;
    /**
     * 将任务加入延时队列
     *
     * @param cluster   redis 集群
     * @param item      加入的内容
     * @param delay     如果要延迟，延迟时间
     */
    public void executeDelay(Jedis cluster, T item, long delay) {
        String uuid = UUID.randomUUID().toString();
        String itemToRedis = JSON.toJSONString(item) + ":" + uuid;
        if (delay > 0) {
            //证明需要延时
            cluster.zadd(QueueName.DELAY_QUEUE.getQueueName(), System.currentTimeMillis() + delay, itemToRedis);
        } else {
            cluster.rpush(QueueName.MESSAGE_QUEUE.getQueueName(), itemToRedis);
        }
    }

    /**
     * 如果延时队列中的任务到达时间了则将它加入任务队列
     * @param cluster redis 集群
     */
    public void pollQueue(Jedis cluster) throws InterruptedException, IOException {
        while (true) {
            //按照score进行排序,Tuple中包含了对应的执行时间
            Set<Tuple> scoreSet = cluster.zrangeWithScores(QueueName.DELAY_QUEUE.getQueueName(), 0, -1);
            Set<String> itemSet = cluster.zrange(QueueName.DELAY_QUEUE.getQueueName(), 0, -1);
            //判断是否为空或者第一个是否已经到达时间了
            if (scoreSet == null || scoreSet.iterator().next().getScore() > System.currentTimeMillis()) {
                Thread.sleep(1);
                continue;
            }
            //现在我们拿到了第一个加进去的字符串，需要进行还原
            String item = itemSet.iterator().next();
            String uuid = item.substring(item.lastIndexOf(":"));
            //对变量取出需要获得对应的锁防止多条线程同时操作对象出现异常
            boolean locked = SetnxLock.tryGetDistributedLockBySetnx(cluster, QueueName.LOCK_QUEUE.getQueueName(), uuid, 30);
            if (!locked){
                continue;
            }
            if (RM_SUCCESS.equals(cluster.zrem(QueueName.DELAY_QUEUE.getQueueName(), item))){
                //将内容从延迟队列中删除并加入到消息队列中
                cluster.rpush(QueueName.MESSAGE_QUEUE.getQueueName(), item);
            }
            SetnxLock.tryReleaseLock(cluster, QueueName.LOCK_QUEUE.getQueueName(), uuid);
            break;
        }
    }
}
