package com.example.suguriko.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class StorageService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String serviceKey;

    public StorageService(WebClient.Builder webClientBuilder,
                        //設定した値をコード内に読み込み 
                          @Value("${supabase.url}") String supabaseUrl,
                          @Value("${supabase.service-key}") String serviceKey) {
        this.supabaseUrl = supabaseUrl;
        this.serviceKey = serviceKey;
        // WebClientのインスタンスを作成
        this.webClient = webClientBuilder.baseUrl(supabaseUrl + "/storage/v1/").build();
    }

    /**
     * ファイルをSupabase Storageにアップロードし、公開URLを返す
     * @param file アップロードするファイル
     * @param bucketName バケット名
     * @return 公開URL
     * @throws IOException ファイル読み取りエラー
     */
    public String uploadFile(MultipartFile file, String bucketName) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // --- ここからが新しいファイル名生成ロジック ---
        String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "");
        
        // ファイルの拡張子を取得 (例: ".jpg")
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            extension = originalFilename.substring(lastDotIndex);
        }

        // UUIDと拡張子を組み合わせて、安全な新しいファイル名を生成
        String newFileName = UUID.randomUUID().toString() + extension;
        // --- ここまで ---

        String uploadPath = "object/" + bucketName + "/" + newFileName;
        String contentType = Objects.requireNonNullElse(file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);

        webClient.post()
                .uri(uploadPath)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceKey)
                .header("X-Upsert", "true")
                .contentType(MediaType.parseMediaType(contentType))
                .body(BodyInserters.fromResource(file.getResource()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 公開URLも、新しいファイル名で組み立てる
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + newFileName;
    }
}