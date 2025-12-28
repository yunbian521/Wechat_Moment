package com.bytedance.wechat_moments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private List<LocationItem> locationList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LocationItem item, int position);
    }

    public LocationAdapter(List<LocationItem> locationList, Context context) {
        this.locationList = locationList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.loaction_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (locationList == null || position >= locationList.size()) {
            return; // 防止空指针
        }

        LocationItem item = locationList.get(position);

        if (item == null) {
            return;
        }

        holder.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "");
        holder.tvAddress.setText(item.getAddress() != null ? item.getAddress() : "");


        // 设置选中状态
        if (item.isSelected()) {
            //holder.tvTitle.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            //holder.tvAddress.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.tvRadioButton.setButtonTintList(ContextCompat.getColorStateList(context, android.R.color.holo_green_light));

        } else {
            //holder.tvTitle.setTextColor(context.getResources().getColor(android.R.color.black));
            //holder.tvAddress.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.tvRadioButton.setButtonTintList(ContextCompat.getColorStateList(context, android.R.color.black));
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public void updateSelectedPosition(int position) {
        // 重置所有选项的选中状态
        for (int i = 0; i < locationList.size(); i++) {
            locationList.get(i).setSelected(i == position);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAddress;

        RadioButton tvRadioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_location_name);
            tvAddress = itemView.findViewById(R.id.tv_location_address);
            tvRadioButton = itemView.findViewById(R.id.rb_selected);
        }
    }
}