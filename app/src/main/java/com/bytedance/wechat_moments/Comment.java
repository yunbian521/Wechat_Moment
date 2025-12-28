package com.bytedance.wechat_moments;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private String replyTo; // 回复给谁

    public Comment() {}

    public Comment(String userId, String userName, String content) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getReplyTo() { return replyTo; }
    public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
}