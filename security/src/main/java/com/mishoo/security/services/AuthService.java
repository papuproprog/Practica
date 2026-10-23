package com.mishoo.security.services;

import com.mishoo.security.exceptions.InvalidCredentialsException;
import com.mishoo.security.exceptions.UserAlreadyExistsException;
import com.mishoo.security.exceptions.UserNotFoundException;
import com.mishoo.security.models.User;
import com.mishoo.security.repositories.UserRepository;
import com.mishoo.security.schemas.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    @Transactional
    public UserResponse register(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        User user = request.toEntity();
        user.setPassword(passwordEncoder.encode(request.password()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ADMIN");
        }

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return AuthResponse.of(token, user.getEmail(), user.getRole());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse change(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        user.updateFrom(userRequest);
        if (userRequest.password() != null && !userRequest.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequest.password()));
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);
    }

}
