package redis.queue.handler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import redis.queue.model.Goods;

/**
 * @author WilderGao
 * time 2019-04-27 14:48
 * motto : everything is no in vain
 * description
 */
@Slf4j
public class PushGoodsHandler extends Handler {

    @Override
    public void handle(String objString) {
        if (objString == null) {
            System.out.println("字符串为空，Json无法转为对象...");
        }
        Goods goods = JSON.parseObject(objString, Goods.class);
        if (goods != null) {
            System.out.println("[取出对象 goods]-->" + goods.toString());
        }else {
            System.out.println("对象解析失败");
        }
    }
}
