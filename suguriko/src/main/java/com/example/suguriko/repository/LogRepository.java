package com.example.suguriko.repository;

import com.example.suguriko.entity.Log;
import com.example.suguriko.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LogRepository extends JpaRepository<Log, Long> {

    // ユーザーID一致するID
    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.id = :id")
    Optional<Log> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.user = :user ORDER BY l.createdAt DESC")
    List<Log> findByUserOrderByCreatedAtDescWithDetails(@Param("user") User user);

    // すべてを検索するメソッド
    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.user = :user AND " +
           "(l.title LIKE %:keyword% OR l.content LIKE %:keyword% OR EXISTS (SELECT t FROM l.tags t WHERE t.name LIKE %:keyword%)) " +
           "ORDER BY l.createdAt DESC")
    List<Log> findByUserAndKeywordWithDetails(@Param("user") User user, @Param("keyword") String keyword);

    // 「本文とタイトルのみ」を検索するメソッド
    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.user = :user AND " +
           "(l.title LIKE %:keyword% OR l.content LIKE %:keyword%) " +
           "ORDER BY l.createdAt DESC")
    List<Log> findByUserAndTextKeywordWithDetails(@Param("user") User user, @Param("keyword") String keyword);

    // 「タグのみ」を検索するメソッド
    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.user = :user AND " +
           "EXISTS (SELECT t FROM l.tags t WHERE t.name LIKE %:keyword%) " +
           "ORDER BY l.createdAt DESC")
    List<Log> findByUserAndTagKeywordWithDetails(@Param("user") User user, @Param("keyword") String keyword);

    // トップページ表示用の、最新3件のログIDを取得する
    @Query("SELECT l.id FROM Log l WHERE l.user = :user ORDER BY l.createdAt DESC")
    List<Long> findLatest3LogIdsByUser(@Param("user") User user, Pageable pageable);

    // IDのリストを元に、詳細情報を含めてログを取得する
    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags WHERE l.id IN :ids")
    List<Log> findByIdInWithTags(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.images WHERE l.id IN :ids")
    List<Log> findByIdInWithImages(@Param("ids") List<Long> ids);

    @Query(value = "SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.isPublic = true AND l.createdAt > :sinceDateTime ORDER BY l.createdAt DESC",
           countQuery = "SELECT count(l) FROM Log l WHERE l.isPublic = true AND l.createdAt > :sinceDateTime")
    Page<Log> findByIsPublicAndCreatedAtAfterOrderByCreatedAtDescWithDetails(@Param("sinceDateTime") LocalDateTime sinceDateTime, Pageable pageable);

    // ユーザーのすべてのログIDを取得する
    @Query("SELECT l.id FROM Log l WHERE l.user = :user ORDER BY l.createdAt DESC")
    List<Long> findAllLogIdsByUser(@Param("user") User user);

    // 「本文とタイトル」で検索したログIDを取得する
    @Query("SELECT l.id FROM Log l WHERE l.user = :user AND (l.title LIKE %:keyword% OR l.content LIKE %:keyword%) ORDER BY l.createdAt DESC")
    List<Long> findLogIdsByUserAndTextKeyword(@Param("user") User user, @Param("keyword") String keyword);

    // 「タグのみ」で検索したログIDを取得する
    @Query("SELECT l.id FROM Log l JOIN l.tags t WHERE l.user = :user AND t.name LIKE %:keyword% ORDER BY l.createdAt DESC")
    List<Long> findLogIdsByUserAndTagKeyword(@Param("user") User user, @Param("keyword") String keyword);
}