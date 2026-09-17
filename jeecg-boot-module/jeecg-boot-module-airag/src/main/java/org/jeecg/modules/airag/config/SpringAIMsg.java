package org.jeecg.modules.airag.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpringAIMsg {
    MessageType messageType;
    String text;
    Map<String, Object> metadata;
    List<AssistantMessage.ToolCall> toolCalls;

    public SpringAIMsg(Message message) {
        this.messageType = message.getMessageType();
        this.text = message.getText();
        this.metadata = message.getMetadata();
        if (message instanceof AssistantMessage am) {
            this.toolCalls = am.getToolCalls();
        }
    }

    public Message toMessage() {
        // 空值安全处理，避免构造 Message 时抛出 NPE
        Map<String, Object> meta = metadata != null ? metadata : Map.of();
        List<AssistantMessage.ToolCall> tools = toolCalls != null ? toolCalls : List.of();
        // Spring AI 1.1.0 中 UserMessage/AssistantMessage 的多参构造器是 protected，必须使用 builder
        // 构建
        return switch (messageType) {
            case SYSTEM -> new SystemMessage(text);
            case USER -> UserMessage.builder().text(text).metadata(meta).build();
            case ASSISTANT -> AssistantMessage.builder().content(text).properties(meta).toolCalls(tools).build();
            default -> throw new IllegalArgumentException("Unsupported message type: " + messageType);
        };
    }
}
