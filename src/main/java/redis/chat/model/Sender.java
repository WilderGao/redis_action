package redis.chat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author WilderGao
 * time 2019-05-06 22:22
 * motto : everything is no in vain
 * description 发送者
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sender {
    /**
     * 发送者名字
     */
    private String name;
}
