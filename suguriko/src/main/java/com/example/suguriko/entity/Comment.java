package com.example.suguriko.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments") // DBのテーブル名は "comments"
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Comment(多) 対 Log(一) の関係
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    @JsonIgnore
    private Log log;

    // Comment(多) 対 User(一) の関係
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // user情報はJSONに含めたいので、ここには付けない

    // データが保存される直前に実行されるメソッド
    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}