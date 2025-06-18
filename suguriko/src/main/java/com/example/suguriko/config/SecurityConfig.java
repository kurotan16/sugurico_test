package com.example.suguriko.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // CSS, JS, 画像などの静的リソースは誰でもアクセス可能
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // 新規登録ページとログインページは誰でもアクセス可能
                .requestMatchers("/register", "/login").permitAll()
                // その他のリクエストはすべて認証が必要
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // ログインページのパス
                .loginPage("/login")
                // ログイン成功時のリダイレクト先
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                // ログアウト成功時のリダイレクト先
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // パスワードをハッシュ化するためのエンコーダー
        return new BCryptPasswordEncoder();
    }
}