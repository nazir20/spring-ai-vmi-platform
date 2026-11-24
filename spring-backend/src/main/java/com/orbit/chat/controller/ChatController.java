package com.orbit.chat.controller;

import com.orbit.chat.dto.ChatRequest;
import com.orbit.chat.dto.ChatResponse;
import com.orbit.chat.service.ChatHistory;
import com.orbit.chat.service.ChatHistory.ConversationChangedException;
import com.orbit.chat.service.ChatService;
import com.orbit.chat.service.ProviderUnavailableException;
import com.orbit.response.Response;
import com.orbit.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatHistory history;

    @GetMapping("/status")
    public Response<Boolean> status() {
        return Response.<Boolean>builder().statusCode(200).data(chatService.isAvailable()).build();
    }

    @GetMapping("/conversations")
    public Response<ChatHistory.Page> recent(@AuthenticationPrincipal AuthUser user,
                                            @RequestParam(required = false) String before) {
        return Response.<ChatHistory.Page>builder().statusCode(200)
                .data(history.recent(user.getUser().getId(), before)).build();
    }

    @GetMapping("/conversations/{id}")
    public Response<ChatHistory.Transcript> conversation(@AuthenticationPrincipal AuthUser user, @PathVariable String id,
                                                        @RequestParam(required = false) Long before) {
        return Response.<ChatHistory.Transcript>builder().statusCode(200)
                .data(history.transcript(user.getUser().getId(), id, before)).build();
    }

    @PostMapping
    public ResponseEntity<Response<ChatResponse>> sendMessage(@Valid @RequestBody ChatRequest request,
                                                              @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(chatService.sendMessage(request, user.getUser().getId()));
    }

    @ExceptionHandler(ConversationChangedException.class)
    public ResponseEntity<Response<Void>> conversationChanged(ConversationChangedException exception) {
        return failure(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<Response<Void>> providerUnavailable(ProviderUnavailableException exception) {
        return failure(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> invalidMessage(MethodArgumentNotValidException exception) {
        return failure(HttpStatus.BAD_REQUEST, "Check the chat details and use a message of 1 to 2,000 characters.");
    }

    private ResponseEntity<Response<Void>> failure(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Response.<Void>builder()
                .statusCode(status.value()).message(message).build());
    }
}
