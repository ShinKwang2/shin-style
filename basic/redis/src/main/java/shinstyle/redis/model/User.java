package shinstyle.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
