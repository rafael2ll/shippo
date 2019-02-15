package com.ft.shippo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.adapters.EasyRecyclerAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.holders.ShippHolder;
import com.ft.shippo.models.Action;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.models.User;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 02/03/18.
 */

public class UserShippsFragment extends BaseViewPagerSearchFragment{
    private EasyRecyclerAdapter<Shipp, ShippHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private InfiniteScrollListener infiniteScrollListener;
    private List<Shipp> shippList = new ArrayList<>();
    private String text;
    private int page = 0;
    private User user;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EasyRecyclerAdapter<Shipp, ShippHolder>(shippList, Shipp.class, R.layout.shipp_holder, ShippHolder.class) {
            @Override
            protected void populateViewHolder(ShippHolder viewHolder, Shipp model, int position) {
                viewHolder.feedWith(getActivity(), new Action(model), false);
            }
        };

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadMore(0);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        infiniteScrollListener = new InfiniteScrollListener(20, linearLayoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadMore(++page);
            }
        };
        apply(linearLayoutManager, adapter);
        recyclerView.addOnScrollListener(infiniteScrollListener);
    }

    private synchronized void loadMore(final int page) {
        if(adapter.isLoading())return;

        toggleLoading(true);
        Shipp.findUserShipps(getActivity(),user.getId(), page, new ListObjectListener<Shipp>(new TypeToken<List<Shipp>>() {
        }.getType()) {
            @Override
            public void onResult(List<Shipp> data, String error) {
                toggleLoading(false);
                if (page == 0) shippList.clear();
                if (data.size() < 20) infiniteScrollListener.shouldLoadMore(false);
                else infiniteScrollListener.shouldLoadMore(true);
                UserShippsFragment.this.page++;
                shippList.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(VolleyError error) {
                UserShippsFragment.this.page--;
                toggleLoading(false);
            }
        });
    }

    private void toggleLoading(boolean b) {
        if(b) adapter.setLoading();
        else  adapter.stopLoading();
    }

    @Override
    public void onTextUpdate(String text) {

    }

    public UserShippsFragment setUser(User user) {
        this.user = user;
        return this;
    }
}
