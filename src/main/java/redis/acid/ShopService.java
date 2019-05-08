package redis.acid;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * @author WilderGao
 * time 2019-05-08 13:22
 * motto : everything is no in vain
 * description Redis 在事务上的应用
 */
@Slf4j
public class ShopService {

    /** 将商品加入商店
     * @param jedis redis客户端
     * @param itemId    商品名称
     * @param sellerId  卖家Id
     * @param price 价格
     * @return  是否成功加入商店
     */
    public boolean listItem(Jedis jedis, String itemId, int sellerId, double price) {
        //拿到商品拥有者对应的队列
        String inventory = "inventory:" + sellerId;
        //构建出商品id+拥有者Id
        String item = itemId + "." + sellerId;
        try {
            jedis.watch(inventory);
            if (null == jedis.sismember(inventory, itemId + "")) {
                //如果队列中不存在这个商品Id，则释放监视器并返回
                jedis.unwatch();
                log.error("该用户不存在此商品Id ... ");
                return false;
            }
            //开始事务
            Transaction transaction = jedis.multi();
            transaction.zadd("market:", price, item);
            transaction.srem(inventory, itemId + "");
            transaction.exec();
            return true;
        }catch (Exception e){
            return false;
        }
    }


    /**
     * 购买商品方法
     * @param jedis redis客户端
     * @param buyerId 买家Id
     * @param itemId  购买内容
     * @param sellerId  卖家Id
     * @param lPrice  购买的价钱
     * @return  是否购买成功
     */
    public boolean purchaseItem(Jedis jedis, int buyerId, String itemId, int sellerId, double lPrice) {
        //买家信息
        String buyer = "user:" + buyerId;
        //卖家信息
        String seller = "user:" + sellerId;
        //商店中的商品
        String item = itemId + "."+sellerId;
        //然后要找到买家的包裹，因为买东西最后要进入自己的包裹
        String inventory = "inventory:"+buyerId;

        //对商城和买家这两个队列进行监控
        jedis.watch("market:", buyer);
        double price = jedis.zscore("market:", item);
        double funds = Double.parseDouble(jedis.hget(buyer, "funds"));
        //如果商品价格出现了变化或者买家存款比价格低，那就没办法购买，返回false
        if (price != lPrice || funds < price){
            jedis.unwatch();
            return false;
        }
        try {
            //事务开启
            Transaction transaction = jedis.multi();
            //给卖家加钱，给买家扣钱
            transaction.hincrBy(buyer, "funds", (long) -price);
            transaction.hincrBy(seller, "funds", (long) price);
            //现在买家的包裹也有这个商品的信息，要加上去
            transaction.sadd(inventory, itemId);
            //这个商品已经被买走了，所以从商城中移除
            transaction.zrem("market:", item);
            transaction.exec();
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
