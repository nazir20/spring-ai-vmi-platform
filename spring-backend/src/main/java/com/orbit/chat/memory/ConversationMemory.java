package com.orbit.chat.memory;

import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ConversationMemory {
    private static final Duration LIFETIME = Duration.ofHours(2);
    private static final int MAX_CONVERSATIONS = 256;
    private static final int MAX_MESSAGES = 16;
    private final Map<String, Conversation> conversations = new LinkedHashMap<>(16, .75f, true);

    public synchronized Conversation begin(String id, long owner, Long siloId) {
        Instant cutoff = Instant.now().minus(LIFETIME);
        conversations.values().removeIf(chat -> !chat.lock.isLocked() && chat.lastUsed.isBefore(cutoff));

        if (id == null || id.isBlank()) {
            if (conversations.size() >= MAX_CONVERSATIONS) {
                var oldestIdle = conversations.values().stream().filter(chat -> !chat.lock.isLocked()).findFirst();
                if (oldestIdle.isEmpty()) throw new BadRequestException("Chat is busy. Try again in a moment.");
                conversations.remove(oldestIdle.get().id);
            }
            id = UUID.randomUUID().toString();
            conversations.put(id, new Conversation(id, owner, siloId));
        }

        Conversation chat = conversations.get(id);
        if (chat == null || chat.owner != owner || !Objects.equals(chat.siloId, siloId)) {
            throw new NotFoundException("Conversation unavailable. Choose New chat to start again.");
        }
        if (!chat.lock.tryLock()) throw new BadRequestException("This chat already has a reply in progress.");
        return chat;
    }

    public static final class Conversation implements AutoCloseable {
        private final String id;
        private final long owner;
        private final Long siloId;
        private final ReentrantLock lock = new ReentrantLock();
        private final List<Message> messages = new ArrayList<>();
        private volatile Instant lastUsed = Instant.now();

        private Conversation(String id, long owner, Long siloId) {
            this.id = id;
            this.owner = owner;
            this.siloId = siloId;
        }

        public String id() {
            return id;
        }

        public List<Message> history() {
            return List.copyOf(messages);
        }

        public void remember(Message question, Message answer) {
            messages.add(question);
            messages.add(answer);
            if (messages.size() > MAX_MESSAGES) messages.subList(0, 2).clear();
        }

        @Override
        public void close() {
            lastUsed = Instant.now();
            lock.unlock();
        }
    }
}
