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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;
import com.example.suguriko.service.StorageService;
import java.io.IOException;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final StorageService storageService;

    public HomeController(UserRepository userRepository, LogRepository logRepository, StorageService storageService) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.storageService = storageService;
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
    public String addLog(@ModelAttribute Log newLog,
                        @RequestParam("imageFile") MultipartFile imageFile,
                        @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        String imageUrl = storageService.uploadFile(imageFile, "logs-images");
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        // 新しいログにユーザーをセットして保存
        newLog.setUser(user);
        newLog.setImageUrl(imageUrl);
        logRepository.save(newLog);

        return "redirect:/";
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

    @GetMapping("/logs/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // ログインユーザー情報を取得
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // 編集対象のログを取得
        Optional<Log> logOptional = logRepository.findById(id);

        // ログが存在し、かつ自分が所有者であるかチェック
        if (logOptional.isPresent() && logOptional.get().getUser().getId().equals(currentUser.getId())) {
            model.addAttribute("log", logOptional.get());
            return "edit-log"; // templates/edit-log.html を表示
        }

        // ログが存在しないか、他人のログの場合はトップページにリダイレクト
        return "redirect:/";
    }

    /**
     * ログを更新する
     */
    @PostMapping("/logs/{id}/edit")
    public String updateLog(@PathVariable Long id,
                            @ModelAttribute Log formLog, // フォームの入力内容を受け取る
                            @AuthenticationPrincipal UserDetails userDetails) {

        // ログインユーザー情報を取得
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // 更新対象のログをDBから取得
        Optional<Log> logOptional = logRepository.findById(id);

        // ログが存在し、かつ自分が所有者であるかチェック
        if (logOptional.isPresent() && logOptional.get().getUser().getId().equals(currentUser.getId())) {
            // 変更を適用する
            Log dbLog = logOptional.get();
            dbLog.setTitle(formLog.getTitle());
            dbLog.setContent(formLog.getContent());
            logRepository.save(dbLog); // 変更を保存
        }

        // 処理が終わったらトップページにリダイレクト
        return "redirect:/";
    }
}