package com.choicely.preassignmentproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.choicely.preassignmentproject.data.DataLoadingHelper;
import com.choicely.preassignmentproject.data.DownloadData;
import com.choicely.preassignmentproject.data.RealmThread;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static TabLayout tabLayout;
    public static ViewPager2 viewPager2;
    private CategoriesAdapter categoriesAdapter;

    private boolean isCacheTimerUp;
    List<String> categories;

    DataLoadingHelper dataLoadingHelper = DataLoadingHelper.getInstance();
    RealmThread realmThread = RealmThread.getInstance(this);
    DownloadData realmClearDownloadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realmClearDownloadData = new DownloadData(getApplicationContext());
        realmClearDownloadData.setType("clear");

        viewPager2 = findViewById(R.id.activity_main_pager);
        tabLayout = findViewById(R.id.activity_main_tabs);
        categories = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.categories)));
        categoriesAdapter = new CategoriesAdapter(this, categories);
        viewPager2.setAdapter(categoriesAdapter);

        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        long lastLoadTimeInMillis = preferences.getLong(
                getString(R.string.last_load_preference), 300001);
        Date initialUpdateTime = new Date(System.currentTimeMillis()
                + (System.currentTimeMillis() - lastLoadTimeInMillis > 300000
                ? 0 : System.currentTimeMillis() - lastLoadTimeInMillis));

        startDataUpdateCycle(initialUpdateTime);
    }

    private void startDataUpdateCycle(Date initialUpdateTime) {
        TimerTask dataUpdateTask = new TimerTask() {
            @Override
            public void run() {
                realmThread.addDownloadDataToList(realmClearDownloadData);
                realmThread.run();
                for (String category : categories) {
                    dataLoadingHelper.downloadCategoryForSaving(category, getApplicationContext());
                }

                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(getString(R.string.last_load_preference), System.currentTimeMillis());
                editor.apply();
            }
        };

        Timer dataUpdateTimer = new Timer();
        dataUpdateTimer.scheduleAtFixedRate(dataUpdateTask, initialUpdateTime, 300000);
    }
}