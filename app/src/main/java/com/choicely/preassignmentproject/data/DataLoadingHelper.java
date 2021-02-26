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
    private OkHttpClient client = new OkHttpClient();
    private List<String> alreadyLoadedManufacturers = new ArrayList<>();

    private BlockingQueue<Runnable> downloadQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(1,
            Runtime.getRuntime().availableProcessors(), 1000,
            TimeUnit.MILLISECONDS, downloadQueue);

    private ExecutorService realmExecutorService = Executors.newSingleThreadExecutor();

    private boolean hasReTried;

    private DataLoadingHelper(Context context) {
        realmExecutorService.execute(() -> Realm.init(context));
    }

    public static synchronized DataLoadingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataLoadingHelper(context);
        }
        return instance;
    }

//    public void loadAllItemsToRealm() {
//        downloadCategoryForSaving("gloves");
//        downloadCategoryForSaving("facemasks");
//        downloadCategoryForSaving("beanies");
//    }

    private void toastMainThread(String message, Context activityContext) {
        ((Activity) activityContext).runOnUiThread(() -> Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show());
    }

    public void downloadCategoryForSaving(String category, Context activityContext) {
        downloadExecutor.execute(() -> {
            Request categoryRequest = new Request.Builder()
                    .url(String.format("https://bad-api-assignment.reaktor.com/v2/products/%s",
                            category)).get().build();

            JSONArray itemCategoryArray = null;
            try {
                String itemCategoryString = client.newCall(categoryRequest).execute().body().string();
                if (!itemCategoryString.equals("[]")) {
                    itemCategoryArray = new JSONArray(itemCategoryString);
                    toastMainThread(String.format("%s downloaded. Third of the way done!", category), activityContext);
                } else {
                    if (!hasReTried) {
                        downloadCategoryForSaving(category, activityContext);
                        hasReTried = true;
                        toastMainThread(String.format("Couldn't download %s! Trying again...", category), activityContext);
                    } else {
                        toastMainThread(String.format("Failed to download %s again! Try again later.", category), activityContext);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            final JSONArray finalItemCategoryArray = itemCategoryArray;
            realmExecutorService.execute(() -> saveDownloadedCategory(finalItemCategoryArray, activityContext));

            //TODO: make the return values work maybe
        });
    }

    private void saveDownloadedCategory(JSONArray categoryArray, Context context) {
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
                if (!alreadyLoadedManufacturers.contains(itemManufacturer)) {
                    alreadyLoadedManufacturers.add(itemManufacturer);
                    //TODO: download new manufacturer
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

        toastMainThread("Almost there! Loading...", context);
    }

    public void loadAvailabilityInfoToRealm() {

    }
}
