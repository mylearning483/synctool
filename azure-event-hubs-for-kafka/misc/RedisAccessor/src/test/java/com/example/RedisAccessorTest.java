package com.example.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.data.redis.RedisTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.redis.fetcher.MonotonicFetcher;

@DataRedisTest(excludeAutoConfiguration = RedisTestConfiguration.class)
@SpringBootTest(classes = { ApplicationConfig.class, RedisAccessor.class })
public class RedisAccessorTests {

    private static final String KEY = "test-key";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisAccessor redisAccessor;

    private Object testValue;

    @BeforeEach
    void setup() {
        // Set up the test value
        MonotonicFetcher fetcher = new MonotonicFetcher();
        testValue = redisAccessor.get(KEY, fetcher, Duration.ofSeconds(5));
    }

    @Test
    void testGetWithValidKey() {
        // Ensure that the cached value is returned
        Object cachedValue = redisAccessor.get(KEY, null, null);
        assertThat(cachedValue).isEqualTo(testValue);
    }

    @Test
    void testGetWithInvalidKey() {
        // Ensure that null is returned for an invalid key
        Object cachedValue = redisAccessor.get("invalid-key", null, null);
        assertThat(cachedValue).isNull();
    }

    @Test
    void testGetWithExpiredValue() throws InterruptedException {
        // Ensure that a new value is fetched after the previous value expires
        MonotonicFetcher fetcher = new MonotonicFetcher();
        redisAccessor.get(KEY, fetcher, Duration.ofSeconds(2));

        // Wait for the previous value to expire
        Thread.sleep(3000);

        // Get the value again
        Object cachedValue = redisAccessor.get(KEY, null, null);
        assertThat(cachedValue).isEqualTo(fetcher.fetch());
    }
}
