package com.choicely.preassignmentproject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.choicely.preassignmentproject.data.ItemData;

import java.util.ArrayList;
import java.util.List;

public class ClothesInfoAdapter extends RecyclerView.Adapter<ClothesInfoAdapter.InfoViewHolder> {

    private static final String TAG = "ClothesInfoAdapter";

    private List<ItemData> items = new ArrayList<>();

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.store_item_content_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        ItemData itemData = items.get(position);
        holder.name.setText(itemData.getItemName());
        holder.manufacturer.setText(itemData.getManufacturer());
        holder.price.setText(String.format("%d€", itemData.getPrice()));
        List<String> colorList = itemData.getItemColor();
        String colors = colorList.get(0);
        if (colorList.size() > 1) {
            for (int index = 1; index < colorList.size(); index++) {
                colors += String.format(" ,%s", colorList.get(index));
            }
        }
        holder.color.setText(colors);
        holder.availability.setText(itemData.getAvailability());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ItemData> items) {
        this.items = items;
    }

    public class InfoViewHolder extends RecyclerView.ViewHolder{

        public TextView name, manufacturer, price, color, availability;
        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.store_item_content_view_name);
            manufacturer = itemView.findViewById(R.id.store_item_content_view_manufacturer);
            price = itemView.findViewById(R.id.store_item_content_view_price);
            color = itemView.findViewById(R.id.store_item_content_view_color);
            availability = itemView.findViewById(R.id.store_item_content_view_availability);
        }
    }
}
