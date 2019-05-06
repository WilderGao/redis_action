package redis.lock;

import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * @author WilderGao
 * time 2019-04-26 15:41
 * motto : everything is no in vain
 * description
 */
public class RedLock {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());
        executor.submit(new RedisLockRunnable("666"));
        executor.submit(new RedisLockRunnable("888"));
        executor.shutdown();
    }
}

class RedisLockRunnable implements Runnable {
    private String value;

    RedisLockRunnable(String value) {
        this.value = value;
    }

    @Override
    public void run() {
        Config config = new Config();
        config.useClusterServers()
                .setScanInterval(2000)
                .addNodeAddress("redis://10.21.56.109:7000")
                .addNodeAddress("redis://10.21.56.109:7001")
                .addNodeAddress("redis://10.21.56.114:7007");
        RedissonClient client = Redisson.create(config);

        RLock lock1 = client.getLock("lock1");
        RLock lock2 = client.getLock("lock2");
        RLock lock3 = client.getLock("lock3");

        RedissonRedLock lock = new RedissonRedLock(lock1, lock2, lock3);
        lock.lockAsync(30, TimeUnit.SECONDS );
        System.out.println("[" + Thread.currentThread().getName() + "]");
        System.out.println("当前时间：" + LocalDateTime.now());
        RBucket<String> bucket = client.getBucket("wilder");
        bucket.set(value);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "执行完成: " + LocalDateTime.now());
        lock.unlock();
        client.shutdown();
    }
}
