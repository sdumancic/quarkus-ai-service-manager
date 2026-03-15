package com.prevelio.rag;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class Agent {

    private final Assistant assistant;

    @Inject
    public Agent(EmbeddingStore<TextSegment> embeddingStore,
            com.prevelio.appointment.application.service.AppointmentAppService appointmentAppService,
            com.prevelio.customer.application.service.CustomerService customerAppService,
            com.prevelio.service.application.service.ServiceItemAppService serviceAppService,
            com.prevelio.vehicle.application.service.VehicleAppService vehicleAppService,
            @ConfigProperty(name = "openai.api.key") String apiKey) {

        Tools tools = new Tools(appointmentAppService, customerAppService, serviceAppService, vehicleAppService);
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.withMaxMessages(100);

        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName("gpt-4o-mini")
                        .build())
                .tools(tools)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();
    }

    public String chat(String customerUuid, String input) {
        return assistant.chat(UUID.fromString(customerUuid), input);
    }
}
