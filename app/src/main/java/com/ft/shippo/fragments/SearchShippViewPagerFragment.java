package com.ft.shippo.fragments;

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
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 30/01/18.
 */

public class SearchShippViewPagerFragment extends BaseViewPagerSearchFragment {
    private EasyRecyclerAdapter<Shipp, ShippHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private InfiniteScrollListener infiniteScrollListener;
    private List<Shipp> shippList = new ArrayList<>();
    private String text;
    private int page = 0;
    private boolean can_load_more = true;

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
        linearLayoutManager = new LinearLayoutManager(getActivity());
        infiniteScrollListener = new InfiniteScrollListener(20, linearLayoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadMore(page + 1);
            }
        };
        apply(linearLayoutManager, adapter);
        recyclerView.addOnScrollListener(infiniteScrollListener);
    }

    private synchronized void loadMore(final int page) {
        if(adapter.isLoading() || !can_load_more) return;
        this.page = page;
        adapter.setLoading();
        Shipp.findShipps(getActivity(), text, page, new ListObjectListener<Shipp>(new TypeToken<List<Shipp>>() {
        }.getType()) {
            @Override
            public void onResult(List<Shipp> data, String error) {
                adapter.stopLoading();
                if (page == 0) shippList.clear();
                if (data.size() < 20) can_load_more = false;
                shippList.addAll(data);
                adapter.notifyDataSetChanged();
                if(shippList.size() == 0) shippFindEmpty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(VolleyError error) {
                SearchShippViewPagerFragment.this.page--;
                adapter.stopLoading();
            }
        });
    }


    @Override
    public void onTextUpdate(String text) {
        page = 0;
        shippFindEmpty.setVisibility(View.GONE);
        can_load_more = true;
        this.text = text;
        adapter.stopLoading();
        loadMore(0);
    }
}
