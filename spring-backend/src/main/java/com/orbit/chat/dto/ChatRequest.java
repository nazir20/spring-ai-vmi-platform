package com.orbit.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    @Size(max = 36)
    private String conversationId;

    @Positive
    private Long siloId;

    @Positive
    private Long afterTurn;

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Use 2,000 characters or fewer")
    private String message;
}
