package com.example.suguriko.service;

import com.example.suguriko.entity.User;
import com.example.suguriko.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        // emailでユーザーを検索するメソッドをRepositoryに追加する必要がある
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1)); // 有効期限を1時間に設定
        userRepository.save(user);
        return token;
    }

    public void resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null); // トークンを無効化
        user.setResetPasswordTokenExpiry(null); // 有効期限を無効化
        userRepository.save(user);
    }
}