import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RedisClient {

    private RedisAccessor redisAccessor;

    @Autowired
    public RedisClient(RedisAccessor redisAccessor) {
        this.redisAccessor = redisAccessor;
    }

    public Optional<Long> getValue(String key) {
        return redisAccessor.getValue(key, new MonotonicFetcher());
    }

}
