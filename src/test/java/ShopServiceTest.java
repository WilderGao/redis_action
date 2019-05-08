import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import redis.acid.ShopService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Set;

/**
 * @author WilderGao
 * time 2019-05-08 20:17
 * motto : everything is no in vain
 * description
 */
@Slf4j
public class ShopServiceTest {

    @Test
    public void addItemAndPurchase(){
        ShopService shopService = new ShopService();
        System.out.println(("测试ShopService类中的方法......"));
        Jedis jedis = new Jedis("10.21.56.109", 7000);
        //wilder的用户信息和包裹信息
        jedis.hset("user:1", "name", "Wilder");
        jedis.hset("user:1", "funds", "100");
        jedis.sadd("inventory:1", "《Redis实战》", "《第一本Docker书》","《Linux私房菜》");

        jedis.hset("user:2", "name", "Purple");
        jedis.hset("user:2", "funds", "200");
        jedis.sadd("inventory:2", "《初级会计》", "《经济法基础》");

        System.out.println(("wilder 准备把《redis实战》放到商城上卖"));
        shopService.listItem(jedis, "《Redis实战》", 1, 30);
        System.out.println(("purple 准备把《经济法基础》放到商城上卖"));
        shopService.listItem(jedis, "《经济法基础》", 2, 24);

        System.out.println(("成功放上去卖了，现在看看商店里面有什么......"));
        Set<Tuple> contents = jedis.zrangeWithScores("market:", 0, -1);
        contents.forEach(c -> System.out.println(c.getElement() + "    售价："+c.getScore()));

        System.out.println("=================================================================");

        System.out.println("现在wilder要在商城里面买一本《经济法基础》");
        shopService.purchaseItem(jedis, 1, "《经济法基础》", 2, 24);
        System.out.println("购买成功，现在看看wilder的包裹有哪些书");
        jedis.smembers("inventory:1").forEach(v-> System.out.println("书名："+ v));
        System.out.println("\n再看看商城现在还有什么书");
        Set<Tuple> contentsAfterPurchase = jedis.zrangeWithScores("market:", 0, -1);
        contentsAfterPurchase.forEach(c -> System.out.println(c.getElement() + "    售价："+c.getScore()));

        System.out.println("=================================================================\n测试结束");
        jedis.del("user:1", "user:2", "inventory:1", "inventory:2", "market:");
        jedis.close();
    }
}
