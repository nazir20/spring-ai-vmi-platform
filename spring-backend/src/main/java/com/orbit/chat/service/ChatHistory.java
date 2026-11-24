package com.orbit.chat.service;

import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistory {
    private static final int PAGE_SIZE = 30;
    private static final int CONTEXT_TURNS = 8;
    private static final int TITLE_LENGTH = 120;
    private static final String SELECT_RECENT = """
            SELECT id, title, updated_at FROM chat_conversations
            WHERE owner_id = :ownerId
            """;
    private static final String ORDER_RECENT = " ORDER BY updated_at DESC, id DESC LIMIT :limit";
    private static final RowMapper<Summary> SUMMARY = (row, index) -> summary(row);
    private final JdbcClient jdbc;

    public record Summary(UUID id, String title, Instant updatedAt) {}
    public record Page(List<Summary> conversations, String nextCursor) {}
    public record Turn(long number, String question, String answer) {}
    public record Transcript(Summary conversation, List<Turn> turns, Long nextBefore) {}
    record PromptHistory(UUID id, long version, List<Turn> turns) {}

    public Page recent(long ownerId, String before) {
        Cursor cursor = before == null ? null : parseCursor(before);
        String cursorFilter = cursor == null ? "" : " AND (updated_at, id) < (:updatedAt, :conversationId)";
        var query = jdbc.sql(SELECT_RECENT + cursorFilter + ORDER_RECENT)
                .param("ownerId", ownerId)
                .param("limit", PAGE_SIZE + 1);
        if (cursor != null) {
            query.param("updatedAt", cursor.updatedAt().atOffset(ZoneOffset.UTC))
                    .param("conversationId", cursor.id());
        }
        List<Summary> rows = query.query(SUMMARY).list();
        if (rows.size() <= PAGE_SIZE) return new Page(rows, null);
        List<Summary> visible = rows.subList(0, PAGE_SIZE);
        var last = visible.get(visible.size() - 1);
        return new Page(visible, last.updatedAt() + "|" + last.id());
    }

    @Transactional(readOnly = true)
    public Transcript transcript(long ownerId, String id, Long before) {
        if (before != null && before < 1) throw new BadRequestException("Invalid message history cursor.");
        var chat = find(ownerId, id);
        long upperBound = before == null ? chat.version() : Math.min(chat.version(), before - 1);
        var turns = readTurns(chat.summary().id(), upperBound, PAGE_SIZE + 1);
        var page = turns.size() > PAGE_SIZE ? turns.subList(0, PAGE_SIZE) : turns;
        Long nextBefore = turns.size() > PAGE_SIZE ? page.get(page.size() - 1).number() : null;
        return new Transcript(chat.summary(), chronological(page), nextBefore);
    }

    @Transactional(readOnly = true)
    public PromptHistory context(long ownerId, String id) {
        if (id == null || id.isBlank()) return new PromptHistory(UUID.randomUUID(), 0, List.of());
        var chat = find(ownerId, id);
        return new PromptHistory(chat.summary().id(), chat.version(),
                chronological(readTurns(chat.summary().id(), chat.version(), CONTEXT_TURNS)));
    }

    @Transactional
    public Summary save(long ownerId, PromptHistory chat, String question, String answer) {
        Summary saved = chat.version() == 0
                ? createConversation(ownerId, chat.id(), title(question))
                : advanceConversation(ownerId, chat);
        jdbc.sql("""
                INSERT INTO chat_turns (conversation_id, turn_number, question, answer)
                VALUES (:conversationId, :turnNumber, :question, :answer)
                """)
                .param("conversationId", chat.id())
                .param("turnNumber", chat.version() + 1)
                .param("question", question)
                .param("answer", answer)
                .update();
        return saved;
    }

    private Summary createConversation(long ownerId, UUID conversationId, String title) {
        return jdbc.sql("""
                INSERT INTO chat_conversations (id, owner_id, title, turn_count)
                VALUES (:conversationId, :ownerId, :title, 1)
                RETURNING id, title, updated_at
                """)
                .param("ownerId", ownerId)
                .param("conversationId", conversationId)
                .param("title", title)
                .query(SUMMARY)
                .single();
    }

    private Summary advanceConversation(long ownerId, PromptHistory chat) {
        return jdbc.sql("""
                UPDATE chat_conversations
                SET turn_count = turn_count + 1, updated_at = GREATEST(updated_at, clock_timestamp())
                WHERE owner_id = :ownerId AND id = :conversationId AND turn_count = :expectedTurnCount
                RETURNING id, title, updated_at
                """)
                .param("ownerId", ownerId)
                .param("conversationId", chat.id())
                .param("expectedTurnCount", chat.version())
                .query(SUMMARY)
                .optional()
                .orElseThrow(ConversationChangedException::new);
    }

    private record StoredConversation(Summary summary, long version) {}
    private record Cursor(Instant updatedAt, UUID id) {}

    private static List<Turn> chronological(List<Turn> latestFirst) {
        var turns = new ArrayList<>(latestFirst);
        Collections.reverse(turns);
        return turns;
    }

    private StoredConversation find(long ownerId, String id) {
        return jdbc.sql("""
                SELECT id, title, updated_at, turn_count
                FROM chat_conversations
                WHERE owner_id = :ownerId AND id = :conversationId
                """)
                .param("ownerId", ownerId)
                .param("conversationId", parseId(id))
                .query((row, index) -> new StoredConversation(summary(row), row.getLong("turn_count")))
                .optional()
                .orElseThrow(ChatHistory::missing);
    }

    private List<Turn> readTurns(UUID id, long upperBound, int limit) {
        return jdbc.sql("""
                SELECT turn_number, question, answer
                FROM chat_turns
                WHERE conversation_id = :conversationId AND turn_number <= :upperBound
                ORDER BY turn_number DESC
                LIMIT :limit
                """)
                .param("conversationId", id)
                .param("upperBound", upperBound)
                .param("limit", limit)
                .query((row, index) -> new Turn(
                        row.getLong("turn_number"), row.getString("question"), row.getString("answer")))
                .list();
    }

    private static Summary summary(ResultSet row) throws SQLException {
        return new Summary(row.getObject("id", UUID.class), row.getString("title"),
                row.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw missing();
        }
    }

    private static Cursor parseCursor(String value) {
        try {
            int divider = value.indexOf('|');
            if (divider > 0 && value.length() <= 80) {
                return new Cursor(Instant.parse(value.substring(0, divider)), UUID.fromString(value.substring(divider + 1)));
            }
        } catch (java.time.format.DateTimeParseException | IllegalArgumentException ex) {
            throw new BadRequestException("Invalid conversation history cursor.");
        }
        throw new BadRequestException("Invalid conversation history cursor.");
    }

    private static String title(String message) {
        String line = message.replaceAll("(?U)\\s+", " ").strip();
        if (line.codePointCount(0, line.length()) <= TITLE_LENGTH) return line;
        return line.substring(0, line.offsetByCodePoints(0, TITLE_LENGTH - 1)).stripTrailing() + "…";
    }

    private static NotFoundException missing() {
        return new NotFoundException("This chat is unavailable.");
    }

    public static class ConversationChangedException extends RuntimeException {
        public ConversationChangedException() {
            super("This chat changed in another tab. Reload it before sending again.");
        }
    }
}
