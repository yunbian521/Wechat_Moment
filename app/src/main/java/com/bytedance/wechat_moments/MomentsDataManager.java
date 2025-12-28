package com.bytedance.wechat_moments;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class MomentsDataManager {
    private static MomentsDataManager instance;
    // 朋友圈列表（包含自己和朋友的历史动态）
    private List<MomentsItem> momentsList;
    private MomentsDataManager() {
        momentsList = new ArrayList<>();
        // 初始化朋友的历史朋友圈（模拟数据）
        initHistoryMoments();
    }

    public static MomentsDataManager getInstance() {
        if (instance == null) {
            instance = new MomentsDataManager();
        }
        return instance;
    }

    // 初始化朋友的历史动态
    private void initHistoryMoments() {

        // 朋友1的动态
        momentsList.add(new MomentsItem(
                R.drawable.moments_bg_friend1,
                R.drawable.avatar_friend1,
                "懒羊羊",
                "跪求来帮我朋友圈第二个点一下赞，谢谢。",
                null // 无图片
        ));
        // 朋友2的动态
        momentsList.add(new MomentsItem(
                R.drawable.moments_bg_friend2,
                R.drawable.avatar_friend2,
                "李大伟",
                "绽放的刹那全世界都疯狂怒视着漆黑的苍穹，奋力疾飞只管留下一秒的回忆。",
                new ArrayList<Uri>()
                {{
                    String packageName = "com.bytedance.wechat_moments"; // 包名单独定义（更易读）
                    Uri imageUri = Uri.parse("android.resource://" + packageName + "/" + R.drawable.moments_bg_friend2);
                    add(imageUri);
                }}
        ));
        momentsList.add(new MomentsItem(
                R.drawable.moments_bg_friend2,
                R.drawable.avatar_friend3,
                "利川",
                "继续加油！",
                new ArrayList<Uri>()
                {{
                    String packageName = "com.bytedance.wechat_moments"; // 包名单独定义（更易读）
                    Uri imageUri = Uri.parse("android.resource://" + packageName + "/" + R.drawable.moments_bg_friend3);
                    add(imageUri);
                }}
        ));
    }

    // 添加新发表的朋友圈（插入到列表顶部）
    public void addNewMoments(MomentsItem item) {
        momentsList.add(0, item); // 新动态显示在顶部
    }

    // 获取朋友圈列表
    public List<MomentsItem> getMomentsList() {
        return momentsList;
    }

    // 加载更多历史动态（模拟）
    public void loadMoreHistory() {
        // 模拟添加更多朋友的历史动态
        momentsList.add(new MomentsItem(
                R.drawable.moments_bg_friend3,
                R.drawable.avatar_friend3,
                "张三",
                "今天天气不错！",
                null
        ));
    }
}