package com.example.suguriko.repository;

import com.example.suguriko.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 今は空でOK
}
