package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ChatService {

    @SystemMessage("You are a helpful assistant. Answer concisely.")
    String chat(@UserMessage String message);
}
