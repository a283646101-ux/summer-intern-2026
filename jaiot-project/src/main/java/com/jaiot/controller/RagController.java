package com.jaiot.controller;

import com.jaiot.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG 知识库 API
 *
 * 支持：
 * - 基于知识库提问
 * - 列出已导入的文档
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * POST /api/rag/ask
     * 基于知识库提问
     *
     * Body: { "query": "多肉植物怎么浇水？" }
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "query 不能为空"));
        }

        log.info("📚 RAG 查询: {}", query);
        String knowledge = ragService.ask(query);

        return ResponseEntity.ok(Map.of(
                "query", query,
                "knowledge", knowledge
        ));
    }

    /**
     * GET /api/rag/documents
     * 列出已导入的知识文档
     */
    @GetMapping("/documents")
    public ResponseEntity<?> listDocuments() {
        return ResponseEntity.ok(ragService.listDocuments());
    }
}
