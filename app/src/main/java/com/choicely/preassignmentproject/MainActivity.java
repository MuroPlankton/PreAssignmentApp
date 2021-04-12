package com.choicely.preassignmentproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.choicely.preassignmentproject.data.DataLoadingHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static TabLayout tabLayout;
    public static ViewPager2 viewPager2;
    private CategoriesAdapter categoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager2 = findViewById(R.id.activity_main_pager);
        tabLayout = findViewById(R.id.activity_main_tabs);
        List<String> categories = new ArrayList<>();
        categories.add("gloves");
        categories.add("facemasks");
        categories.add("beanies");
        categoriesAdapter = new CategoriesAdapter(this, categories);
        viewPager2.setAdapter(categoriesAdapter);

//        DataLoadingHelper.getInstance().downloadCategoryForSaving("gloves", this);
    }
}