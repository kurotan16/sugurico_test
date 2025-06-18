package com.example.suguriko.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Getter, Setterなどを自動生成 (Lombok)
@Entity
@Table(name = "users") // DBのテーブル名を指定
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;
}