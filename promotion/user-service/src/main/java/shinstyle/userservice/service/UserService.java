package shinstyle.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinstyle.common.snowflake.Snowflake;
import shinstyle.userservice.dto.UserDto;
import shinstyle.userservice.entity.User;
import shinstyle.userservice.entity.UserLoginHistory;
import shinstyle.userservice.exception.DuplicateUserException;
import shinstyle.userservice.exception.UnauthorizedAccessException;
import shinstyle.userservice.exception.UserNotFoundException;
import shinstyle.userservice.repository.UserLoginHistoryRepository;
import shinstyle.userservice.repository.UserRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final Snowflake snowflake = new Snowflake();
    private final UserRepository userRepository;
    private final UserLoginHistoryRepository userLoginHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto.Response createUser(UserDto.SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateUserException("User already exists with email: " + request.getEmail());
        }

        User savedUser = userRepository.save(
                User.create(
                        snowflake.nextId(),
                        request.getName(),
                        request.getEmail(),
                        passwordEncoder.encode(request.getPassword()))
        );

        return UserDto.Response.from(savedUser);
    }

    public UserDto.Response authenticate(UserDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedAccessException("Invalid credentials");
        }
        return UserDto.Response.from(user);
    }

    public UserDto.Response getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return UserDto.Response.from(user);
    }

    @Transactional
    public UserDto.Response updateUser(Long userId, UserDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.changeName(request.getName());
        return UserDto.Response.from(
                userRepository.save(user)
        );
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedAccessException("Invalid credentials");
        }

        user.changePasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<UserLoginHistory> getUserLoginHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return userLoginHistoryRepository.findByUserOrderByLoginTimeDesc(user);
    }


}
