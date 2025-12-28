package com.bytedance.wechat_moments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MomentsActivity extends AppCompatActivity {
    private SwipeRefreshLayout srlRefresh;
    private RecyclerView rvMoments;
    private MomentsAdapter adapter;
    private MomentsDataManager dataManager;
    private boolean isLoading = false; // 防止重复加载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moments);

        bindTopBar();
        dataManager = MomentsDataManager.getInstance();
        initView();
        initRefresh();
        initLoadMore();
    }

    // 绑定返回+摄像头按钮
    private void bindTopBar() {
        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 摄像头按钮（跳转发布页面）
        ImageView ivCamera = findViewById(R.id.iv_camera);
        ivCamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class); // 替换为你的发布页面
            startActivity(intent);
        });

        // 绑定个人用户名（可选：从用户信息中读取）
        TextView tvMyUsername = findViewById(R.id.tv_my_username);
        tvMyUsername.setText("云边");
    }
    private void initView() {
        srlRefresh = findViewById(R.id.srl_refresh);
        rvMoments = findViewById(R.id.rv_moments);
        rvMoments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MomentsAdapter(dataManager.getMomentsList());
        rvMoments.setAdapter(adapter);
    }

    // 下拉刷新逻辑
    private void initRefresh() {
        srlRefresh.setOnRefreshListener(() -> {
            // 1. 刷新前：保存当前列表的快照（复制为新列表，避免引用冲突）
            List<MomentsItem> oldList = new ArrayList<>(adapter.getCurrentList());

            // 2. 模拟网络请求延迟（实际项目中替换为真实接口请求）
            new Handler().postDelayed(() -> {
                // 3. 获取最新的朋友圈列表
                List<MomentsItem> newList = dataManager.getMomentsList();

                // 4. 对比新旧列表：仅当内容不同时才更新
                if (!newList.equals(oldList)) {
                    // 有新内容：更新适配器
                    adapter.updateData(newList);
                }
                // 5. 无论是否更新，都关闭刷新状态
                srlRefresh.setRefreshing(false);
            }, 1000);
        });
    }

    // 上拉加载更多逻辑
    private void initLoadMore() {
        rvMoments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                // 列表滑到底部
                if (layoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1
                        && !isLoading) {
                    isLoading = true;
                    // 模拟加载更多
                    new Handler().postDelayed(() -> {
                        dataManager.loadMoreHistory();
                        adapter.addMoreData(dataManager.getMomentsList().subList(
                                adapter.getItemCount(), dataManager.getMomentsList().size()
                        ));
                        isLoading = false;
                    }, 1000);
                }
            }
        });
    }
}