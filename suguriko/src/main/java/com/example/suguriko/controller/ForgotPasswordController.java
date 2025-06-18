package com.example.suguriko.controller;

import java.time.LocalDateTime;
import java.util.Optional; 
import com.example.suguriko.entity.User;
import com.example.suguriko.service.MailService;
import com.example.suguriko.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {

    private final UserService userService;
    private final MailService mailService;

    public ForgotPasswordController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    // パスワード再設定要求フォームの表示
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password-form";
    }

    // メールを送信する処理
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = userService.createPasswordResetToken(user);
            mailService.sendPasswordResetMail(user.getEmail(), token);
        }
        // ユーザーが存在しない場合でも、存在するようなメッセージを表示（セキュリティ対策）
        redirectAttributes.addFlashAttribute("message", "パスワード再設定用のメールを送信しました。メールが届かない場合は、入力したアドレスが正しいか確認してください。");
        return "redirect:/forgot-password";
    }

    // パスワード再設定フォームの表示
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findByResetPasswordToken(token);
        if (userOptional.isEmpty() || userOptional.get().getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "リンクが無効か、有効期限が切れています。もう一度やり直してください。");
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        return "reset-password-form";
    }

    // パスワードを更新する処理
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findByResetPasswordToken(token);
        if (userOptional.isEmpty() || userOptional.get().getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "リンクが無効か、有効期限が切れています。もう一度やり直してください。");
            return "redirect:/forgot-password";
        }
        
        User user = userOptional.get();
        userService.resetPassword(user, password);
        
        redirectAttributes.addFlashAttribute("message", "パスワードが正常に更新されました。");
        return "redirect:/login";
    }
}