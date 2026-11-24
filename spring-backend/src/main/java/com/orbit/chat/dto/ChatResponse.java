package com.orbit.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.chat.service.ChatHistory.Summary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String conversationId;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Summary conversation;
}
