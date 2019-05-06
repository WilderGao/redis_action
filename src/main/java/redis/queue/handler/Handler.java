package redis.queue.handler;

/**
 * @author WilderGao
 * time 2019-04-27 14:47
 * motto : everything is no in vain
 * description
 */
public abstract class Handler {

    /**
     * 处理抽象方法
     * @param objString 对象对应的json
     */
    public abstract void handle(String objString);
}
