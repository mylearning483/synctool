/*
Here's how the RedisAccessor class works:

The class constructor initializes a Jedis client to connect to the Redis instance.
The getCachedValue method takes a key for the cached value, a cacheExpiration time
in seconds, and a fetcher implementation that can fetch the value when it needs to be refreshed.
The method first checks if the value is in the cache using the get method of the Jedis client.
If the value is in the cache, it is returned wrapped in an Optional.
If the value is not in the cache, the method attempts to acquire a lock 
by setting a key with a unique identifier as the value using the NX and EX options. 
If the key is successfully set, the lock is acquired and the fetcher is called to fetch the value. 
The value is then set in the cache using the setex method of the Jedis client. 
Finally, the lock is released by deleting the key using the del method of the Jedis client, 
and the fetched value is returned wrapped in an Optional.
If the key is already set (i.e., the lock is held by another thread), the method waits for a random interval
between 0 and 100 milliseconds before retrying to get the value from the cache. 
This is to prevent all threads from trying to refresh the value at the same time, which can cause cache stampede.
*/


import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import java.util.Objects;
import java.util.Optional;

public interface Fetcher<T> {
    T fetch() throws Exception;
}


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
