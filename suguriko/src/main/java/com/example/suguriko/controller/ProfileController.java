package com.example.suguriko.controller;

import com.example.suguriko.entity.User;
import com.example.suguriko.repository.UserRepository;
import com.example.suguriko.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;

    public ProfileController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // プロフィール編集画面の表示
    @GetMapping("/profile")
    public String showProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // 現在のユーザー情報をDBから取得
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // モデルにユーザー情報を渡して、画面に表示できるようにする
        model.addAttribute("user", currentUser);
        return "profile"; // templates/profile.html を表示
    }

    // プロフィール更新処理
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String username,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {

        try {
            // UserServiceの更新メソッドを呼び出す
            userService.updateProfile(userDetails.getUsername(), username, email);
            redirectAttributes.addFlashAttribute("successMessage", "プロフィールが正常に更新されました。");
        } catch (IllegalStateException e) {
            // サービスで重複エラーなどが発生した場合
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // エラーがあった場合は、入力内容を維持してフォームに戻す
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/profile";
        }

        // ユーザー名が変更されたら、表示が更新されるトップページにリダイレクトする
        if (!userDetails.getUsername().equals(username)) {
            return "redirect:/";
        }
        
        return "redirect:/profile";
    }
}