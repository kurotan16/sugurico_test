package com.example.suguriko.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetMail(String to, String token) {
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("【スグリコ】パスワード再設定のご案内");
        message.setText("以下のリンクをクリックしてパスワードを再設定してください。\n\n" + resetUrl + "\n\nこのリンクは1時間で無効になります。");
        mailSender.send(message);
    }
}