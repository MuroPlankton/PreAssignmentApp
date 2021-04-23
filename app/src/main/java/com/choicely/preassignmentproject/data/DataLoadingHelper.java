package com.choicely.preassignmentproject.data;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DataLoadingHelper {

    private static DataLoadingHelper instance;
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS).build();

    private BlockingQueue<Runnable> downloadQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(1,
            Runtime.getRuntime().availableProcessors(), 1000,
            TimeUnit.MILLISECONDS, downloadQueue);

    private DataLoadingHelper() {
    }

    public static synchronized DataLoadingHelper getInstance() {
        if (instance == null) {
            instance = new DataLoadingHelper();
        }
        return instance;
    }

    public void downloadCategoryForSaving(String category, Context activityContext) {
        downloadExecutor.execute(() -> {
            final JsonArray itemCategoryArray = requestData(String.format("https://bad-api-assignment.reaktor.com/v2/products/%s",
                    category), activityContext, true);

            if (itemCategoryArray == null) {
                downloadCategoryForSaving(category, activityContext);
            } else {
                DownloadData categoryData = new DownloadData(activityContext);
                categoryData.setType("category");
                categoryData.setDataArray(itemCategoryArray);

                RealmThread realmThread = RealmThread.getInstance(activityContext);
                realmThread.addDownloadDataToList(categoryData);
                realmThread.run();
            }
        });
    }

    public void downloadManufacturerAvailabilityForSaving(String manufacturer, Context context) {
        downloadExecutor.submit(() -> {
            final JsonArray availabilityArray = requestData(
                    String.format("https://bad-api-assignment.reaktor.com/v2/availability/%s",
                            manufacturer), context, false);

            if (availabilityArray == null) {
                downloadManufacturerAvailabilityForSaving(manufacturer, context);
            } else {
                DownloadData availabilityData = new DownloadData(context);
                availabilityData.setType("manufacturer");
                availabilityData.setDataArray(availabilityArray);

                RealmThread realmThread = RealmThread.getInstance(context);
                realmThread.addDownloadDataToList(availabilityData);
                realmThread.run();
            }
        });
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
            } else {
                Thread.sleep(5000);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseArray;
    }
}
