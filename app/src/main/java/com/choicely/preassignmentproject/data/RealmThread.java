package com.choicely.preassignmentproject.data;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmList;

public class RealmThread extends Thread {
    private static final String TAG = "RealmThread";
    private static RealmThread instance;
    private Map<String, Handler> dataReturnLocationMap = Collections.synchronizedMap(new HashMap<>());

    private List<DownloadData> downloadDataList = Collections.synchronizedList(new ArrayList<>());
    private List<String> alreadyLoadedManufacturers = new ArrayList<>();

    private RealmThread(Context ctx) {
        DownloadData initData = new DownloadData(ctx);
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

    public void addReturnLocation(String category, Handler communicationsHandler) {
        if (!dataReturnLocationMap.containsKey(category)) {
            dataReturnLocationMap.put(category, communicationsHandler);
        }
    }

    public void addDownloadDataToList(DownloadData downloadData) {
        downloadDataList.add(downloadData);
    }

    @Override
    public void run() {
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
                    case "load":
                        updateDataToUI();
                        break;
                    case "clear":
                        clearData();
                }
            }
        }
    }

    private void saveDownloadedCategory(DownloadData dataToHandle) {
        long startTime = System.currentTimeMillis();
        JsonArray categoryArray = dataToHandle.getDataArray();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        for (int index = 0; index < categoryArray.size(); index++) {
            JsonObject itemDataJsonObject = categoryArray.get(index).getAsJsonObject();
            String itemID = itemDataJsonObject.get("id").getAsString().toLowerCase();
            ItemData itemData = realm.where(ItemData.class).equalTo("itemId", itemID).findFirst();

            if (itemData == null) {
                itemData = new ItemData();
                itemData.setItemId(itemID);
            }

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

            realm.copyToRealmOrUpdate(itemData);
        }
        realm.commitTransaction();
        Log.d(TAG, "saveDownloadedCategory: amount of seconds it took to save downloaded category: "
                + (System.currentTimeMillis() - startTime) / 1000);

        updateDataToUI();
    }

    private void updateDataToUI() {
        Realm realm = Realm.getDefaultInstance();
        Set returnLocationKeySet = dataReturnLocationMap.keySet();
        synchronized (dataReturnLocationMap) {
            Iterator iterator = returnLocationKeySet.iterator();
            while (iterator.hasNext()) {
                String category = iterator.next().toString();
                final List<ItemData> finalCategoryItemDataList = realm
                        .copyFromRealm(realm.where(ItemData.class)
                                .equalTo("category", category)
                                .findAll());

                if (finalCategoryItemDataList != null && !finalCategoryItemDataList.isEmpty() && finalCategoryItemDataList.size() > 0) {
                    dataReturnLocationMap.get(category).obtainMessage(0, finalCategoryItemDataList).sendToTarget();
                }
            }
        }
    }

    private void saveDownloadedManufacturerAvailabilityInfo(DownloadData dataToHandle) {
        long startTime = System.currentTimeMillis();
        JsonArray availabilityArray = dataToHandle.getDataArray();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        for (int index = 0; index < availabilityArray.size(); index++) {
            JsonObject itemDataJsonObject = availabilityArray.get(index).getAsJsonObject();

            String itemID = itemDataJsonObject.get("id").getAsString().toLowerCase();
            ItemData itemData = realm.where(ItemData.class).equalTo("itemId", itemID).findFirst();

            if (itemData == null) {
                itemData = new ItemData();
                itemData.setItemId(itemID);
            }

            String dataPayload = itemDataJsonObject.get("DATAPAYLOAD").getAsString();
            itemData.setAvailability(DataPayloadXMLParser.parseDataPayloadXML(dataPayload));


            realm.copyToRealmOrUpdate(itemData);
        }
        realm.commitTransaction();
        Log.d(TAG, "saveDownloadedManufacturerAvailabilityInfo: one manufacturer saved to realm in "
                + (System.currentTimeMillis() - startTime) / 1000 + " seconds");

        updateDataToUI();
    }

    private void clearData() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
}
