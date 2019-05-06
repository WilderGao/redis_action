package redis.queue.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author WilderGao
 * time 2019-04-27 14:57
 * motto : everything is no in vain
 * description
 */
@Data
@AllArgsConstructor
public class Person {
    private String name;
    private String password;
}
