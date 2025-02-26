package shinstyle.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinstyle.userservice.dto.UserDto;
import shinstyle.userservice.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDto.SignupRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody UserDto.UpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }
}
