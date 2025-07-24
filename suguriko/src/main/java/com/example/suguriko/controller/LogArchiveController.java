package com.example.suguriko.controller;

import com.example.suguriko.entity.Log;
import com.example.suguriko.entity.User;
import com.example.suguriko.repository.LogRepository;
import com.example.suguriko.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/logs/archive") // このControllerは /logs/archive で始まるURLを処理
public class LogArchiveController {

    private final UserRepository userRepository;
    private final LogRepository logRepository;

    public LogArchiveController(UserRepository userRepository, LogRepository logRepository) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
    }

    @GetMapping
    public String showArchive(
        @RequestParam(name = "keyword", required = false) String keyword,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Log> logs;

        // keywordパラメータの有無で処理を分岐
        if (keyword != null && !keyword.isBlank()) {
            logs = logRepository.findByUserAndKeywordWithDetails(user, keyword);
            // ...
        } else {
            logs = logRepository.findByUserOrderByCreatedAtDescWithDetails(user);
        }

        model.addAttribute("logs", logs);
        return "logs-archive"; // templates/logs-archive.html を表示
    }
}