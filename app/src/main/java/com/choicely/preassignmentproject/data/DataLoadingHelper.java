package com.choicely.preassignmentproject.data;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            final JSONArray itemCategoryArray = requestData(String.format("https://bad-api-assignment.reaktor.com/v2/products/%s",
                    category), activityContext, true);

            if (itemCategoryArray == null) {
                downloadCategoryForSaving(category, activityContext);
            } else {
                realmExecutorService.execute(() -> saveDownloadedCategory(itemCategoryArray, activityContext));
            }
        });
    }

    private void saveDownloadedCategory(JSONArray categoryArray, Context context) {
        List<String> manufacturerList = alreadyLoadedManufacturers;
        long startTime = System.currentTimeMillis();

        try {
            for (int index = 0; index < categoryArray.length(); index++) {
                JSONObject itemDataJsonObject = categoryArray.getJSONObject(index);

                ItemData itemData = new ItemData();
                itemData.setItemId(itemDataJsonObject.getString("id"));
                itemData.setItemName(itemDataJsonObject.getString("name"));
                itemData.setCategory(itemDataJsonObject.getString("type"));
                itemData.setPrice(itemDataJsonObject.getInt("price"));

                String itemManufacturer = itemDataJsonObject.getString("manufacturer");
                itemData.setManufacturer(itemManufacturer);
                if (!manufacturerList.contains(itemManufacturer)) {
                    manufacturerList.add(itemManufacturer);
                    downloadManufacturerAvailabilityForSaving(itemManufacturer, context);
                }

                JSONArray itemColorJsonArray = itemDataJsonObject.getJSONArray("color");
                RealmList<String> itemColorList = new RealmList<>();
                for (int i = 0; i < itemColorJsonArray.length(); i++) {
                    itemColorList.add(itemColorJsonArray.getString(i));
                }
                itemData.setItemColor(itemColorList);

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(itemData);
                realm.commitTransaction();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("DataLoadingHelper", "saveDownloadedCategory: amount of seconds it took to save downloaded category: " + (System.currentTimeMillis() - startTime) / 1000);
        alreadyLoadedManufacturers = manufacturerList;
        toastMainThread("Almost there! Loading...", context);
    }

    private void downloadManufacturerAvailabilityForSaving(String manufacturer, Context context) {
        downloadExecutor.submit(() -> {
            final JSONArray availabilityArray = requestData(
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

    private void saveDownloadedAvailabilityInfo(JSONArray availabilityArray, Context context) {
        long startTime = System.currentTimeMillis();
        try {
            for (int index = 0; index < availabilityArray.length(); index++) {
                JSONObject itemDataJsonObject = availabilityArray.getJSONObject(index);

                ItemData itemData = new ItemData();
                itemData.setItemId(itemDataJsonObject.getString("id").toLowerCase());

                String dataPayload = itemDataJsonObject.getString("DATAPAYLOAD");
                itemData.setAvailable(!dataPayload.contains("OUTOFSTOCK"));

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(itemData);
                realm.commitTransaction();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("DataLoadingHelper", "saveDownloadedAvailabilityInfo: one manufacturer saved to realm in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        manufacturersSaved++;
        if (manufacturersSaved == alreadyLoadedManufacturers.size() - 1) {
            toastMainThread("All of the data has been saved!", context);
        }
    }

    private JSONArray requestData(String link, Context context, boolean isCategory) {
        Request request = new Request.Builder()
                .url(link).get().build();

        JSONArray responseArray = null;
        try {
            String responseString = client.newCall(request).execute().body().string();
            if (!responseString.contains("[]")) {
                if (isCategory) {
                    responseArray = new JSONArray(responseString);
                } else {
                    responseArray = new JSONObject(responseString).getJSONArray("response");
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
