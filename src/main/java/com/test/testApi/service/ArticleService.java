package com.test.testApi.service;

import com.test.testApi.dto.req.ArticleReq;
import com.test.testApi.dto.res.ArticleRes;
import com.test.testApi.entity.Article;
import com.test.testApi.entity.User;
import com.test.testApi.repository.ArticleRepository;
import com.test.testApi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. 新增文章
    @Transactional
    public ArticleRes createArticle(ArticleReq req, String authorEmail) {
        log.info("User {} is creating a new article", authorEmail);

        User author = userRepository.findByEmail(authorEmail).orElseThrow(() -> new RuntimeException("User not found: " + authorEmail));

        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setContent(req.getContent());
        article.setStatus(req.getStatus());
        article.setTags(req.getTags());
        article.setAuthor(author);

        Article savedArticle = articleRepository.save(article);
        return convertToRes(savedArticle);
    }

    // 2. 分頁與標題搜尋
    public Page<ArticleRes> getArticles(String keyword, Pageable pageable) {
        Specification<Article> spec = (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null; // 若無關鍵字，不加入過濾條件
            }
            // 產生 SQL: WHERE title LIKE '%keyword%'
            return cb.like(root.get("title"), "%" + keyword + "%");
        };

        return articleRepository.findAll(spec, pageable).map(this::convertToRes);
    }

    // 3. 取得單篇文章
    public ArticleRes getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found: " + id));
        return convertToRes(article);
    }

    // 4. 更新文章
    @Transactional
    public ArticleRes updateArticle(Long id, ArticleReq req) {
        log.info("Updating article id: {}", id);

        Article article = articleRepository.findById(id).orElseThrow(() -> new RuntimeException("Article not found: " + id));

        article.setTitle(req.getTitle());
        article.setContent(req.getContent());
        article.setStatus(req.getStatus());
        article.setTags(req.getTags());

        Article updatedArticle = articleRepository.save(article);
        return convertToRes(updatedArticle);
    }

    // 5. 刪除文章
    @Transactional
    public void deleteArticle(Long id) {
        log.info("Deleting article id: {}", id);
        articleRepository.deleteById(id);
    }

    // 內部共用：將 Entity 轉為回傳用的 DTO
    private ArticleRes convertToRes(Article article) {
        ArticleRes res = new ArticleRes();
        res.setId(article.getId());
        res.setTitle(article.getTitle());
        res.setContent(article.getContent());
        res.setStatus(article.getStatus());
        res.setTags(article.getTags());
        res.setAuthorName(article.getAuthor().getUserName());
        res.setCreatedAt(article.getCreatedAt());
        res.setUpdatedAt(article.getUpdatedAt());
        return res;
    }
}