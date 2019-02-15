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
import com.ft.shippo.holders.ChatHolder;
import com.ft.shippo.models.Chat;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.OfflineChat;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.User;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.orm.SugarRecord.listAll;

/**
 * Created by rafael on 06/02/18.
 */

public class ChatFragment extends BaseRecyclerFragment {
    public static final String TAG = "CharFragment";
    private List<OfflineChat> offlineChatList = new ArrayList<>();
    private EasyRecyclerAdapter<OfflineChat, ChatHolder> chatAdapter;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (chatAdapter != null) {
                offlineChatList = listAll(OfflineChat.class);
                chatAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyFirebaseMessagingService.MESSAGE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        offlineChatList.clear();
        offlineChatList.addAll(OfflineChat.listAll(OfflineChat.class, "created_at desc"));
        Collections.sort(offlineChatList);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enableToolbar(R.string.conversation);
        toolbar.setNavigationIcon(null);
        toolbar.setLogo(R.drawable.ic_chat_white);
        toolbar.setTitle(R.string.conversation);
        offlineChatList.clear();
        offlineChatList.addAll(OfflineChat.listAll(OfflineChat.class, "created_at desc"));
        Collections.sort(offlineChatList);
        chatAdapter = new EasyRecyclerAdapter<OfflineChat, ChatHolder>(offlineChatList, OfflineChat.class, R.layout.chat_holder, ChatHolder.class) {
            @Override
            protected void populateViewHolder(ChatHolder viewHolder, OfflineChat model, int position) {
                viewHolder.feedWith(getActivity(), model);
            }
        };
        apply(new LinearLayoutManager(getActivity()), chatAdapter);
        setabIcon(R.drawable.ic_new_chat_white);
        getRefreshLayout().setColorSchemeResources(R.color.red_A200, R.color.green_A200, R.color.blue_A200, R.color.yellow_A200, R.color.red_A200, R.color.cyan_A200, R.color.pink_A200);
        setFabClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SearchUserDialog().addListener(new SearchUserDialog.OnUserSelectedListener() {
                    @Override
                    public void onUserSelected(User user) {
                        if (user != null) {
                            refreshLayout.setRefreshing(true);
                            Chat.createConversation(getActivity(), new ObjectListener<Chat>(Chat.class) {
                                @Override
                                public void onResult(Chat data, String error) {
                                    new OfflineChat(data).save();
                                    offlineChatList.clear();
                                    refreshLayout.setRefreshing(false);
                                    offlineChatList.addAll(OfflineChat.listAll(OfflineChat.class));
                                    chatAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onError(VolleyError error) {

                                }
                            }, User.getCurrentUser().getId(), user.getId());
                        }
                    }
                    @Override
                    public void onCancel() {

                    }
                }).show(getChildFragmentManager(), "");
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                offlineChatList.clear();
                offlineChatList.addAll(OfflineChat.listAll(OfflineChat.class));
                chatAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
            }
        });
    }

}
