package shinstyle.userservice.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shinstyle.userservice.dto.UserDto;
import shinstyle.userservice.service.JWTService;
import shinstyle.userservice.service.UserService;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class AuthController {

    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto.LoginRequest request) {
        UserDto.Response userResponse = userService.authenticate(request);
        String token = jwtService.generateToken(userResponse);
        return ResponseEntity.ok(UserDto.LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .build()
        );
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody UserDto.TokenRequest request) {
        Claims claims = jwtService.validateToken(request.getToken());

        return ResponseEntity.ok(UserDto.TokenResponse.builder()
                .email(claims.getSubject())
                .valid(true)
                .role(claims.get("role", String.class))
                .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody UserDto.TokenRequest tokenRequest) {
        String newToken = jwtService.refreshToken(tokenRequest.getToken());
        return ResponseEntity.ok(Collections.singletonMap("token", newToken));
    }
}
