package com.example.suguriko.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "tags")
@EqualsAndHashCode(exclude = "logs") // 無限ループを避けるため
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore // JSONに変換する際に無限ループを避ける
    private Set<Log> logs = new HashSet<>();

    // コンストラクタ
    public Tag() {}

    public Tag(String name) {
        this.name = name;
    }
}
