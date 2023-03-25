import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import java.util.Objects;
import java.util.Optional;

@Component
public class RedisAccessor {

    private final Jedis jedis;

    public RedisAccessor() {
        this.jedis = new Jedis("localhost", 6379);
    }

    public Optional<String> getCachedValue(String key, long cacheExpiration, Fetcher<String> fetcher) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(fetcher, "Fetcher cannot be null");

        String value = null;

        try {
            // Check if the value is in the cache
            value = jedis.get(key);
        } catch (JedisException e) {
            // Handle Jedis exception
        }

        if (value != null) {
            // Value is in the cache, return it
            return Optional.of(value);
        } else {
            // Value is not in the cache, acquire the lock to refresh it
            String lockKey = key + "_lock";
            String clientId = Thread.currentThread().getName();
            String result = jedis.set(lockKey, clientId, "NX", "EX", 10);

            if (result != null && result.equals("OK")) {
                // Lock acquired, fetch the value and set it in the cache
                try {
                    value = fetcher.fetch();
                    jedis.setex(key, (int) cacheExpiration, value);
                } catch (Exception e) {
                    // Handle fetcher exception
                } finally {
                    // Release the lock
                    jedis.del(lockKey);
                }

                return Optional.ofNullable(value);
            } else {
                // Lock not acquired, wait for a random interval before retrying
                try {
                    Thread.sleep((long) (Math.random() * 100));
                } catch (InterruptedException e) {
                    // Handle interrupted exception
                }

                // Retry to get the value from the cache
                return getCachedValue(key, cacheExpiration, fetcher);
            }
        }
    }
}
