package bll;

import java.time.LocalDateTime;

public class Message {
    private int messageId;
    private Integer groupId;
    private Integer conversationId;
    private String sender;
    private String content;
    private LocalDateTime sentAt;

    public Message(int messageId, Integer groupId, Integer conversationId, String sender, String content, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.groupId = groupId;
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

    // Getters and setters
    public int getMessageId() {
        return messageId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    @Override
    public String toString() {
        return sender + ": " + content;
    }
}
