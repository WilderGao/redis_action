package redis.queue.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author WilderGao
 * time 2019-04-27 14:58
 * motto : everything is no in vain
 * description
 */
@Data
@AllArgsConstructor
public class Goods {
    private int id;
    private String name;
    private float price;
}
