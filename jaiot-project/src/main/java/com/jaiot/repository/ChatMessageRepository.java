package com.jaiot.repository;

import com.jaiot.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天消息 Repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** 按会话 ID 查询，按时间正序 */
    List<ChatMessage> findBySessionIdOrderByCreateTimeAsc(String sessionId);

    /** 删除某会话的所有消息 */
    void deleteBySessionId(String sessionId);
}
