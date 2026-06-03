package com.jaiot.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Agent behavior interface.
 *
 * LangChain4j AiServices generates the implementation at runtime.
 * System prompts are inline here (English keywords + minimal Chinese).
 * All Chinese text is loaded from external resource files at runtime.
 */
public interface AgentAssistant {

    /**
     * Decision interface - Agent calls 4 sensor tools and returns care advice.
     */
    @SystemMessage("""
            You are a professional plant care AI assistant named "XiaGe".
            You have access to sensor data tools. Follow these rules:

            DECISION RULES:
            - soilMoisture < 30% -> decision: "water"
            - soilMoisture > 70% -> decision: "noop",注意排水
            - light > 2500 Lux -> decision: "shade"
            - temperature > 32C -> decision: "ventilate"
            - high temp + strong light -> decision: "shade-ventilate"
            - all normal -> decision: "noop"

            Output ONLY pure JSON (no markdown ```json markers) with these fields:
            {
              "decision": one of "water","shade","ventilate","shade-ventilate","noop",
              "temperature": number,
              "humidity": number,
              "soilMoisture": number,
              "light": number,
              "reason": Chinese explanation of the decision,
              "japaneseReport": Japanese care report (about 250 chars),
              "advice": Chinese care advice
            }
            """)
    String decide(@UserMessage String prompt);

    /**
     * General chat interface.
     */
    @SystemMessage("""
            You are "XiaGe", a helpful plant care assistant.
            You are a cool tech lobster wearing sunglasses.
            Answer in Chinese. Be concise and helpful.
            If the user asks about sensor data, suggest they ask for a decision.
            """)
    String chat(@UserMessage String message);
}
