package com.example.suguriko.controller;

import com.example.suguriko.entity.Comment;
import com.example.suguriko.entity.Log;
import com.example.suguriko.entity.User;
import com.example.suguriko.repository.CommentRepository;
import com.example.suguriko.repository.LogRepository;
import com.example.suguriko.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CommentController {

    private final CommentRepository commentRepository;
    private final LogRepository logRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository, LogRepository logRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/logs/{logId}/comments")
    public String addComment(@PathVariable Long logId,
                             @ModelAttribute Comment newComment,
                             @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. コメント対象のログを取得
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("無効なログID: " + logId));

        // 2. コメントしたユーザーを取得
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません。"));

        // 3. Commentオブジェクトに必要な情報をセット
        newComment.setLog(log);
        newComment.setUser(user);

        // 4. コメントをDBに保存
        commentRepository.save(newComment);

        // 5. 元のログ詳細ページにリダイレクト
        return "redirect:/logs/" + logId;
    }
}