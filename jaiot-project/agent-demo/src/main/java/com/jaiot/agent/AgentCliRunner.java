package com.jaiot.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * 交互式命令行 Agent
 * <p>
 * 启动后直接在终端对话，输入 "exit" 退出。
 * 通过 --agent.cli=true 启用（默认不启用，避免干扰 REST 服务）。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "agent.cli", havingValue = "true")
@RequiredArgsConstructor
public class AgentCliRunner implements CommandLineRunner {

    private final SimpleAgent simpleAgent;

    @Override
    public void run(String... args) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   🦞 虾哥 Agent - 交互式控制台      ║");
        System.out.println("║   输入你的问题，输入 exit 退出       ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("🧑 你说: ");
                String input = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                    System.out.println("🦞 虾哥: 拜拜，下次聊！🦞");
                    break;
                }

                if (input.isEmpty()) {
                    continue;
                }

                long start = System.currentTimeMillis();
                String reply = simpleAgent.say(input);
                long elapsed = System.currentTimeMillis() - start;

                System.out.println("🦞 虾哥: " + reply);
                System.out.printf("   ⏱ %.1fs\n", elapsed / 1000.0);
                System.out.println();
            }
        }
    }
}
