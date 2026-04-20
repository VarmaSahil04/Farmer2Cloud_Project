package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.dto.LoginRequest;
import net.farmtocloud.app.dto.LoginResponse;
import net.farmtocloud.app.dto.SignupRequest;
import net.farmtocloud.app.entity.User;
import net.farmtocloud.app.repository.UserRepository;
import net.farmtocloud.app.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public User signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole().toUpperCase())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .cropTypes(request.getCropTypes())
                .upiId(request.getUpiId())
                .bankDetails(request.getBankDetails())
                .businessName(request.getBusinessName())
                .address(request.getAddress())
                .dailyRequirements(request.getDailyRequirements())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered: {} as {}", savedUser.getEmail(), savedUser.getRole());
        return savedUser;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        log.info("User logged in: {}", user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
