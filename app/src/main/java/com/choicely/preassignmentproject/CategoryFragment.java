package com.choicely.preassignmentproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.choicely.preassignmentproject.data.DownloadData;
import com.choicely.preassignmentproject.data.ItemData;
import com.choicely.preassignmentproject.data.RealmThread;
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
    private Context context;

    public CategoryFragment(List<String> categories, Context ctx) {
        this.categories = categories;
        this.context = ctx;
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

        SharedPreferences preferences = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        long lastLoadTimeInMillis = preferences.getLong(
                getString(R.string.last_load_preference), 300001);

        RealmThread realmThread = RealmThread.getInstance(context);
        realmThread.addReturnLocation(getArguments().getString("category"), handler);

        if (System.currentTimeMillis() - lastLoadTimeInMillis < 300000) {
            DownloadData downloadData = new DownloadData(context);
            downloadData.setType("load");
            realmThread.addDownloadDataToList(downloadData);
            realmThread.run();
        }


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
