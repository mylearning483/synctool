import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@SpringBootApplication
public class MyRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyRedisApplication.class, args);
    }

    // Create RedisTemplate bean with serializer
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        return template;
    }

    // Create RedisAccessor bean with RedisTemplate and lock timeout
    @Bean
    public RedisAccessor redisAccessor(RedisTemplate<String, Object> redisTemplate) {
        int lockTimeout = 5000; // 5 seconds
        return new RedisAccessor(redisTemplate, lockTimeout);
    }

}
