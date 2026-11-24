package com.orbit.chat.service;

import com.orbit.chat.dto.ChatRequest;
import com.orbit.chat.dto.ChatResponse;
import com.orbit.response.Response;

public interface ChatService {

    boolean isAvailable();

    Response<ChatResponse> sendMessage(ChatRequest request, long userId);
}
