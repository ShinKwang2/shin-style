package shinstyle.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitConfigTest {


    private WebClient webClient = WebClient.create("http://localhost:8000");

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testRateLimitWithinLimit() throws Exception {
        // 제한 속도 내에서 요청을 여러 번 보낸다.
        for (int i = 0; i < 10; i++) {
            webClient.method(HttpMethod.POST)
                    .uri("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(new LoginRequest("test3@example.com", "password123")))
                    .exchangeToMono(clientResponse -> {
                        return Mono.just(clientResponse.statusCode());
                    })
                    .doOnNext(code -> {
                        // 응답 상태 코드 확인
                        System.out.println("Status Code: " + code);
                    })
                    .subscribe();
        }
        Thread.sleep(1000);
    }

    @Test
    void testRateLimitExceedsLimit() throws Exception {
        // 제한 속도 내에서 요청을 여러 번 보낸다.
        for (int i = 0; i < 30; i++) {
            webClient.method(HttpMethod.POST)
                    .uri("/api/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(new LoginRequest("test3@example.com", "password123")))
                    .exchangeToMono(clientResponse -> {
                        return Mono.just(clientResponse.statusCode());
                    })
                    .doOnNext(code -> {
                        // 응답 상태 코드 확인
                        System.out.println("Status Code: " + code);
                    })
                    .subscribe();
        }
        Thread.sleep(1000);
    }


    @AllArgsConstructor
    @Getter
    @ToString
    static class LoginRequest {
        private String email;
        private String password;
    }
}
