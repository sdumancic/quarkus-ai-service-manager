package com.prevelio.rag.infrastructure;

import com.prevelio.rag.application.Agent;

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.PathParam;
import io.quarkus.websockets.next.WebSocket;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@WebSocket(path = "/chat/{customerUuid}")
@Slf4j
public class AgentWebSocket {

    private final Agent agent;

    @Inject
    public AgentWebSocket(Agent agent) {
        this.agent = agent;
    }

    @OnTextMessage
    public String onMessage(@PathParam("customerUuid") String customerUuid, String message) {
        log.info("Received message from customer {}: {}", customerUuid, message);
        String response = agent.chat(customerUuid, message);
        log.info("Sending response to customer {}: {}", customerUuid, response);
        return response;
    }
}
