package com.ft.shippo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.UserActivity;
import com.ft.shippo.adapters.EasyRecyclerAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.holders.UserHolder;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.User;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 30/01/18.
 */

public class SearchUserViewPagerFragment extends BaseViewPagerSearchFragment {
    private EasyRecyclerAdapter<User, UserHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private InfiniteScrollListener infiniteScrollListener;
    private List<User> userList = new ArrayList<>();
    private String text="";
    private int page = 0;
    private UserHolder.OnItemClickListener userClick = new UserHolder.OnItemClickListener() {
        @Override
        public void onClick(User user) {
            startActivity(new Intent(getActivity(), UserActivity.class).putExtra(UserActivity.USER_KEY,user.getId()));
        }
    };
    private boolean can_load_more = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        adapter = new EasyRecyclerAdapter<User, UserHolder>(userList, User.class, R.layout.user_item_holder, UserHolder.class) {
            @Override
            protected void populateViewHolder(UserHolder viewHolder, final User model, int position) {
                viewHolder.feedIt(getActivity(), model);
                viewHolder.setOnClick(userClick);
            }
        };
        infiniteScrollListener = new InfiniteScrollListener(20, linearLayoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadMore(++page);
            }
        };
        apply(linearLayoutManager, adapter);
        recyclerView.addOnScrollListener(infiniteScrollListener);
        loadMore(page++);
    }

    private void loadMore(final int page) {
        if(adapter.isLoading() || ! can_load_more) return;
        adapter.setLoading();
        this.page = page;
        User.findUserByName(getActivity(), page, text, new ListObjectListener<User>(new TypeToken<List<User>>() {
        }.getType()) {
            @Override
            public void onResult(List<User> data, String error) {
                adapter.stopLoading();
                if (page == 0) userList.clear();
                if (data.size() < 20) can_load_more = false;
                else infiniteScrollListener.shouldLoadMore(true);
                userList.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(VolleyError error) {
                SearchUserViewPagerFragment.this.page--;
                adapter.stopLoading();
            }
        });
    }

    @Override
    public void onTextUpdate(String text) {
        page = 0;
        can_load_more = true;
        this.text = text;
        adapter.stopLoading();
        loadMore(0);
    }
}
