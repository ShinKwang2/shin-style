package shinstyle.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void cacheData(String key, Object data, long timeoutSeconds) {
        try {
            redisTemplate.opsForValue().set(key, data, timeoutSeconds, TimeUnit.SECONDS);
            log.info("Data cached successfully for key: {}", key);
        } catch (Exception e) {
            log.error("Error caching data: {}", e.getMessage());
            throw new RuntimeException("Cache operation failed", e);
        }
    }

    public <T> Optional<T> getCacheData(String key, Class<T> type) {
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.convertValue(data, type));
        } catch (Exception e) {
            log.error("Error retrieving cached data: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void deleteCachedData(String key) {
        try {
            redisTemplate.delete(key);
            log.info("Cache deleted successfully for key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting cached data: {}", e.getMessage());
            throw new RuntimeException("Cache deletion failed", e);
        }
    }
}
