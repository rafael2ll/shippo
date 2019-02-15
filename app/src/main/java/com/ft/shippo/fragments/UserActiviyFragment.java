package com.ft.shippo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.ft.shippo.adapters.ActivityAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.models.User;

/**
 * Created by rafael on 02/03/18.
 */

public class UserActiviyFragment extends BaseViewPagerSearchFragment {

    ActivityAdapter activityAdapter;
    LinearLayoutManager linearLayoutManager;
    InfiniteScrollListener infiniteScrollListener;
    User user;
    private int page = 0;

    public UserActiviyFragment(){}

    public UserActiviyFragment setUser(User u){
        this.user = u;
        return  this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        activityAdapter = new ActivityAdapter(getActivity(), user.getId());
        infiniteScrollListener = new InfiniteScrollListener(20, linearLayoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                    activityAdapter.loadMore();
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnScrollListener(infiniteScrollListener);
        setAdapter(activityAdapter);
    }

    @Override
    public void onTextUpdate(String text) {

    }
}
