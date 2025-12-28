package com.bytedance.wechat_moments;

import android.net.Uri;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class MomentsItem {
    // 原有字段（背景、头像、用户名、内容、图片）
    private int bgResId;
    private int avatarResId;
    private String username;
    private String content;
    private List<Uri> images;

    // 新增：点赞用户列表（存储点赞者的用户名）
    private List<String> likeUsers = new ArrayList<>();

    public MomentsItem(int bgResId, int avatarResId, String username, String content, List<Uri> images) {
        this.bgResId = bgResId;
        this.avatarResId = avatarResId;
        this.username = username;
        this.content = content;
        this.images = images;
    }

    // Getter & Setter
    public int getBgResId() { return bgResId; }
    public int getAvatarResId() { return avatarResId; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public List<Uri> getImages() { return images; }
    public List<String> getLikeUsers() { return likeUsers; }

    // 新增：添加点赞用户（去重）
    public boolean addLikeUser(String userName) {
        if (!likeUsers.contains(userName)) {
            likeUsers.add(userName);
            return true; // 添加成功
        }
        return false; // 已点赞过
    }

    // 新增：移除点赞用户
    public boolean removeLikeUser(String userName) {
        return likeUsers.remove(userName);
    }

    // 核心：重写equals方法，对比内容而非引用
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MomentsItem that = (MomentsItem) o;
        return bgResId == that.bgResId &&
                avatarResId == that.avatarResId &&
                Objects.equals(username, that.username) &&
                Objects.equals(content, that.content) &&
                Objects.equals(images, that.images) &&
                Objects.equals(likeUsers, that.likeUsers);
    }

    // 配套：重写hashCode
    @Override
    public int hashCode() {
        return Objects.hash(bgResId, avatarResId, username, content, images, likeUsers);
    }
}