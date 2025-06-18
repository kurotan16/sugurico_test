package com.example.suguriko.repository;

import com.example.suguriko.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // ユーザー名でユーザーを検索するためのメソッド
    Optional<User> findByUsername(String username);
}