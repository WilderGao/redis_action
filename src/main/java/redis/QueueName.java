package redis;

/**
 * @author WilderGao
 * time 2019-05-02 20:08
 * motto : everything is no in vain
 * description
 */
public enum QueueName {
    /**
     * 成员队列
     */
    PERSON_QUEUE("queue:person"),
    /**
     * 商品队列
     */
    GOODS_QUEUE("queue:goods"),
    /**
     * 延时队列
     */
    DELAY_QUEUE("queue:delay"),
    /**
     * 消息队列
     */
    MESSAGE_QUEUE("queue:message"),
    /**
     * 获得锁的队列名
     */
    LOCK_QUEUE("queue:lock"),
    /**
     * 聊天队列名
     */
    CHAT_QUEUE("chat:"),
    /**
     * 聊天信息队列名
     */
    CHAT_MESSAGE("message:"),

    /**
     * 用户队列名
     */
    USER("user:"),
    /**
     * 生成用户 Id 队列名
     */
    USER_ID("user:id:"),
    /**
     * 发布的动态 Id 队列名
     */
    STATUS_ID("status:id:"),
    /**
     * 用户状态队列名
     */
    STATUS("status:"),
    /**
     * 个人主页队列名
     */
    HOME("home:"),
    /**
     * 关注者队列名
     */
    FOLLOWERS("followers:"),
    /**
     * 正在关注队列名
     */
    FOLLOWINGS("followings:"),
    /**
     * 关注的人的朋友圈队列
     */
    PROFILE("profile:");

    private String queueName;

    QueueName(String queueName){
        this.queueName = queueName;
    }

    public String getQueueName(){
        return queueName;
    }
}
