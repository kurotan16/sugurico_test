package com.example.suguriko.repository;

import com.example.suguriko.entity.Log;
import com.example.suguriko.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    // 特定のユーザーのログを、作成日時の新しい順に取得する
    List<Log> findByUserOrderByCreatedAtDesc(User user);
}