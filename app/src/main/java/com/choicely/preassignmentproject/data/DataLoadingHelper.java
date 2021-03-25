package com.choicely.preassignmentproject.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DataLoadingHelper {

    private static DataLoadingHelper instance;
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS).build();

    private List<String> alreadyLoadedManufacturers = new ArrayList<>();
    private int manufacturersSaved = 0;

    private BlockingQueue<Runnable> downloadQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(1,
            Runtime.getRuntime().availableProcessors(), 1000,
            TimeUnit.MILLISECONDS, downloadQueue);

    private ExecutorService realmExecutorService = Executors.newSingleThreadExecutor();

    private DataLoadingHelper(Context context) {
        realmExecutorService.execute(() -> Realm.init(context));
    }

    public static synchronized DataLoadingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataLoadingHelper(context);
        }
        return instance;
    }

    private void toastMainThread(String message, Context activityContext) {
        ((Activity) activityContext).runOnUiThread(() -> Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show());
    }

    public void downloadCategoryForSaving(String category, Context activityContext) {
        downloadExecutor.execute(() -> {
            final JsonArray itemCategoryArray = requestData(String.format("https://bad-api-assignment.reaktor.com/v2/products/%s",
                    category), activityContext, true);

            if (itemCategoryArray == null) {
                downloadCategoryForSaving(category, activityContext);
            } else {
                realmExecutorService.execute(() -> saveDownloadedCategory(itemCategoryArray, activityContext));
            }
        });
    }

    private void saveDownloadedCategory(JsonArray categoryArray, Context context) {
        List<String> manufacturerList = alreadyLoadedManufacturers;
        long startTime = System.currentTimeMillis();

        for (int index = 0; index < categoryArray.size(); index++) {
            JsonObject itemDataJsonObject = categoryArray.get(index).getAsJsonObject();

            ItemData itemData = new ItemData();
            itemData.setItemId(itemDataJsonObject.get("id").getAsString());
            itemData.setItemName(itemDataJsonObject.get("name").getAsString());
            itemData.setCategory(itemDataJsonObject.get("type").getAsString());
            itemData.setPrice(itemDataJsonObject.get("price").getAsInt());

            String itemManufacturer = itemDataJsonObject.get("manufacturer").getAsString();
            itemData.setManufacturer(itemManufacturer);
            if (!manufacturerList.contains(itemManufacturer)) {
                manufacturerList.add(itemManufacturer);
                downloadManufacturerAvailabilityForSaving(itemManufacturer, context);
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

        Log.d("DataLoadingHelper", "saveDownloadedCategory: amount of seconds it took to save downloaded category: " + (System.currentTimeMillis() - startTime) / 1000);
        alreadyLoadedManufacturers = manufacturerList;
        toastMainThread("Almost there! Loading...", context);
    }

    private void downloadManufacturerAvailabilityForSaving(String manufacturer, Context context) {
        downloadExecutor.submit(() -> {
            final JsonArray availabilityArray = requestData(
                    String.format("https://bad-api-assignment.reaktor.com/v2/availability/%s",
                            manufacturer), context, false);

            if (availabilityArray == null) {
                downloadManufacturerAvailabilityForSaving(manufacturer, context);
            } else {
                realmExecutorService.execute(() -> {
                    saveDownloadedAvailabilityInfo(availabilityArray, context);
                });
            }
        });
    }

    private void saveDownloadedAvailabilityInfo(JsonArray availabilityArray, Context context) {
        long startTime = System.currentTimeMillis();
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

        Log.d("DataLoadingHelper", "saveDownloadedAvailabilityInfo: one manufacturer saved to realm in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        manufacturersSaved++;
        if (manufacturersSaved == alreadyLoadedManufacturers.size() - 1) {
            toastMainThread("All of the data has been saved!", context);
        }
    }

    private JsonArray requestData(String link, Context context, boolean isCategory) {
        Request request = new Request.Builder()
                .url(link).get().build();

        JsonArray responseArray = null;
        try {
            String responseString = client.newCall(request).execute().body().string();
            if (!responseString.contains("[]")) {
                if (isCategory) {
                    responseArray = JsonParser.parseString(responseString).getAsJsonArray();
                } else {
                    responseArray = JsonParser.parseString(responseString).getAsJsonObject().getAsJsonArray("response");
                }
                toastMainThread(String.format("Category or availability information downloaded."), context);
            } else {
                toastMainThread(String.format("Couldn't download data! Trying again..."), context);
                Thread.sleep(5000);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseArray;
    }
}
