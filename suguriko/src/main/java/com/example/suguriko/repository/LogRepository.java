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

    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.id = :id")
    Optional<Log> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.user = :user ORDER BY l.createdAt DESC")
    List<Log> findByUserOrderByCreatedAtDescWithDetails(@Param("user") User user);
    
    @Query(value = "SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.user = :user ORDER BY l.createdAt DESC",
           countQuery = "SELECT count(l) FROM Log l WHERE l.user = :user")
    List<Log> findFirst3ByUserOrderByCreatedAtDescWithDetails(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.user = :user AND " +
           "(l.title LIKE %:keyword% OR l.content LIKE %:keyword% OR EXISTS (SELECT t FROM l.tags t WHERE t.name LIKE %:keyword%)) " +
           "ORDER BY l.createdAt DESC")
    List<Log> findByUserAndKeywordWithDetails(@Param("user") User user, @Param("keyword") String keyword);

    @Query(value = "SELECT DISTINCT l FROM Log l LEFT JOIN FETCH l.tags LEFT JOIN FETCH l.images WHERE l.isPublic = true AND l.createdAt > :sinceDateTime ORDER BY l.createdAt DESC",
           countQuery = "SELECT count(l) FROM Log l WHERE l.isPublic = true AND l.createdAt > :sinceDateTime")
    Page<Log> findByIsPublicAndCreatedAtAfterOrderByCreatedAtDescWithDetails(@Param("sinceDateTime") LocalDateTime sinceDateTime, Pageable pageable);
}