package com.choicely.preassignmentproject.data;

import android.content.Context;
import android.os.Handler;

import com.google.gson.JsonArray;

public class DownloadData {

    private JsonArray dataArray;
    private Context context;
    private String type;
    private String category;
    private Handler handler;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public JsonArray getDataArray() {
        return dataArray;
    }

    public void setDataArray(JsonArray dataArray) {
        this.dataArray = dataArray;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
