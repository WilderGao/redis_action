package redis.queue;

import com.alibaba.fastjson.JSON;
import redis.QueueName;
import redis.RedisCluster;
import redis.clients.jedis.JedisCluster;
import redis.queue.handler.PersonHandler;
import redis.queue.handler.Handler;
import redis.queue.handler.PushGoodsHandler;
import redis.queue.model.Goods;
import redis.queue.model.Person;

import java.util.List;

/**
 * @author WilderGao
 * time 2019-04-27 14:33
 * motto : everything is no in vain
 * description 优先队列实现
 */
public class PriorityQueue {
    private static final int TIMEOUT = 30;

    private void handle(JedisCluster cluster, String queueName, Handler handler){
        while (true) {
            List<String> popResult = cluster.blpop(TIMEOUT, queueName);
            if (popResult == null){
                continue;
            }
            handler.handle(popResult.get(1));
            break;
        }
    }

    public static void main(String[] args) {
        Person personWilder = new Person("wilder", "12345678");
        Person personPurple = new Person("purple", "12343545");

        Goods goodsKaoNai = new Goods(1,"益禾烤奶", 7);
        Goods goodsLongZhu = new Goods(2, "珑珠奶绿", 8);

        JedisCluster cluster = RedisCluster.getCluster();
        cluster.rpush(QueueName.PERSON_QUEUE.getQueueName(), JSON.toJSONString(personWilder));
        cluster.rpush(QueueName.PERSON_QUEUE.getQueueName(), JSON.toJSONString(personPurple));

        cluster.rpush(QueueName.GOODS_QUEUE.getQueueName(), JSON.toJSONString(goodsKaoNai));
        cluster.rpush(QueueName.GOODS_QUEUE.getQueueName(), JSON.toJSONString(goodsLongZhu));

        Handler personHandler = new PersonHandler();
        Handler goodsHandler = new PushGoodsHandler();

        PriorityQueue queue = new PriorityQueue();
        queue.handle(cluster, QueueName.PERSON_QUEUE.getQueueName(), personHandler);
        queue.handle(cluster, QueueName.GOODS_QUEUE.getQueueName(), goodsHandler);
    }
}
