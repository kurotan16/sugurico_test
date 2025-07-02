package com.example.suguriko.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // 1つのLogは複数のLogImageを持つことができる (一対多)
    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LogImage> images = new ArrayList<>();

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

    // ★★★ ヘルパーメソッドを追加 ★★★
    public void addImage(LogImage image) {
        images.add(image);
        image.setLog(this);
    }
}