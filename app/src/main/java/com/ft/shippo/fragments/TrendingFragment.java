package com.ft.shippo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.MyFirebaseMessagingService;
import com.ft.shippo.R;
import com.ft.shippo.adapters.EasyRecyclerAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.holders.ShippHolder;
import com.ft.shippo.holders.TrendingShippHolder;
import com.ft.shippo.models.Action;
import com.ft.shippo.models.Country;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.utils.Types;
import com.ft.shippo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 21/02/18.
 */

public class TrendingFragment extends BaseRecyclerFragment {
    public static final String TAG = "TrendingFragment";
    EasyRecyclerAdapter<Shipp, TrendingShippHolder> shippAdapter;
    InfiniteScrollListener infiniteScrollListener;
    List<Shipp> shippList = new ArrayList<>();
    LinearLayoutManager layoutManager;
    String country = null;
    boolean isFromMyFriends = false;
    int filter_type = 0;
    int page = 0;
    boolean isLoading = false;
    TrendingFilterDialog.Callback filterCallback = new TrendingFilterDialog.Callback() {
        @Override
        public void onApplied(int filter, boolean only_friends, String country_code) {
            filter_type = filter;
            isFromMyFriends = only_friends;
            country = country_code;
            loadMore(0);
        }
    };
    OnToolbarFeedItemClick toolbarFeedItemClick;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(toolbar != null) toolbar.setNavigationIcon(R.drawable.bell_on);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(MyFirebaseMessagingService.NEW_NOTIFICATION));
        shippAdapter = new EasyRecyclerAdapter<Shipp, TrendingShippHolder>(shippList, Shipp.class, R.layout.trending_shipp_holder, TrendingShippHolder.class) {
            @Override
            protected void populateViewHolder(TrendingShippHolder viewHolder, Shipp model, int position) {
                viewHolder.feedWith(getActivity(), new Action(model), false);
            }
        };
        loadMore(0);
    }

    private void loadMore(int i) {
        if(isLoading) return;
        isLoading= true;
        page = i;
        if(page > 0) shippAdapter.setLoading();
        else if(refreshLayout != null) refreshLayout.setRefreshing(false);
        Shipp.trending(getActivity(), i, filter_type, country, isFromMyFriends, new ListObjectListener<Shipp>(Types.LIST_SHIPP) {

            @Override
            public void onResult(List<Shipp> data, String error) {
                if (page == 0) shippList.clear();
                shippList.addAll(data);
                shippAdapter.notifyDataSetChanged();
                isLoading = false;
                getRefreshLayout().setRefreshing(false);
                if(shippAdapter.isLoading())shippAdapter.stopLoading();
            }

            @Override
            public void onError(VolleyError error) {
                Snackbar.make(getRefreshLayout(), R.string.network_error, Snackbar.LENGTH_LONG).show();
                getRefreshLayout().setRefreshing(false);
                isLoading = false;
                TrendingFragment.this.page--;
                if(shippAdapter.isLoading())shippAdapter.stopLoading();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshLayout().setColorSchemeResources(R.color.red_A200, R.color.green_A200, R.color.blue_A200, R.color.yellow_A200, R.color.red_A200, R.color.pink_A200);
        if(isLoading)getRefreshLayout().setRefreshing(true);
        view.findViewById(R.id.toolbar_includer).setVisibility(View.VISIBLE);
        toolbar.setTitle(R.string.trending);
        toolbar.setNavigationIcon(R.drawable.bell_out);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarFeedItemClick.onClick(FeedFragment.OnToolbarFeedItemClick.NOTFICATION);
            }
        });
        toolbar.inflateMenu(R.menu.search);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                toolbarFeedItemClick.onClick(FeedFragment.OnToolbarFeedItemClick.SEARCH);

                return true;
            }
        });

        layoutManager = new LinearLayoutManager(getActivity());
        infiniteScrollListener = new InfiniteScrollListener(20, layoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadMore(1+page);
            }
        };
        getRefreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMore(0);
            }
        });
        mRecycler.addOnScrollListener(infiniteScrollListener);
        apply(layoutManager, shippAdapter);
        mFab.setImageResource(R.drawable.ic_filter_list_white);
        setFabClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new TrendingFilterDialog().setParams(filter_type, isFromMyFriends, country, filterCallback)
                    .show(getActivity().getSupportFragmentManager(), "");
            }
        });
    }
    private void handleNewNotification() {
        if(OfflineNotification.count(OfflineNotification.class, "isseen = ?", new String[]{"0"}) > 0)
            toolbar.setNavigationIcon(R.drawable.bell_on);
        else toolbar.setNavigationIcon(R.drawable.bell_out);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (shippAdapter != null && mRecycler != null) {
            mRecycler.addOnScrollListener(infiniteScrollListener);
        }
        if(toolbar != null) handleNewNotification();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shippAdapter != null && mRecycler != null) {
            mRecycler.removeOnScrollListener(infiniteScrollListener);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public TrendingFragment attachListener(OnToolbarFeedItemClick listener) {
        this.toolbarFeedItemClick = listener;
        return this;
    }

    public interface OnToolbarFeedItemClick{
        public void onClick(int view);
        public int NOTFICATION = 0;
        public int SEARCH = 1;
    }
}
