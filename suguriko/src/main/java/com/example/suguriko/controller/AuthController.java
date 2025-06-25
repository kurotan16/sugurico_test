package com.example.suguriko.controller;

import com.example.suguriko.entity.User;
import com.example.suguriko.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ★★★ import を追加 ★★★

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html を表示
    }

    @GetMapping("/register")
    public String register(Model model) {
        // "user"という名前の属性がモデルになければ、新しいUserオブジェクトを追加
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register"; // templates/register.html を表示

    }

    // ★★★ メソッド全体を修正 ★★★
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        // ユーザー名が既に存在するかチェック
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "そのユーザー名は既に使用されています。");
            redirectAttributes.addFlashAttribute("user", user); // 入力内容をフォームに返す
            return "redirect:/register";
        }

        // メールアドレスが既に存在するかチェック
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "そのメールアドレスは既に使用されています。");
            redirectAttributes.addFlashAttribute("user", user); // 入力内容をフォームに返す
            return "redirect:/register";
        }

        // パスワードをハッシュ化
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "登録が完了しました。ログインしてください。");
        return "redirect:/login"; 
    }
}