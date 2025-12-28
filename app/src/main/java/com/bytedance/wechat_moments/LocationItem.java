package com.bytedance.wechat_moments;
public class LocationItem {
    private String title;
    private String address;
    private boolean isSelected;
    private int type; // 0: 不显示位置, 1: 省份, 2: 市, 3: 具体位置

    public LocationItem(String title, String address, int type) {
        this.title = title;
        this.address = address;
        this.type = type;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
}