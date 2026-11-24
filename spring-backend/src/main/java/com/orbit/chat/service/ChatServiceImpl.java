package com.orbit.chat.service;

import com.orbit.chat.config.ChatPromptConfig;
import com.orbit.chat.dto.ChatRequest;
import com.orbit.chat.dto.ChatResponse;
import com.orbit.chat.memory.ConversationMemory;
import com.orbit.exception.BadRequestException;
import com.orbit.mcp.tools.*;
import com.orbit.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationMemory conversationMemory;
    private final ChatHistory chatHistory;
    private final SiloChatContext siloContext;
    private final Object[] readOnlyTools;
    private final boolean available;

    public ChatServiceImpl(ChatClient.Builder chatClient, ConversationMemory conversationMemory, ChatHistory chatHistory,
                           SiloChatContext siloContext, SiloTools siloTools, AlertTools alertTools,
                           ConsumptionTools consumptionTools, CustomerTools customerTools, OutstationTools outstationTools,
                           ProductTools productTools, PurchaseOrderTools purchaseOrderTools, ShipmentTools shipmentTools,
                           SupplierTools supplierTools, SiloMeasurementTools siloMeasurementTools,
                           @Value("${spring.ai.openai.chat.api-key:${spring.ai.openai.api-key:}}") String apiKey) {
        this.chatClient = chatClient.build();
        this.conversationMemory = conversationMemory;
        this.chatHistory = chatHistory;
        this.siloContext = siloContext;
        this.readOnlyTools = new Object[] { siloTools, alertTools, consumptionTools, customerTools, outstationTools,
                productTools, purchaseOrderTools, shipmentTools, supplierTools, siloMeasurementTools };
        this.available = !apiKey.isBlank() && !Set.of("YOUR_API_KEY", "local-demo-placeholder").contains(apiKey);
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public Response<ChatResponse> sendMessage(ChatRequest request, long userId) {
        if (!available) throw new BadRequestException("Add your KI:connect key to Spring and restart the backend.");

        var question = new UserMessage(request.getMessage().strip());
        Long siloId = request.getSiloId();
        if (siloId == null) {
            var chat = chatHistory.context(userId, request.getConversationId());
            if (request.getAfterTurn() != null && request.getAfterTurn() != chat.version()) {
                throw new ChatHistory.ConversationChangedException();
            }
            var messages = new ArrayList<Message>();
            for (var turn : chat.turns()) {
                messages.add(new UserMessage(turn.question()));
                messages.add(new AssistantMessage(turn.answer()));
            }
            String answer = answer(messages, question, null);
            var summary = chatHistory.save(userId, chat, question.getText(), answer);
            return response(new ChatResponse(summary.id().toString(), answer, summary));
        }

        try (var conversation = conversationMemory.begin(request.getConversationId(), userId, siloId)) {
            String answer = answer(conversation.history(), question, siloContext.records(siloId));
            conversation.remember(question, new AssistantMessage(answer));
            return response(new ChatResponse(conversation.id(), answer, null));
        }
    }

    private String answer(List<Message> history, UserMessage question, String records) {
        var messages = new ArrayList<>(history);
        messages.add(question);
        var prompt = chatClient.prompt().messages(messages);
        if (records == null) {
            prompt.system(ChatPromptConfig.SYSTEM_PROMPT).tools(readOnlyTools);
        } else {
            prompt.system(spec -> spec.text("{instructions}\nSilo records in JSON:\n{records}")
                    .param("instructions", ChatPromptConfig.SILO_PROMPT).param("records", records));
        }
        String answer;
        try {
            answer = prompt.call().content();
        } catch (RuntimeException ex) {
            throw new ProviderUnavailableException();
        }
        if (answer == null || answer.isBlank()) throw new ProviderUnavailableException();
        return answer;
    }

    private Response<ChatResponse> response(ChatResponse reply) {
        return Response.<ChatResponse>builder().statusCode(200).message("Message processed successfully").data(reply).build();
    }
}
