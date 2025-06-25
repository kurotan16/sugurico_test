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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable; // ★★★ import を追加 ★★★
import java.util.Optional; // ★★★ import を追加 ★★★

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final LogRepository logRepository;

    public HomeController(UserRepository userRepository, LogRepository logRepository) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // ログイン中のユーザー情報を取得
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        // そのユーザーのログ一覧を取得
        List<Log> logs = logRepository.findByUserOrderByCreatedAtDesc(user);

        model.addAttribute("logs", logs);
        model.addAttribute("newLog", new Log()); // フォーム用の空のLogオブジェクト
        return "index";
    }

    @PostMapping("/logs")
    public String addLog(@ModelAttribute Log newLog, @AuthenticationPrincipal UserDetails userDetails) {
        // ログイン中のユーザー情報を取得
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        // 新しいログにユーザーをセットして保存
        newLog.setUser(user);
        logRepository.save(newLog);

        return "redirect:/"; // トップページにリダイレクト
    }

    @PostMapping("/logs/{id}/delete")
    public String deleteLog(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        // 現在ログインしているユーザーの情報を取得
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        // 削除対象のログをIDで検索
        Optional<Log> logOptional = logRepository.findById(id);

        // ログが存在し、かつそのログの所有者が現在のユーザーである場合のみ削除
        if (logOptional.isPresent()) {
            Log log = logOptional.get();
            if (log.getUser().getId().equals(currentUser.getId())) {
                logRepository.deleteById(id);
            }
        }
        
        // 処理が終わったらトップページにリダイレクト
        return "redirect:/";
    }
}