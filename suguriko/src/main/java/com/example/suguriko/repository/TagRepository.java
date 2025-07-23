package com.example.suguriko.repository;

import com.example.suguriko.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    
    // タグ名でタグを検索する
    Optional<Tag> findByName(String name);

    // 複数のタグ名でタグを検索する
    Set<Tag> findByNameIn(List<String> names);
}
