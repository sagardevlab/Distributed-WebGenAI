package com.sagardevlab.distributed_webgenai.intelligence_service.llm;

import com.sagardevlab.distributed_webgenai.common_lib.enums.ChatEventStatus;
import com.sagardevlab.distributed_webgenai.common_lib.enums.ChatEventType;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatEvent;
import com.sagardevlab.distributed_webgenai.intelligence_service.entity.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LlmResponseParserTest {

    private final LlmResponseParser parser = new LlmResponseParser();

    @Test
    void parsesMessagesToolLogsAndFileEditsInOrder() {
        String response = """
                <message phase="start">Let me look.</message>
                <tool args="src/App.tsx">Reading App.tsx...</tool>
                <file path="src/App.tsx">export default function App() { return null }</file>
                <message phase="completed">Done!</message>
                """;

        List<ChatEvent> events = parser.parseChatEvents(response, new ChatMessage());

        assertThat(events).extracting(ChatEvent::getType).containsExactly(
                ChatEventType.MESSAGE, ChatEventType.TOOL_LOG, ChatEventType.FILE_EDIT, ChatEventType.MESSAGE);
        assertThat(events).extracting(ChatEvent::getSequenceOrder).containsExactly(1, 2, 3, 4);
        assertThat(events.get(1).getMetadata()).isEqualTo("src/App.tsx");

        ChatEvent fileEdit = events.get(2);
        assertThat(fileEdit.getFilePath()).isEqualTo("src/App.tsx");
        assertThat(fileEdit.getStatus()).isEqualTo(ChatEventStatus.PENDING);
        assertThat(fileEdit.getContent()).isEqualTo("export default function App() { return null }");
    }

    @Test
    void stripsMarkdownFenceAroundFileContent() {
        String response = """
                <file path="src/App.tsx">```tsx
                const a = 1
                ```</file>""";

        List<ChatEvent> events = parser.parseChatEvents(response, new ChatMessage());

        assertThat(events).singleElement().extracting(ChatEvent::getContent).isEqualTo("const a = 1");
    }

    @Test
    void ignoresTextOutsideTags() {
        assertThat(parser.parseChatEvents("plain text without any tags", new ChatMessage())).isEmpty();
    }
}
