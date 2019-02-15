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
import android.view.View;

import com.android.volley.error.VolleyError;
import com.ft.shippo.MyFirebaseMessagingService;
import com.ft.shippo.R;
import com.ft.shippo.adapters.EasyRecyclerAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.holders.NotificationHolder;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.Notification;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.OfflineNotification;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rafael on 23/02/18.
 */

public class NotificationFragment extends BaseRecyclerFragment {

    public static final String TAG = "NotificationFragment";
    LinearLayoutManager layoutManager;
    InfiniteScrollListener infiniteScrollListener;
    EasyRecyclerAdapter<OfflineNotification, NotificationHolder> notificationAdapter;
    List<OfflineNotification> offlineNotificationList = new ArrayList<>();

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(offlineNotificationList != null) {
                offlineNotificationList.clear();
                offlineNotificationList.addAll(OfflineNotification.find(OfflineNotification.class, null, new String[0], null, "date desc", "50"));
                if (notificationAdapter != null) notificationAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(MyFirebaseMessagingService.NEW_NOTIFICATION));

        offlineNotificationList = OfflineNotification.find(OfflineNotification.class, null, new String[0], null, null, "500" );
        Collections.reverse(offlineNotificationList);
        notificationAdapter = new EasyRecyclerAdapter<OfflineNotification, NotificationHolder>(offlineNotificationList, OfflineNotification.class, R.layout.holder_notification, NotificationHolder.class) {
            @Override
            protected void populateViewHolder(NotificationHolder viewHolder, OfflineNotification model, int position) {
                viewHolder.feedWith(getActivity(), model, false);
            }
        };

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFab.setVisibility(View.GONE);
        layoutManager = new LinearLayoutManager(getActivity());
        view.findViewById(R.id.toolbar_includer).setVisibility(View.VISIBLE);
        toolbar.setTitle(R.string.notifications);
        toolbar.setLogo(R.drawable.bell_on);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        infiniteScrollListener = new InfiniteScrollListener(50, layoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadMore(firstVisibleItemPosition);
            }
        };
        mRecycler.addOnScrollListener(infiniteScrollListener);
        apply(layoutManager, notificationAdapter);
        getRefreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Notification.getNotifcation(getActivity(), new ListObjectListener<Notification>(new TypeToken<List<Notification>>(){}.getType()) {
                    @Override
                    public void onResult(List<Notification> data, String error) {
                        for(Notification notification : data) {
                            OfflineNotification offlineNotification = new OfflineNotification(notification);
                            List<OfflineNotification> offlineNotifications = OfflineNotification.findByID(notification.getId());
                            if (offlineNotifications.size() > 0) {
                                offlineNotification = offlineNotifications.get(0);
                                offlineNotification.update(notification);
                                offlineNotification.save();
                            } else {
                                offlineNotification.save();
                            }
                        }
                        offlineNotificationList.clear();
                        offlineNotificationList.addAll(OfflineNotification.find(OfflineNotification.class, null, new String[0], null, "date desc", "50" ));
                        notificationAdapter.notifyDataSetChanged();
                        getRefreshLayout().setRefreshing(false);

                    }

                    @Override
                    public void onError(VolleyError error) {
                        getRefreshLayout().setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void loadMore(int firstVisibleItemPosition) {
        offlineNotificationList.addAll(OfflineNotification.findWithQuery(OfflineNotification.class, "LIMIT 500,?", ""+firstVisibleItemPosition));
        Collections.reverse(offlineNotificationList);
        notificationAdapter.notifyDataSetChanged();
    }
}
