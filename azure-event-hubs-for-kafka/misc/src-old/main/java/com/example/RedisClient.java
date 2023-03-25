import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisClient {

    private final RedisAccessor redisAccessor;

    @Autowired
    public RedisClient(RedisAccessor redisAccessor) {
        this.redisAccessor = redisAccessor;
    }

    public String getCachedValue(String key, long cacheExpiration) {
        Fetcher<String> fetcher = new MonotonicFetcher();
        return redisAccessor.getCachedValue(key, cache
