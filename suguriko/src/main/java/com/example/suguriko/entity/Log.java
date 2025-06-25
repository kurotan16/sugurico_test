package com.example.suguriko.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "logs") // DBに"logs"テーブルが作られる
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255) // タイトルは必須にしない場合は nullable=false は不要
    private String title;

    @Column(nullable = false, length = 1000) // 1000文字まで
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Logエンティティは「多」、Userエンティティは「一」の関係
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // user_idカラムでUserテーブルと連携
    private User user;

    // データが保存される直前に実行されるメソッド
    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}