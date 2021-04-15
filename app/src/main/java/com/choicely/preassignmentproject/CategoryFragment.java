package com.choicely.preassignmentproject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.choicely.preassignmentproject.data.DataLoadingHelper;
import com.choicely.preassignmentproject.data.ItemData;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class CategoryFragment extends Fragment implements Handler.Callback {

    private static final String TAG = "CategoryFragment";

    private RecyclerView recyclerView;
    private ClothesInfoAdapter adapter;
    private View view;
    private Handler handler = new Handler(this);
    private List<String> categories;
    private LinearLayout loadingLayout;

    public CategoryFragment(List<String> categories) {
        this.categories = categories;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.category_fragment_content, container, false);

        loadingLayout = view.findViewById(R.id.category_fragment_content_loading);
        recyclerView = view.findViewById(R.id.category_fragment_content_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        adapter = new ClothesInfoAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);

        DataLoadingHelper.getInstance().downloadCategoryForSaving(getArguments().getString("category"), getContext(), handler);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        new TabLayoutMediator(MainActivity.tabLayout, MainActivity.viewPager2, (tab, position) -> tab.setText(categories.get(position))).attach();
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        loadingLayout.setVisibility(LinearLayout.GONE);
        adapter.setItems((List<ItemData>) msg.obj);
        adapter.notifyDataSetChanged();
        return true;
    }
}
