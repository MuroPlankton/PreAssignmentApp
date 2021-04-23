package com.choicely.preassignmentproject;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class CategoriesAdapter extends FragmentStateAdapter {

    private static final String TAG = "CategoriesAdapter";

    private List<String> categories;
    private Context context;

    public CategoriesAdapter(@NonNull FragmentActivity fragmentActivity, List<String> categories) {
        super(fragmentActivity);
        this.categories = categories;
        this.context = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        CategoryFragment fragment = new CategoryFragment(categories, context);
        Bundle data = new Bundle();
        data.putString("category", categories.get(position));
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
