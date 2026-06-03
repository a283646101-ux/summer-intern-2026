package com.jaiot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RAG 知识文档实体
 *
 * 存储已导入知识库的文档元信息。
 */
@Entity
@Table(name = "rag_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 文档名称 */
    @Column(nullable = false)
    private String fileName;

    /** 文档内容摘要 */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /** 切分后的文档块数量 */
    private Integer chunkCount;

    /** 导入时间 */
    @Column(nullable = false)
    private LocalDateTime createTime;
}
