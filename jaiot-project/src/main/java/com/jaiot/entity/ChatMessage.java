package com.jaiot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 *
 * 存储用户与 Agent 的对话历史，支持多会话管理。
 */
@Entity
@Table(name = "chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会话 ID */
    @Column(nullable = false, length = 100)
    private String sessionId;

    /** 角色：user / assistant */
    @Column(nullable = false, length = 20)
    private String role;

    /** 消息内容 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 消息时间 */
    @Column(nullable = false)
    private LocalDateTime createTime;
}
