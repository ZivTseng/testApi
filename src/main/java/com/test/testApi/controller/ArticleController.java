package com.test.testApi.controller;

import com.test.testApi.dto.req.ArticleReq;
import com.test.testApi.dto.res.ArticleRes;
import com.test.testApi.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // 1. 新增文章 (Principal 會自動抓取目前登入者的 Token 資訊)
    @PostMapping
    public ResponseEntity<ArticleRes> createArticle(@Valid @RequestBody ArticleReq req, Principal principal) {
        log.info("User {} creating article", principal.getName());
        ArticleRes res = articleService.createArticle(req, principal.getName());
        return ResponseEntity.ok(res);
    }

    // 2. 取得文章列表 (支援分頁與關鍵字搜尋)
    @GetMapping
    public ResponseEntity<Page<ArticleRes>> getArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching articles - keyword: {}, page: {}, size: {}", keyword, page, size);

        // 預設依照建立時間遞減排序 (最新的文章在最前面)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArticleRes> articles = articleService.getArticles(keyword, pageable);

        return ResponseEntity.ok(articles);
    }

    // 3. 取得單篇文章詳細資訊
    @GetMapping("/{id}")
    public ResponseEntity<ArticleRes> getArticleById(@PathVariable Long id) {
        log.info("Fetching article id: {}", id);
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    // 4. 更新文章
    @PutMapping("/{id}")
    public ResponseEntity<ArticleRes> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleReq req) {
        log.info("Updating article id: {}", id);
        return ResponseEntity.ok(articleService.updateArticle(id, req));
    }

    // 5. 刪除文章
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        log.info("Deleting article id: {}", id);
        articleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }
}