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
        
        // 公開ログをページ単位で取得
        Page<Log> logPage = logRepository.findByIsPublicOrderByCreatedAtDesc(true, pageable);
        
        model.addAttribute("logPage", logPage);
        
        return "timeline"; // templates/timeline.html を表示
    }
}