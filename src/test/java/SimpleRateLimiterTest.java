import org.junit.Test;
import redis.action.SimpleRateLimiter;
import redis.clients.jedis.Jedis;

import java.io.IOException;

/**
 * @author WilderGao
 * time 2019-05-16 22:11
 * motto : everything is no in vain
 * description
 */
public class SimpleRateLimiterTest {

    @Test
    public void limitTest() throws IOException {
        System.out.println("================== 测试简单限流功能 ================");
        Jedis jedis = new Jedis("10.21.56.109", 7000);
        SimpleRateLimiter limiter = new SimpleRateLimiter(jedis);
        System.out.println("现在请求同一个功能20次，限流是60s内只能请求5次");
        for (int i = 0; i < 20; i++) {
            if (limiter.isActionAllowed("wilder", "reply", 60, 10)) {
                System.out.println("第" + i + "次请求成功");
            }else {
                System.out.println("第" + i + "次请求失败");
            }
        }
        System.out.println("=============== 测试结束 ===============");
    }
}
