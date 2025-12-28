package com.bytedance.wechat_moments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final List<Uri> selectedImages = new ArrayList<>();
    private final String input_content = "";
    private PreviewAdapter previewAdapter;
    private EditText etDescription;
    private ImageButton btnQQ;

    private TextView TextView_location;


    private static final int REQUEST_CODE_LOCATION = 1001;
    private String selectedCity = "";
    private String selectedAddress = "";
    private final ActivityResultLauncher<String[]> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        // 申请持久访问权限（关键！）
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        selectedImages.add(uri); // 持久URI，刷新不失效
                    }
                    updatePreviewAndCover();
                }
            });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDescription = findViewById(R.id.et_description);//描述框

        LinearLayout itemLocationLayout = findViewById(R.id.item_location);
        LinearLayout itemRemindLayout = findViewById(R.id.item_remind);
        LinearLayout itemVisibleLayout = findViewById(R.id.item_visible);
        MaterialButton add_button = findViewById(R.id.btn_add_cover);

        add_button.setOnClickListener(v -> checkPermissionAndOpenAlbum());

        // 预览框（仅显示图片）
        RecyclerView rvPreview = findViewById(R.id.rv_preview);
        previewAdapter = new PreviewAdapter(selectedImages);
        //这段将RecyclerView变成横向滚动
        rvPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPreview.setAdapter(previewAdapter);  //rvPreview是RecyclerView预览框

        //长按照片交换位置和删除功能实现
        ViewGroup rootLayout = findViewById(R.id.activity_root_layout);
        //  绑定ItemTouchHelper（拖动排序+删除）
        PhotoItemTouchHelperCallback callback = new PhotoItemTouchHelperCallback(this, selectedImages, previewAdapter, rootLayout);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvPreview);

        findViewById(R.id.pre_see).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView_location = findViewById(R.id.tv_title_location);

        // 2. 设置点击监听
        // 恢复保存的状态
        if (savedInstanceState != null) {
            selectedCity = savedInstanceState.getString("selected_city", "");
            selectedAddress = savedInstanceState.getString("selected_address", "");
            updateButtonText();
        }

        itemLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationActivity();
            }
        });

        itemRemindLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        itemVisibleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        etDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) { // 输入框获取焦点（点击/触摸时触发）
                showSystemKeyboard(v); // 强制弹出系统键盘
            }
        });

        btnQQ = findViewById(R.id.QQ_space);
        btnQQ.setOnClickListener(v -> showTipDialog());

        // 设置发布按钮
        MaterialButton btnPublish = findViewById(R.id.look);
        btnPublish.setOnClickListener(v -> {
            // 1. 获取编辑的文本内容（示例：从EditText获取）
            String editContent = etDescription.getText().toString().trim();
            // 3. 封装当前用户的朋友圈数据（模拟：背景、头像用本地资源）
            MomentsItem myMoments = new MomentsItem(
                    R.drawable.wechat_background, // 朋友圈背景图（需在drawable中添加）
                    R.drawable.avatar_mine, // 自己的头像
                    "云边", // 自己的用户名
                    editContent,
                    selectedImages
            );

            // 4. 跳转到朋友圈界面，传递数据
            Intent intent = new Intent(this, MomentsActivity.class);
            // 传递当前发表的朋友圈（需实现Parcelable，这里简化用全局单例存储）
            MomentsDataManager.getInstance().addNewMoments(myMoments);
            startActivity(intent);
            finish(); // 关闭发表页面
        });

    }

private void openLocationActivity() {
    Intent intent = new Intent(this, LocationActivity.class);
    intent.putExtra("selected_city", selectedCity);
    intent.putExtra("selected_address", selectedAddress);
    startActivityForResult(intent, REQUEST_CODE_LOCATION);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
        selectedCity = data.getStringExtra("selected_city");
        selectedAddress = data.getStringExtra("selected_address");
        String displayText = data.getStringExtra("display_text");
        updateButtonText();
        // 保存选择到 SharedPreferences
        saveLocationPreference();
    }
}

private void updateButtonText() {
    if (selectedCity == null || selectedCity.isEmpty()) {
        // 不显示位置
        TextView_location.setText("所在位置");
        TextView_location.setTextColor(getResources().getColor(android.R.color.black));
    } else if (selectedAddress == null || selectedAddress.isEmpty()) {
        // 只选择了城市
        TextView_location.setText(selectedCity);
        TextView_location.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    } else {
        // 选择了具体位置
        TextView_location.setText(selectedCity + " - " + selectedAddress);
        TextView_location.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }
}

private void saveLocationPreference() {
    getSharedPreferences("location_prefs", MODE_PRIVATE)
            .edit()
            .putString("selected_city", selectedCity)
            .putString("selected_address", selectedAddress)
            .apply();
}

private void loadLocationPreference() {
    selectedCity = getSharedPreferences("location_prefs", MODE_PRIVATE)
            .getString("selected_city", "");
    selectedAddress = getSharedPreferences("location_prefs", MODE_PRIVATE)
            .getString("selected_address", "");
}

@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("selected_city", selectedCity);
    outState.putString("selected_address", selectedAddress);
}

    // 弹出“提示”对话框
    private void showTipDialog() {
        // 构建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置对话框标题
        builder.setTitle("提示");
        // 设置对话框内容（换行用\n）
        builder.setMessage("谁可以看选择私密时，不能同步到\nQQ空间");
        // 设置“确定”按钮及点击事件
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 点击“确定”后关闭对话框（默认行为，可添加额外逻辑）
            dialog.dismiss();
        });
        // 禁止点击对话框外部/返回键关闭（可选，根据需求）
        builder.setCancelable(false);
        // 显示对话框
        builder.show();
    }
    /**
     * 弹出系统软键盘（封装成工具方法，可复用）
     */
    private void showSystemKeyboard(View view) {
        // 获取系统输入法管理器
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            // 方式1：通用弹出（推荐，适配绝大多数机型）
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

            // 方式2：强制弹出（若方式1无效，替换此句）
            // imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    //检查权限并打开相册
    private void checkPermissionAndOpenAlbum() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;  //选择对应相册权限
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openAlbum();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, 100);
        }
    }
    //打开系统相册
    private void openAlbum() {
        /*
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*"); // 只选择图片
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 允许多选
        */

        // 直接启动相册选择（无需构建Intent）
        pickImagesLauncher.launch(new String[]{"image/*"});

    }
    //更新预览列表和封面
    @SuppressLint("NotifyDataSetChanged")
    private void updatePreviewAndCover() {
        // 更新封面（复用updateCover方法）
        previewAdapter.notifyDataSetChanged();
    }

}

// 初始化监听事件
