package com.jaiot.repository;

import com.jaiot.entity.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RAG 知识文档 Repository
 */
@Repository
public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {
}
