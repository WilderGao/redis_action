import org.junit.Test;
import redis.QueueName;
import redis.clients.jedis.Jedis;
import redis.social.SocialFollower;
import redis.social.SocialStatus;
import redis.social.SocialUser;

import java.io.IOException;
import java.util.Map;

/**
 * @author WilderGao
 * time 2019-05-09 15:54
 * motto : everything is no in vain
 * description
 */
public class SocialTest {
    @Test
    public void userSignTest() throws IOException {
        System.out.println("============= 用户创建测试 =============");
        Jedis jedis = new Jedis("10.21.56.109", 7000);
        SocialUser socialUser = new SocialUser(jedis);
        String loginAccount = "145236984";
        String name = "nice";
        System.out.println("账号：" + loginAccount + "\n用户名：" + name);
        long uid = socialUser.createUser(loginAccount, name);
        Map<String, String> result = jedis.hgetAll(QueueName.USER.getQueueName() + uid);
        System.out.println("创建成功，看看用户信息");
        result.forEach((k, v) -> System.out.println(k + " :" + v));
    }

    @Test
    public void postStatusTest() {
        Jedis jedis = new Jedis("10.21.56.109", 7000);
        SocialStatus socialStatus = new SocialStatus(jedis);
        long statusId = socialStatus.createStatus(3, "深入理解Java虚拟机...");
        Map<String, String> statusInfo = jedis.hgetAll(QueueName.STATUS.getQueueName() + statusId);
        statusInfo.forEach((k, v) -> System.out.println(k + " :" + v));
        jedis.close();
    }

    @Test
    public void followUserTest(){
        System.out.println("wilder 关注 purple 测试.....");
        Jedis jedis = new Jedis("10.21.56.109", 7000);
        SocialFollower socialFollower = new SocialFollower(jedis);
        System.out.println(socialFollower.followUser(2,3));
        jedis.close();
    }

    @Test
    public void unFollowUserTest(){
        System.out.println("wilder 取关 purple 测试......");
        Jedis jedis = new Jedis("10.21.56.109", 7000);
        SocialFollower socialFollower = new SocialFollower(jedis);
        System.out.println(socialFollower.unFollowUser(1,3));
        jedis.close();
    }
}
