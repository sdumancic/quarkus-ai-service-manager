package com.prevelio.rag;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import java.util.UUID;

public interface Assistant {

    @SystemMessage("""
            You are a helpful store assistant with access to local documents and tools.
            When user starts chat you will receive his customerUuid. Based on this customerUuid you will search for customer information and use it to greet him.
            If you don't know the customer information, say you don't know. Don't make up an answer.
            When asked question you will search the documents for relevant information and use it to answer the question.
            If you don't know the answer, say you don't know. Don't make up an answer.
            """)
    String chat(@MemoryId UUID customerUuid, @UserMessage String userMessage);
}
