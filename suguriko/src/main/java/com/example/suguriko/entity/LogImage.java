package com.example.suguriko.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // NoArgsConstructorを追加

@Data
@Entity
@Table(name = "log_images") // DBのテーブル名は "log_images"
@NoArgsConstructor // 引数なしコンストラクタをLombokで自動生成
public class LogImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    // LogImageエンティティは「多」、Logエンティティは「一」の関係
    @ManyToOne(fetch = FetchType.LAZY) // LAZYフェッチを指定
    @JoinColumn(name = "log_id", nullable = false) // log_idカラムでLogテーブルと連携
    private Log log;

    // 画像URLを引数に取るコンストラクタ
    public LogImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}