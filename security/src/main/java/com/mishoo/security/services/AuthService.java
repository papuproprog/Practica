package com.mishoo.security.services;

import com.mishoo.security.exceptions.InvalidCredentialsException;
import com.mishoo.security.exceptions.UserAlreadyExistsException;
import com.mishoo.security.exceptions.UserNotFoundException;
import com.mishoo.security.models.User;
import com.mishoo.security.multitenancy.TenantContext;
import com.mishoo.security.repositories.UserRepository;
import com.mishoo.security.schemas.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final DataSource dataSource;

    private static final List<String> KNOWN_TENANTS = List.of("centro", "norte", "sur");

    @Transactional
    public UserResponse register(UserRequest request) {
        String targetTenant = request.branch() != null && !request.branch().isBlank()
                ? TenantContext.sanitizeTenant(request.branch())
                : TenantContext.getCurrentTenant();
        TenantContext.setCurrentTenant(targetTenant);

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists in branch " + targetTenant);
        }

        User user = request.toEntity();
        user.setBranch(targetTenant);
        user.setPassword(passwordEncoder.encode(request.password()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ADMIN");
        }

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    public AuthResponse login(AuthRequest request) {
        String originalTenant = TenantContext.getCurrentTenant();
        List<String> tenantsToTry = new ArrayList<>();

        if (request.email() != null) {
            String lower = request.email().toLowerCase();
            for (String t : KNOWN_TENANTS) {
                if (lower.contains(t) && !tenantsToTry.contains(t)) {
                    tenantsToTry.add(t);
                }
            }
        }
        if (!tenantsToTry.contains(originalTenant)) {
            tenantsToTry.add(originalTenant);
        }
        for (String t : KNOWN_TENANTS) {
            if (!tenantsToTry.contains(t)) {
                tenantsToTry.add(t);
            }
        }

        User foundUser = null;
        String matchedTenant = null;

        for (String tenant : tenantsToTry) {
            TenantContext.setCurrentTenant(tenant);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, email, password, role, branch FROM users WHERE email = '" + request.email().replace("'", "''") + "'")) {
                if (rs.next()) {
                    String encPassword = rs.getString("password");
                    if (passwordEncoder.matches(request.password(), encPassword)) {
                        foundUser = new User();
                        foundUser.setId(rs.getLong("id"));
                        foundUser.setEmail(rs.getString("email"));
                        foundUser.setPassword(encPassword);
                        foundUser.setRole(rs.getString("role"));
                        foundUser.setBranch(rs.getString("branch"));
                        matchedTenant = tenant;
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("Error checking tenant {} for user {}: {}", tenant, request.email(), e.getMessage());
            }
        }

        if (foundUser == null || matchedTenant == null) {
            TenantContext.setCurrentTenant(originalTenant);
            throw new InvalidCredentialsException();
        }

        TenantContext.setCurrentTenant(matchedTenant);
        String branch = (foundUser.getBranch() != null && !foundUser.getBranch().isBlank())
                ? foundUser.getBranch()
                : matchedTenant;

        String token = jwtService.generateToken(foundUser.getEmail(), foundUser.getRole(), branch);
        return AuthResponse.of(token, foundUser.getEmail(), foundUser.getRole(), branch);
    }

    public List<UserResponse> getAllUsers() {
        Set<String> seenEmails = new LinkedHashSet<>();
        List<UserResponse> all = new ArrayList<>();
        String originalTenant = TenantContext.getCurrentTenant();

        for (String tenant : KNOWN_TENANTS) {
            TenantContext.setCurrentTenant(tenant);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, email, role, branch FROM users ORDER BY id ASC")) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    String branch = rs.getString("branch");
                    if (seenEmails.add(email)) {
                        all.add(new UserResponse(
                                id,
                                email,
                                role,
                                (branch != null && !branch.isBlank()) ? branch : tenant
                        ));
                    }
                }
            } catch (Exception e) {
                log.debug("Could not read users from tenant {}: {}", tenant, e.getMessage());
            }
        }

        TenantContext.setCurrentTenant(originalTenant);
        return all;
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
