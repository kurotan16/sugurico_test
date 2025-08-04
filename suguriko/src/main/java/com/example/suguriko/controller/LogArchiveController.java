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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

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
        @RequestParam(name = "searchType", defaultValue = "all") String searchType,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Long> logIds;

        // 1. まず条件に合うログIDのリストを取得する
        if (keyword == null || keyword.isBlank()) {
            // キーワードなし -> 全件取得
            logIds = logRepository.findAllLogIdsByUser(user);
        } else {
            // キーワードあり -> searchTypeで分岐
            switch (searchType) {
                case "text":
                    logIds = logRepository.findLogIdsByUserAndTextKeyword(user, keyword);
                    break;
                case "tag":
                    logIds = logRepository.findLogIdsByUserAndTagKeyword(user, keyword);
                    break;
                case "all":
                default:
                    // 「すべて」検索の場合は、一度JPQL版のメソッドでIDを取得する
                    // (この部分は少し非効率だが、ひとまずこれで実装)
                    logIds = logRepository.findByUserAndKeywordWithDetails(user, keyword)
                                        .stream()
                                        .map(Log::getId)
                                        .collect(Collectors.toList());
                    break;
            }
        }

        List<Log> logs;
        if (logIds.isEmpty()) {
            logs = new ArrayList<>();
        } else {
            // 2. 取得したIDリストを元に、詳細情報を一括取得
            // (TagとImageを別々にフェッチして、直積爆発を避ける)
            List<Log> logsWithTags = logRepository.findByIdInWithTags(logIds);
            List<Log> logsWithDetails = logRepository.findByIdInWithImages(logIds);
            
            // 3. createdAtで降順にソートして最終的なリストを作成
            logs = logsWithDetails.stream()
                        .sorted(Comparator.comparing(Log::getCreatedAt).reversed())
                        .collect(Collectors.toList());
        }

        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "logs-archive";
    }
}