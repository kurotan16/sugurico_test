package com.example.suguriko.repository;

import com.example.suguriko.entity.Log;
import com.example.suguriko.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    // 特定のユーザーのログを、作成日時の新しい順に取得する
    List<Log> findByUserOrderByCreatedAtDesc(User user);

    // 特定のユーザーのログを、作成日時の新しい順に、上位N件だけ取得する
    List<Log> findFirst3ByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT l FROM Log l WHERE l.user = :user AND " +
           "(l.title LIKE %:keyword% OR l.content LIKE %:keyword%) " +
           "ORDER BY l.createdAt DESC")
    List<Log> findByUserAndKeyword(
        @Param("user") User user,
        @Param("keyword") String keyword
    );
}