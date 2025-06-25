package com.example.suguriko.controller;

import com.example.suguriko.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordChangeController {

    private final UserService userService;

    public PasswordChangeController(UserService userService) {
        this.userService = userService;
    }

    // パスワード変更フォームを表示
    @GetMapping("/password/change")
    public String showPasswordChangeForm() {
        return "password-change-form";
    }

    // パスワード変更処理
    @PostMapping("/password/change")
    public String processPasswordChange(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            // UserServiceのメソッドを呼び出し
            userService.changePassword(
                userDetails.getUsername(),
                currentPassword,
                newPassword,
                confirmPassword
            );
            redirectAttributes.addFlashAttribute("successMessage", "パスワードが正常に変更されました。");
            return "redirect:/profile"; // 成功したらプロフィールページへリダイレクト
        } catch (IllegalArgumentException e) {
            // Serviceで発生したエラー（パスワード不一致など）をキャッチ
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/password/change"; // 失敗したらフォームへ戻る
        }
    }
}