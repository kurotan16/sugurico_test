package com.example.suguriko.controller;

import com.example.suguriko.service.StorageService;
import com.example.suguriko.entity.LogImage;
import com.example.suguriko.entity.Comment;
import com.example.suguriko.entity.Log;
import com.example.suguriko.entity.User;
import com.example.suguriko.repository.LogImageRepository;
import com.example.suguriko.repository.LogRepository;
import com.example.suguriko.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final StorageService storageService;
    private final LogImageRepository logImageRepository;

    public HomeController(UserRepository userRepository, LogRepository logRepository, StorageService storageService, LogImageRepository logImageRepository) {
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.storageService = storageService;
        this.logImageRepository = logImageRepository;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // ログイン中のユーザー情報を取得
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        // そのユーザーのログ一覧を取得
        List<Log> logs = logRepository.findFirst3ByUserOrderByCreatedAtDesc(user);

        model.addAttribute("logs", logs);
        model.addAttribute("newLog", new Log()); // フォーム用の空のLogオブジェクト
        return "index";
    }

    @PostMapping("/logs")
    public String addLog(@ModelAttribute Log newLog,
                         @RequestParam("imageFiles") MultipartFile[] imageFiles, // 配列で受け取る
                         @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. 複数の画像ファイルを一括でアップロードし、URLのリストを取得
        List<String> imageUrls = storageService.uploadFiles(imageFiles, "logs-images");

        // 2. 取得した各URLからLogImageオブジェクトを作成し、newLogに追加
        for (String imageUrl : imageUrls) {
            LogImage logImage = new LogImage(imageUrl);
            newLog.addImage(logImage); // ヘルパーメソッドを使って追加
        }

        // 3. ログイン中のユーザー情報を取得し、newLogにセット
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        newLog.setUser(user);
        
        // 4. ログをDBに保存（Cascade設定により、関連するLogImageも自動で保存される）
        logRepository.save(newLog);

        return "redirect:/"; // トップページにリダイレクト
    }

    @GetMapping("/logs/{id}")
    public String showLogDetail(@PathVariable Long id, Model model) {
        // IDを使ってLogをデータベースから取得する
        Optional<Log> logOptional = logRepository.findById(id);

        if (logOptional.isPresent()) {
            // Logが見つかった場合、モデルに渡して詳細ページを表示
            model.addAttribute("log", logOptional.get());
            // コメントフォーム用に、空のCommentオブジェクトも渡す
            model.addAttribute("newComment", new Comment());
            return "log-detail"; // templates/log-detail.html を表示
        } else {
            // Logが見つからなかった場合、トップページにリダイレクト
            return "redirect:/";
        }
    }

    @PostMapping("/logs/{id}/delete")
    public String deleteLog(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        // 現在ログインしているユーザーの情報を取得
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        // 削除対象のログをIDで検索
        Optional<Log> logOptional = logRepository.findById(id);

        // ログが存在し、かつそのログの所有者が現在のユーザーである場合のみ削除
        if (logOptional.isPresent()) {
            Log logToDelete = logOptional.get();
            if (logToDelete.getUser().getId().equals(currentUser.getId())) {
                
                // 1. 関連する画像をSupabase Storageから削除
                for (LogImage image : logToDelete.getImages()) {
                    storageService.deleteFile(image.getImageUrl(), "logs-images");
                }
                
                // 2. ログをDBから削除 (関連するLogImageレコードもカスケード削除される)
                logRepository.delete(logToDelete);
            }
        }
        
        // 処理が終わったらトップページにリダイレクト
        return "redirect:/";
    }

    @GetMapping("/logs/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // ログインユーザー情報を取得
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // 編集対象のログを取得
        Optional<Log> logOptional = logRepository.findById(id);

        // ログが存在し、かつ自分が所有者であるかチェック
        if (logOptional.isPresent() && logOptional.get().getUser().getId().equals(currentUser.getId())) {
            model.addAttribute("log", logOptional.get());
            return "edit-log"; // templates/edit-log.html を表示
        }

        // ログが存在しないか、他人のログの場合はトップページにリダイレクト
        return "redirect:/";
    }

    /**
     * ログを更新する
     */
    // ★★★ メソッドの引数を修正 ★★★
    @PostMapping("/logs/{id}/edit")
    public String updateLog(@PathVariable Long id,
                            @ModelAttribute Log formLog,
                            @RequestParam("imageFiles") MultipartFile[] imageFiles,
                            // required=false で、チェックボックスが1つもなくてもエラーにしない
                            @RequestParam(name = "deleteImageIds", required = false) List<Long> deleteImageIds,
                            @AuthenticationPrincipal UserDetails userDetails) {

        // ログインユーザー情報を取得
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // 更新対象のログをDBから取得
        Optional<Log> logOptional = logRepository.findById(id);

        // ログが存在し、かつ自分が所有者であるかチェック
        if (logOptional.isPresent() && logOptional.get().getUser().getId().equals(currentUser.getId())) {
            Log dbLog = logOptional.get();
            
            // 1. テキスト情報を更新
            dbLog.setTitle(formLog.getTitle());
            dbLog.setContent(formLog.getContent());

            // 2. 既存の画像を削除
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                for (Long imageId : deleteImageIds) {
                    // DBから削除対象のLogImageを検索
                    logImageRepository.findById(imageId).ifPresent(imageToDelete -> {
                        // Storageからファイルを削除
                        storageService.deleteFile(imageToDelete.getImageUrl(), "logs-images");
                        // Logエンティティのimagesリストから削除 (JPAがDBからも削除してくれる)
                        dbLog.getImages().remove(imageToDelete);
                    });
                }
            }

            // 3. 新しい画像を追加
            List<String> newImageUrls = storageService.uploadFiles(imageFiles, "logs-images");
            for (String imageUrl : newImageUrls) {
                LogImage newImage = new LogImage(imageUrl);
                dbLog.addImage(newImage); // ヘルパーメソッドで追加
            }
            
            // 4. 変更をまとめて保存
            logRepository.save(dbLog);
        }

        // 処理が終わったらトップページにリダイレクト
        return "redirect:/";
    }
}