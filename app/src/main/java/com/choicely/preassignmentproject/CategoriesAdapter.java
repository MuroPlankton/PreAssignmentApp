package com.choicely.preassignmentproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class CategoriesAdapter extends FragmentStateAdapter {

    private List<String> categories;

    public CategoriesAdapter(@NonNull FragmentActivity fragmentActivity, List<String> categories) {
        super(fragmentActivity);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        CategoryFragment fragment = new CategoryFragment();
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
