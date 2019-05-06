package redis.queue.handler;

import com.alibaba.fastjson.JSON;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.queue.model.Person;

/**
 * @author WilderGao
 * time 2019-04-27 14:48
 * motto : everything is no in vain
 * description
 */
@Slf4j
public class PersonHandler extends Handler {

    @Override
    public void handle(String objString) {
        System.out.println(objString);
        Person person = JSON.parseObject(objString, Person.class);
        System.out.println("[取出对象 person]-->" + person.toString());
    }
}
