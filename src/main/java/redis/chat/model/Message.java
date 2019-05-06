package redis.chat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author WilderGao
 * time 2019-05-07 00:19
 * motto : everything is no in vain
 * description 消息类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private long mid;
    private long time;
    private Sender sender;
    private String message;
}
