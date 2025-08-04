package com.example.suguriko.controller;

import com.example.suguriko.entity.Log;
import com.example.suguriko.repository.LogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TimelineController {

    private final LogRepository logRepository;

    public TimelineController(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping("/timeline")
    public String showTimeline(@RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size,
                               Model model) {
        
        // ページネーション情報を設定 (0ページ目から、1ページあたり10件)
        Pageable pageable = PageRequest.of(page, size);
        
        // 取得する期間を定義 (例: 7日前から現在まで)
        LocalDateTime sinceDateTime = LocalDateTime.now().minusDays(7);
        
        // 1. まず条件に合うログIDのリスト(ページ)を取得する
        Page<Long> logIdPage = logRepository.findPublicLogIdsAfter(sinceDateTime, pageable);
        List<Long> logIds = logIdPage.getContent();

        List<Log> logs;
        if (logIds.isEmpty()) {
            logs = new ArrayList<>();
        } else {
            // 2. 取得したIDリストを元に、詳細情報を一括取得
            List<Log> logsWithTags = logRepository.findByIdInWithTags(logIds);
            List<Log> logsWithDetails = logRepository.findByIdInWithImages(logIds);

            // 3. createdAtで降順にソートして最終的なリストを作成
            logs = logsWithDetails.stream()
                        .sorted(Comparator.comparing(Log::getCreatedAt).reversed())
                        .collect(Collectors.toList());
        }

        // 4. ページネーション情報をビューに渡すために、手動でPageオブジェクトを再生成する
        // ※この部分は少し複雑ですが、ページネーションUIを正しく表示するための定型句です
        Page<Log> logPage = new org.springframework.data.domain.PageImpl<>(
            logs, 
            pageable, 
            logIdPage.getTotalElements()
        );
        
        model.addAttribute("logPage", logPage);
        
        return "timeline";
    }
}