package shinstyle.redis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import shinstyle.redis.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Test
    void cacheDataTest() {
        // Given
        User user = new User(1L, "testUser", "test@test.com", LocalDateTime.now());

        // When
        cacheService.cacheData("user:1", user, 60);
        Optional<User> cachedUser = cacheService.getCacheData("user:1", User.class);

        // Then
        assertThat(cachedUser).isPresent();
        assertThat(user.getUsername()).isEqualTo(cachedUser.get().getUsername());
    }
}