package com.prevelio.rag.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
    private String message;
    private String customerUuid;
}
