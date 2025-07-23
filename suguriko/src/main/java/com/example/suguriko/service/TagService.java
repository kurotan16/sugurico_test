package com.example.suguriko.service;

import com.example.suguriko.entity.Tag;
import com.example.suguriko.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Set<Tag> findOrCreateTags(String tagInput) {
        // 入力が空の場合は空のセットを返す
        if (tagInput == null || tagInput.isBlank()) {
            return new HashSet<>();
        }
    
        // 1. カンマや全角スペースで分割し、前後の空白を除去し、空文字列を除外
        List<String> tagNames = Arrays.stream(tagInput.split("[,、\\s]+"))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct() // 重複を除外
                .collect(Collectors.toList());

        // 2. タグの上限数をチェック (5個まで)
        if (tagNames.size() > 5) {
            throw new IllegalArgumentException("タグは5個までしか設定できません。");
        }

        // 3. 既存のタグをDBから一括で検索
        Set<Tag> existingTags = tagRepository.findByNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // 4. まだDBに存在しない、新しいタグを特定
        Set<Tag> newTags = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(Tag::new) // name -> new Tag(name)
                .collect(Collectors.toSet());

        // 5. 新しいタグをDBに保存
        tagRepository.saveAll(newTags);

        // 6. 既存のタグと新しく保存したタグを合体させて返す
        Set<Tag> allTags = new HashSet<>(existingTags);
        allTags.addAll(newTags);

        return allTags;
    }

}
