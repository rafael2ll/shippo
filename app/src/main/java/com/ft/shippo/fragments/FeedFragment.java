package com.ft.shippo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ft.shippo.CreateShippActivity;
import com.ft.shippo.MyFirebaseMessagingService;
import com.ft.shippo.R;
import com.ft.shippo.adapters.FeedShippAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.utils.Utils;


public class FeedFragment extends BaseRecyclerFragment {
    public static final String TAG = "FeedFragment";
    FeedShippAdapter mFeedAdapter;
    OnToolbarFeedItemClick toolbarFeedItemClick;
    SwipeRefreshLayout.OnRefreshListener refreshListener;
    private InfiniteScrollListener infiniteScrollListener;

    public FeedFragment() {
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (toolbar != null) toolbar.setNavigationIcon(R.drawable.bell_on);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        };
        mFeedAdapter = new FeedShippAdapter(getActivity()) {
            @Override
            public void onRefreshed(boolean hasError, boolean isEmpty) {
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                if (isEmpty) showFeedEmpty();
                if (hasError) showErrorView(R.string.try_again);
            }
        };
        refresh();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(MyFirebaseMessagingService.NEW_NOTIFICATION));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.toolbar_includer).setVisibility(View.VISIBLE);
        getRefreshLayout().setColorSchemeResources(R.color.red_A200, R.color.green_A200, R.color.blue_A200, R.color.yellow_A200, R.color.red_A200, R.color.pink_A200);
        handleNewNotification();
        if (mFeedAdapter.isLoading()) refreshLayout.setRefreshing(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarFeedItemClick.onClick(OnToolbarFeedItemClick.NOTFICATION);
            }
        });
        toolbar.inflateMenu(R.menu.search);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                toolbarFeedItemClick.onClick(OnToolbarFeedItemClick.SEARCH);
                return true;
            }
        });

        TextView title = view.findViewById(R.id.textViewToolbar);
        title.setTypeface(Utils.getShippoTypeface(getActivity()));
        toolbar.setTitle("");
        title.setText(R.string.app_name);
        title.setVisibility(View.VISIBLE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        infiniteScrollListener = new InfiniteScrollListener(20, linearLayoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadMore();
            }
        };

        setLayoutManager(linearLayoutManager);
        setAdapter(mFeedAdapter);
        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mFab.getVisibility() == View.VISIBLE) {
                    mFab.hide();
                } else if (dy < 0 && mFab.getVisibility() != View.VISIBLE) {
                    mFab.show();
                }
            }
        });
        mRecycler.addOnScrollListener(infiniteScrollListener);
        setFabClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateShippActivity.class));
            }
        });
        refreshLayout.setOnRefreshListener(refreshListener);
        setErrorClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideErrorView();
                if (isFeedEmpty())
                    toolbarFeedItemClick.onClick(OnToolbarFeedItemClick.SEARCH);
                    // startActivity(new Intent(getActivity(), SuggestFriendsActivity.class));
                else {
                    refreshLayout.setRefreshing(true);
                    refreshListener.onRefresh();
                    refresh();
                }
            }
        });
    }


    public void loadMore() {
        mFeedAdapter.loadMore();
    }

    public void refresh() {
        mFeedAdapter.refresh();
    }

    private void handleNewNotification() {
        if (OfflineNotification.count(OfflineNotification.class, "isseen = ?", new String[]{"0"}) > 0)
            toolbar.setNavigationIcon(R.drawable.bell_on);
        else toolbar.setNavigationIcon(R.drawable.bell_out);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mFeedAdapter != null && mRecycler != null) {
            mRecycler.addOnScrollListener(infiniteScrollListener);
        }
        if (toolbar != null) handleNewNotification();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFeedAdapter != null && mRecycler != null) {
            mRecycler.removeOnScrollListener(infiniteScrollListener);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public void attachListener(OnToolbarFeedItemClick listener) {
        this.toolbarFeedItemClick = listener;

    }

    public interface OnToolbarFeedItemClick {
        public void onClick(int view);

        public int NOTFICATION = 0;
        public int SEARCH = 1;
    }
}
