package com.choicely.preassignmentproject.data;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class RealmThread extends Thread {
    private static final String TAG = "RealmThread";
    private static RealmThread instance;
    private List<String> alreadyLoadedManufacturers = new ArrayList<>();
    private int manufacturersSaved = 0;

    private List<DownloadData> downloadDataList = new ArrayList<>();

    private RealmThread(Context ctx) {
        DownloadData initData = new DownloadData();
        initData.setContext(ctx);
        initData.setType("init");
        downloadDataList.add(initData);
        start();
        run();
    }

    public static synchronized RealmThread getInstance(Context ctx) {
        if (instance == null) {
            instance = new RealmThread(ctx);
        }
        return instance;
    }

    public void addDownloadDataToList(DownloadData downloadData) {
        downloadDataList.add(downloadData);
        Log.d(TAG, "addDownloadDataToList: List size: " + downloadDataList.size());
    }

    @Override
    public void run() {
        Log.d(TAG, "run: Run method called");
        if (downloadDataList.size() > 0) {
            DownloadData dataToHandle = downloadDataList.get(0);
            downloadDataList.remove(0);
            if (dataToHandle.getType() != null && dataToHandle.getContext() != null) {
                String dataType = dataToHandle.getType();
                switch (dataType) {
                    case "init":
                        Realm.init(dataToHandle.getContext());
                        break;
                    case "category":
                        saveDownloadedCategory(dataToHandle);
                        break;
                    case "manufacturer":
                        saveDownloadedManufacturerAvailabilityInfo(dataToHandle);
                        break;
                }
            }
        }
    }

    private void saveDownloadedCategory(DownloadData dataToHandle) {
        long startTime = System.currentTimeMillis();
        JsonArray categoryArray = dataToHandle.getDataArray();
        for (int index = 0; index < categoryArray.size(); index++) {
            JsonObject itemDataJsonObject = categoryArray.get(index).getAsJsonObject();

            ItemData itemData = new ItemData();
            itemData.setItemId(itemDataJsonObject.get("id").getAsString());
            itemData.setItemName(itemDataJsonObject.get("name").getAsString());
            itemData.setCategory(itemDataJsonObject.get("type").getAsString());
            itemData.setPrice(itemDataJsonObject.get("price").getAsInt());

            String itemManufacturer = itemDataJsonObject.get("manufacturer").getAsString();
            itemData.setManufacturer(itemManufacturer);
            if (!alreadyLoadedManufacturers.contains(itemManufacturer)) {
                alreadyLoadedManufacturers.add(itemManufacturer);
                DataLoadingHelper.getInstance()
                        .downloadManufacturerAvailabilityForSaving(
                                itemManufacturer, dataToHandle.getContext());
            }

            JsonArray itemColorJsonArray = itemDataJsonObject.getAsJsonArray("color");
            RealmList<String> itemColorList = new RealmList<>();
            for (int i = 0; i < itemColorJsonArray.size(); i++) {
                itemColorList.add(itemColorJsonArray.get(i).getAsString());
            }
            itemData.setItemColor(itemColorList);

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(itemData);
            realm.commitTransaction();
        }
        Log.d(TAG, "saveDownloadedCategory: amount of seconds it took to save downloaded category: "
                        + (System.currentTimeMillis() - startTime) / 1000);

        //TODO: data should be updated to user at this point
    }

    private void saveDownloadedManufacturerAvailabilityInfo(DownloadData dataToHandle) {
        long startTime = System.currentTimeMillis();
        JsonArray availabilityArray = dataToHandle.getDataArray();
        for (int index = 0; index < availabilityArray.size(); index++) {
            JsonObject itemDataJsonObject = availabilityArray.get(index).getAsJsonObject();

            ItemData itemData = new ItemData();
            itemData.setItemId(itemDataJsonObject.get("id").getAsString().toLowerCase());

            String dataPayload = itemDataJsonObject.get("DATAPAYLOAD").getAsString();
            itemData.setAvailable(!dataPayload.contains("OUTOFSTOCK"));

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(itemData);
            realm.commitTransaction();
        }
        manufacturersSaved++;
        Log.d(TAG, "saveDownloadedAvailabilityInfo: one manufacturer saved to realm in "
                        + (System.currentTimeMillis() - startTime) / 1000 + " seconds");

        //TODO: data should be updated to user at this point
    }
}
