package com.example.suguriko.service;

import com.example.suguriko.entity.User;
import com.example.suguriko.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;


@Service
public class UserService {
    // ... (既存のコードはそのまま)

    @Transactional // 複数のDB操作を1つのトランザクションとして扱う
    public void updateProfile(String currentUsername, String newUsername, String newEmail) {
        // 現在のユーザー情報を取得
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません。"));

        boolean usernameChanged = false; //ユーザー名が変更されたかどうかのフラグ

        // --- 重複チェック ---
        // 1. 新しいユーザー名が、自分以外のユーザーに使われていないかチェック
        if (!currentUser.getUsername().equals(newUsername)) {
            userRepository.findByUsernameAndIdNot(newUsername, currentUser.getId())
                .ifPresent(u -> {
                    throw new IllegalStateException("そのユーザー名は既に使用されています。");
                });
            currentUser.setUsername(newUsername);
            usernameChanged = true; //フラグを立てる
        }

        // 2. 新しいEmailが、自分以外のユーザーに使われていないかチェック
        if (!currentUser.getEmail().equals(newEmail)) {
            userRepository.findByEmailAndIdNot(newEmail, currentUser.getId())
                .ifPresent(u -> {
                    throw new IllegalStateException("そのメールアドレスは既に使用されています。");
                });
            currentUser.setEmail(newEmail);
        }

        // 変更を保存
        userRepository.save(currentUser);

        // ユーザー名が変更された場合、SecurityContextの認証情報も更新する
        if (usernameChanged) {
            updateAuthentication(newUsername);
        }
    }

    /**
     * 現在の認証情報を新しいユーザー名で更新する
     * @param newUsername 新しいユーザー名
     */
    private void updateAuthentication(String newUsername) {
        // 現在の認証情報を取得
        Authentication oldAuth = SecurityContextHolder.getContext().getAuthentication();
        
        // 新しいUserDetailsオブジェクトを作成（パスワードは更新しないので空でOK）
        UserDetails newPrincipal = new org.springframework.security.core.userdetails.User(
            newUsername,
            "", // パスワードは空のままで問題ない
            Collections.emptyList()
        );

        // 新しい認証情報を作成
        Authentication newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            newPrincipal,
            oldAuth.getCredentials(),
            oldAuth.getAuthorities()
        );

        // SecurityContextに新しい認証情報を設定
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

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