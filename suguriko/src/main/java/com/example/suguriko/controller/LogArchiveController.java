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
        @RequestParam(name = "searchType", defaultValue = "all") String searchType, // ULRパラメータを受け取る(指定無しならAll)
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Log> logs;

        // keywordが空の場合は、検索せずに空の結果を返す
        if (keyword == null || keyword.isBlank()) {
            logs = logRepository.findByUserOrderByCreatedAtDescWithDetails(user);
        } else {
            // ラジオボタンに応じて変化
            switch (searchType) {
                case "text":
                    logs = logRepository.findByUserAndTextKeywordWithDetails(user, keyword);
                    break;
                case "tag":
                    logs = logRepository.findByUserAndTagKeywordWithDetails(user, keyword);
                    break;
                case "all":
                default:
                    logs = logRepository.findByUserAndKeywordWithDetails(user, keyword);
                    break;
            }
        }

        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType); // 選択されたタイプをビューに返す
        return "logs-archive";
    }
}