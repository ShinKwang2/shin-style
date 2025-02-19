package shinstyle.redis.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LeaderboardServiceTest {

    @Autowired
    private LeaderboardService leaderboardService;

    @Test
    void leaderboardOperationsTest() {
        // Given
        String userId = "user2";
        double score = 100.0;

        // When
        leaderboardService.addScore(userId, score);
        List<String> topPlayers = leaderboardService.getTopPlayers(1);
        Long rank = leaderboardService.getUserRank(userId);

        // Then
        assertThat(topPlayers).isNotEmpty();
        assertThat(userId).isEqualTo(topPlayers.get(0));
        assertThat(rank).isEqualTo(0L); // 0-based index

    }
}