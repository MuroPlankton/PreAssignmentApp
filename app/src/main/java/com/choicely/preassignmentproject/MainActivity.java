package com.choicely.preassignmentproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.choicely.preassignmentproject.data.DataLoadingHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataLoadingHelper.getInstance().downloadCategoryForSaving("gloves", this);
    }
}