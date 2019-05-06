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
    CHAT_MESSAGE("message:");

    private String queueName;

    QueueName(String queueName){
        this.queueName = queueName;
    }

    public String getQueueName(){
        return queueName;
    }
}
