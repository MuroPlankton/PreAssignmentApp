package com.choicely.preassignmentproject.data;

import android.content.Context;

import com.google.gson.JsonArray;

public class DownloadData {

    private JsonArray dataArray;
    private Context context;
    private String type;

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
