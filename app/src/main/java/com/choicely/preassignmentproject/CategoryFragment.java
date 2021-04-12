package com.choicely.preassignmentproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayoutMediator;

public class CategoryFragment extends Fragment {

    private LinearLayout loadingLayout;
    private RecyclerView recyclerView;
    private ClothesInfoAdapter adapter;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.category_fragment_content, container, false);

        loadingLayout = view.findViewById(R.id.category_fragment_content_loading);
        recyclerView = view.findViewById(R.id.category_fragment_content_recycler);
        adapter = new ClothesInfoAdapter();
        recyclerView.setAdapter(adapter);

        //TODO: start asking for content here

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        new TabLayoutMediator(MainActivity.tabLayout, MainActivity.viewPager2, (tab, position) -> tab.setText(getArguments().getString("category"))).attach();
    }
}
