import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

public class RedisAccessor {

    private RedisTemplate<String, Object> redisTemplate;
    private int lockTimeout;

    public RedisAccessor(RedisTemplate<String, Object> redisTemplate, int lockTimeout) {
        this.redisTemplate = redisTemplate;
        this.lockTimeout = lockTimeout;
    }

    public <T> Optional<T> getValue(String key, Fetcher<T> fetcher) {
        T value = (T) redisTemplate.opsForValue().get(key);
        if (value == null) {
            String lockKey = key + ":lock";
            if (redisTemplate.opsForValue().setIfAbsent(lockKey, true)) {
                try {
                    redisTemplate.expire(lockKey, lockTimeout);
                    value = fetcher.fetch();
                    redisTemplate.opsForValue().set(key, value);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                try {
                    Thread.sleep(50); // Pause for 50 milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getValue(key, fetcher); // Retry
            }
        }
        return Optional.ofNullable(value);
    }

}
