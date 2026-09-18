package com.example.course_registration_portal.service;

import com.example.course_registration_portal.dto.ProfileDto;
import com.example.course_registration_portal.dto.RegisterDto;
import com.example.course_registration_portal.entity.User;
import com.example.course_registration_portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerStudent(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with email " + dto.getEmail() + " already exists.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone().trim());
        user.setDepartment(dto.getDepartment());
        user.setRole("STUDENT");

        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    @Transactional
    public User updateProfile(Long userId, ProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(dto.getName().trim());
        user.setPhone(dto.getPhone().trim());
        user.setDepartment(dto.getDepartment());

        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
            if (dto.getCurrentPassword() == null || !passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            if (dto.getNewPassword().trim().length() < 6) {
                throw new IllegalArgumentException("New password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
        }

        return userRepository.save(user);
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole("STUDENT");
    }

    public long countStudents() {
        return userRepository.countByRole("STUDENT");
    }
}
