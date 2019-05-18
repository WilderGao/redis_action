package redis.social;

import lombok.AllArgsConstructor;
import redis.QueueName;
import redis.clients.jedis.*;

import java.util.Map;
import java.util.Set;

/**
 * @author WilderGao
 * time 2019-05-10 11:53
 * motto : everything is no in vain
 * description 用redis模拟一个社交网站:关注者和正在关注功能实现
 */
@AllArgsConstructor
public class SocialFollower {
    private Jedis jedis;
    /**
     * 默认观看的朋友圈数量
     */
    private static final int PROFILE_SIZE = 100;

    /**
     * uild 关注 followId 接口
     *
     * @param uid      本人Id
     * @param followId 关注者Id
     * @return 是否关注成功
     */
    public boolean followUser(long uid, long followId) {
        String follower = QueueName.FOLLOWERS.getQueueName() + followId;
        String following = QueueName.FOLLOWINGS.getQueueName() + uid;
        //如果uid已经关注了followId，那么就直接返回
        if (null != jedis.zscore(follower, Double.toString(followId))) {
            System.out.println("已经关注过啦");
            return true;
        }
        long now = System.currentTimeMillis();
        Set<Tuple> followingStatus = jedis.zrevrangeWithScores(QueueName.PROFILE.getQueueName() + followId, 0, PROFILE_SIZE);
        Transaction transaction = jedis.multi();
        //在关注和被关注两个队列中添加信息
        transaction.zadd(following, now, Long.toString(followId));
        transaction.zadd(follower, now, Long.toString(uid));
        //修改user队列中follower和following的数量
        transaction.hincrBy(QueueName.USER.getQueueName() + uid, "following", 1);
        transaction.hincrBy(QueueName.USER.getQueueName() + followId, "followers", 1);
        //找到关注者个人主页上最前的 PROFILE_SIZE 数量朋友圈，添加到 uid 主页上，以后主页上就可以看到他的朋友圈
        if (followingStatus != null) {
            followingStatus.forEach(tuple -> transaction.zadd(QueueName.HOME.getQueueName() + uid, tuple.getScore(),
                    tuple.getElement()));
        }
        transaction.zremrangeByRank(QueueName.HOME.getQueueName() + uid, 0, -PROFILE_SIZE - 1);
        transaction.exec();
        return true;
    }


    /**
     * uid 取消关注 unFollowId
     *
     * @param uid        本人Id
     * @param unFollowId 取消关注者Id
     * @return 执行结果
     */
    public boolean unFollowUser(long uid, long unFollowId) {
        String unfollowing = QueueName.FOLLOWINGS.getQueueName() + uid;
        String unfollower = QueueName.FOLLOWERS.getQueueName() + unFollowId;
        //先看看是不是有关注 unfollowId
        if (null == jedis.zscore(unfollowing, Long.toString(unFollowId))) {
            //都没关注，后面没必要继续下去了
            System.out.println("都没关注他取关个鬼");
            return true;
        }
        Set<Tuple> followingStatus = jedis.zrevrangeWithScores(QueueName.PROFILE.getQueueName() + unFollowId, 0, PROFILE_SIZE);
        Transaction transaction = jedis.multi();
        transaction.zrem(unfollowing, Long.toString(unFollowId));
        transaction.zrem(unfollower, Long.toString(uid));
        transaction.hincrBy(QueueName.USER.getQueueName() + uid, "followings", -1);
        transaction.hincrBy(QueueName.USER.getQueueName() + unFollowId, "followers", -1);

        if (followingStatus != null) {
            followingStatus.forEach(tuple -> transaction.zrem(QueueName.HOME.getQueueName() + uid, tuple.getElement()));
        }
        transaction.exec();
        return true;
    }

    public void commonFollowing(long uid, long ouid) {
        String user = QueueName.FOLLOWINGS.getQueueName() + uid;
        String otherUser = QueueName.FOLLOWINGS.getQueueName() + ouid;
        String common = QueueName.COMMON.getQueueName() + uid + ":" + ouid;
        jedis.zinterstore(common, user, otherUser);
        Set<String> commonIds = jedis.zrange(common, 0, -1);
        commonIds.forEach(v -> {
            Map<String, String> information = jedis.hgetAll(QueueName.USER.getQueueName() + v);
            for (Map.Entry<String, String> entry : information.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        });
    }

}
