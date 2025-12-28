package com.bytedance.wechat_moments;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Geocoder;
import android.location.Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private List<LocationItem> locationList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCurrentLocation;
    private String selectedCity = "";
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_page);

        initView();
        initData();
        initLocationService();
    }

    private void initView() {
        recyclerView = findViewById(R.id.rv_location_list);
        tvCurrentLocation = findViewById(R.id.tv_current_location);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        // 获取之前保存的选中位置
        selectedCity = getIntent().getStringExtra("selected_city");
        selectedAddress = getIntent().getStringExtra("selected_address");

        // 创建列表数据
        locationList.clear();

        // 添加"不显示位置"选项
        LocationItem noLocation = new LocationItem("不显示位置", "", 0);
        locationList.add(noLocation);

        // 模拟厦门市的数据
        LocationItem xiamenCity = new LocationItem("厦门", "福建省", 2);
        locationList.add(xiamenCity);

        // 添加具体位置
        locationList.add(new LocationItem("软件园二期", "福建省厦门市思明区观日路4号", 3));
        locationList.add(new LocationItem("厦门保立网络技术有限公司", "福建省厦门市思明区软件园2期观日路56号4层和5层", 3));
        locationList.add(new LocationItem("厦门国际会议中心", "福建省厦门市思明区金晨路198号", 3));
        locationList.add(new LocationItem("星巴克(软件园店)", "福建省厦门市思明区软件园二期观日路54号地上1层107单元", 3));

        // 设置选中状态
        updateSelectedItem();

        adapter = new LocationAdapter(locationList, this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((item, position) -> {
            handleItemClick(item, position);
        });
    }

    private void updateSelectedItem() {
        // 如果是第一次进入，默认选中"不显示位置"
        boolean hasSelection = false;

        if (selectedAddress != null && !selectedAddress.isEmpty()) {
            // 查找匹配的具体位置
            for (int i = 0; i < locationList.size(); i++) {
                LocationItem item = locationList.get(i);
                if (item.getType() == 3 && item.getTitle().equals(selectedAddress)) {
                    item.setSelected(true);
                    hasSelection = true;
                    break;
                }
            }
        } else if (selectedCity != null && !selectedCity.isEmpty()) {
            // 查找匹配的城市
            for (int i = 0; i < locationList.size(); i++) {
                LocationItem item = locationList.get(i);
                if (item.getType() == 2 && item.getTitle().equals(selectedCity)) {
                    item.setSelected(true);
                    hasSelection = true;
                    break;
                }
            }
        }

        // 如果没有选中任何项目，默认选中"不显示位置"
        if (!hasSelection) {
            locationList.get(0).setSelected(true);
        }
    }

    private void handleItemClick(LocationItem item, int position) {
        Intent resultIntent = new Intent();

        switch (item.getType()) {
            case 0: // 不显示位置
                resultIntent.putExtra("selected_city", "");
                resultIntent.putExtra("selected_address", "");
                resultIntent.putExtra("display_text", "不显示位置");
                break;

            case 2: // 市
                resultIntent.putExtra("selected_city", item.getTitle());
                resultIntent.putExtra("selected_address", "");
                resultIntent.putExtra("display_text", item.getTitle());
                break;

            case 3: // 具体位置
                resultIntent.putExtra("selected_city", "厦门");
                resultIntent.putExtra("selected_address", item.getTitle());
                resultIntent.putExtra("display_text", "厦门 - " + item.getTitle());
                break;
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void initLocationService() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 获取当前位置
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                getAddressFromLocation(location);
                            } else {
                                tvCurrentLocation.setText("无法获取当前位置");
                            }
                        }
                    });
        } catch (SecurityException e) {
            tvCurrentLocation.setText("需要位置权限");
        }
    }

    private void getAddressFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String province = address.getAdminArea();
                String city = address.getLocality();
                String fullAddress = address.getAddressLine(0);

                String displayText = "当前位置: " + province + " " + city;
                if (fullAddress != null) {
                    displayText += "\n" + fullAddress;
                }

                tvCurrentLocation.setText(displayText);
            }
        } catch (Exception e) {
            tvCurrentLocation.setText("解析地址失败");
        }
    }
}