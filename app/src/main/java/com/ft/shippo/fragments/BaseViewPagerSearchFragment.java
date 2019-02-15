package com.ft.shippo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ft.shippo.R;

/**
 * Created by rafael on 30/01/18.
 */

public abstract class BaseViewPagerSearchFragment extends Fragment {
    RecyclerView recyclerView;
    ProgressBar progressBar;
    View shippFindEmpty;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_recycler, container, false);
        recyclerView = v.findViewById(R.id.recycler_view);
        progressBar = v.findViewById(R.id.loading);
        shippFindEmpty = v.findViewById(R.id.shipp_find_empty);
        return v;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
    }

    public void apply(RecyclerView.LayoutManager layoutManager, RecyclerView.Adapter adapter) {
        setLayoutManager(layoutManager);
        setAdapter(adapter);
    }

    public abstract void onTextUpdate(String text);
}
