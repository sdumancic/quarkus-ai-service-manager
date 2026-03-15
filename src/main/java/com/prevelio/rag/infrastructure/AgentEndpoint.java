package com.prevelio.rag.infrastructure;

import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.reactive.RestStreamElementType;

import com.prevelio.rag.application.Agent;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import com.prevelio.rag.application.AgUiEvent;
import com.prevelio.rag.application.ChatRequest;

@Path("/agent")
@Slf4j
@ApplicationScoped
public class AgentEndpoint {

    @Inject
    Agent agent;

    @POST
    @Path("/run")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<AgUiEvent> run(ChatRequest request) {

        return Multi.createFrom().emitter(emitter -> {

            new Thread(() -> {
                try {

                    log.info("Agent run started for {}", request.getCustomerUuid());

                    emitter.emit(new AgUiEvent("RUN_STARTED", null));

                    String response = agent.chat(
                            request.getCustomerUuid(),
                            request.getMessage());

                    emitter.emit(new AgUiEvent(
                            "TEXT_MESSAGE_CONTENT",
                            response));

                    emitter.emit(new AgUiEvent("RUN_FINISHED", null));

                    emitter.complete();

                } catch (Exception e) {
                    emitter.fail(e);
                }
            }).start();

        });
    }
}
