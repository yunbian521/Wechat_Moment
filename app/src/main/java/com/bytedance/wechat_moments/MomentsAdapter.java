package com.bytedance.wechat_moments;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.widget.PopupMenu;


public class MomentsAdapter extends RecyclerView.Adapter<MomentsAdapter.MomentsViewHolder> {
    private List<MomentsItem> momentsList;
    private final Object lock = new Object();
    // 当前登录用户的用户名（示例：替换为实际用户名）
    private String currentUserName = "云边";

    public MomentsAdapter(List<MomentsItem> momentsList) {
        this.momentsList = momentsList;
    }
    @NonNull
    @Override
    public MomentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_moments, parent, false);
        return new MomentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MomentsViewHolder holder, int position) {

        MomentsItem item = momentsList.get(position);
        // 1. 绑定头像、用户名
        holder.ivAvatar.setImageResource(item.getAvatarResId());
        holder.tvUsername.setText(item.getUsername());
        // 2. 绑定文字内容
        // 2. 设置朋友圈内容（放大文字）
        holder.tvContent.setText(item.getContent());
        holder.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // 强制设置16sp
        holder.tvContent.setText(item.getContent());
        // 3. 绑定照片列表

        bindImages(holder.llImages, item.getImages());
        // 4. 绑定点赞栏（根据点赞列表显示/隐藏）
        bindLikeBar(holder.llLikeBar, holder.tvLikeUsers, item);
        // 5. 绑定“···”按钮的弹出菜单
        bindMoreMenu(holder.itemView.getContext(), holder.ivMore, item, position);
    }

    // 绑定照片列表
    private void bindImages(LinearLayout container, List<Uri> images) {
        container.removeAllViews();
        if (images == null || images.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);

        Context context = container.getContext();
        int imageSizeDp = 100;
        int marginDp = 0; // 可调整为1~3dp（控制紧凑度）
        int imageSizePx = dp2px(context, imageSizeDp);
        int marginPx = dp2px(context, marginDp);

        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setColumnCount(3); // 一行3张
        gridLayout.setRowCount(3);    // 最多3行
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        gridLayout.setLayoutParams(gridParams);

        int maxCount = Math.min(images.size(), 9);
        for (int i = 0; i < maxCount; i++) {
            Uri uri = images.get(i);
            ImageView iv = new ImageView(context);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSizePx;
            params.height = imageSizePx;

            // 正确逻辑：仅给“非第一列”加左侧间距，“非第一行”加顶部间距
            int left = (i % 3 != 0) ? marginPx : 0; // 不是第一列 → 左侧加间距
            int top = (i / 3 != 0) ? marginPx : 0;  // 不是第一行 → 顶部加间距
            params.setMargins(left, top, 0, 0); // 仅左侧和顶部留间距

            params.rowSpec = GridLayout.spec(i / 3);
            params.columnSpec = GridLayout.spec(i % 3);
            iv.setLayoutParams(params);

            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setAdjustViewBounds(true);
            iv.setImageURI(uri);

            gridLayout.addView(iv);
        }
        container.addView(gridLayout);
    }

    private int dp2px(Context context, float dp) {
        if (context == null) return (int) dp;
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    // 绑定点赞栏（显示爱心+用户名列表）
    private void bindLikeBar(LinearLayout likeBar, TextView likeUsersTv, MomentsItem item) {
        List<String> likeUsers = item.getLikeUsers();
        if (likeUsers.isEmpty()) {
            likeBar.setVisibility(View.GONE);
            return;
        }
        likeBar.setVisibility(View.VISIBLE);
        // 拼接用户名（用“,”分隔）
        StringBuilder usersStr = new StringBuilder();
        for (int i = 0; i < likeUsers.size(); i++) {
            usersStr.append(likeUsers.get(i));
            if (i != likeUsers.size() - 1) {
                usersStr.append("，");
            }
        }
        likeUsersTv.setText(usersStr.toString());
    }

    // 绑定“···”按钮的弹出菜单
    private void bindMoreMenu(Context context, ImageView ivMore, MomentsItem item, int position) {
        ivMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.NO_GRAVITY, 0, R.style.CustomPopupMenu);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_like, popupMenu.getMenu());

            // 1. 反射强制显示图标（即使异常也不阻断后续逻辑）
            try {
                Field field = popupMenu.getClass().getDeclaredField("mPopup");
                field.setAccessible(true);
                Object menuPopupHelper = field.get(popupMenu);
                Class<?> helperClass = Class.forName("com.android.internal.view.menu.MenuPopupHelper");
                Method method = helperClass.getDeclaredMethod("setForceShowIcon", boolean.class);
                method.setAccessible(true);
                method.invoke(menuPopupHelper, true);
            } catch (Exception e) {
                e.printStackTrace(); // 仅打印异常，不中断
            }

            // 2. 确保点击事件正确绑定
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                // 匹配菜单项的id（必须和menu_like.xml中的id一致）
                if (menuItem.getItemId() == R.id.menu_like) {
                    // 执行点赞逻辑（示例）
                    synchronized (lock) {
                        boolean isAdded = item.addLikeUser(currentUserName);
                        if (isAdded) {
                            notifyItemChanged(position);
                        }
                    }
                    return true; // 返回true表示消费了点击事件
                }
                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return momentsList.size();
    }

    // 更新列表数据（下拉刷新用）
    public void updateData(List<MomentsItem> newList) {
        momentsList.clear();
        momentsList.addAll(newList);
        notifyDataSetChanged();
    }

    // 添加更多数据（上拉加载用）
    public void addMoreData(List<MomentsItem> moreList) {
        if (moreList == null || moreList.isEmpty()) return;
        // 强制复制为新ArrayList，彻底避免SubList
        List<MomentsItem> safeList = new ArrayList<>(moreList);
        synchronized (lock) {
            int startPos = momentsList.size();
            momentsList.addAll(safeList);
            notifyItemRangeInserted(startPos, safeList.size());
        }
    }
    // ViewHolder：适配新控件
    static class MomentsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        ImageView ivMore;
        TextView tvContent;
        LinearLayout llImages;
        LinearLayout llLikeBar;
        TextView tvLikeUsers;
        public MomentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            ivMore = itemView.findViewById(R.id.iv_more);
            tvContent = itemView.findViewById(R.id.tv_content);
            llImages = itemView.findViewById(R.id.ll_images);
            llLikeBar = itemView.findViewById(R.id.ll_like_bar);
            tvLikeUsers = itemView.findViewById(R.id.tv_like_users);
        }
    }
    public List<MomentsItem> getCurrentList() {
        return new ArrayList<>(momentsList);
    }
}