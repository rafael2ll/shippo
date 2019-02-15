package com.ft.shippo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.volley.error.VolleyError;
import com.ft.shippo.adapters.EasyRecyclerAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.fragments.SearchUserDialog;
import com.ft.shippo.holders.UserHolder;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.User;
import com.google.gson.reflect.TypeToken;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by rafael on 02/02/18.
 */

public class UserFollowDetailsActivity extends AppCompatActivity {
    protected int page = 0;
    @BindView(R.id.recycler_view)
    private RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    private Toolbar toolbar;
    @BindView(R.id.toolbar_includer)
    private View toolbarIncluder;
    private Type type;
    private String user_id;
    private EasyRecyclerAdapter<User, UserHolder> adapter;
    private List<User> userList = new ArrayList<>();
    private InfiniteScrollListener infiniteScrollListener;
    private LinearLayoutManager linearLayoutManager;
    private ListObjectListener<User> listObjectListener;
    UserHolder.OnItemClickListener userSelectedListener = new UserHolder.OnItemClickListener() {
        @Override
        public void onClick(User user) {
            startActivity(new Intent(UserFollowDetailsActivity.this, UserActivity.class).putExtra(UserActivity.USER_KEY, user.getId()));
        }
    };
    boolean can_load_more = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler);
        ButterKnifeLite.bind(this);
        findViewById(R.id.fab).setVisibility(View.GONE);
        user_id = getIntent().getStringExtra(UserActivity.USER_KEY);
        type = Objects.equals(getIntent().getAction(), Type.FOLLOWING.toString()) ? Type.FOLLOWING : Type.FOLLOWERS;
        toolbarIncluder.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(type.equals(Type.FOLLOWING) ? R.string.following : R.string.followers);
        linearLayoutManager = new LinearLayoutManager(this);
        infiniteScrollListener = new InfiniteScrollListener(50, linearLayoutManager) {
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadMore(page + 1);
            }
        };
        adapter = new EasyRecyclerAdapter<User, UserHolder>(userList, User.class, R.layout.user_item_holder, UserHolder.class) {
            @Override
            protected void populateViewHolder(UserHolder viewHolder, User model, int position) {
                viewHolder.feedIt(UserFollowDetailsActivity.this, model);
                viewHolder.setOnClick(userSelectedListener);
            }
        };

        listObjectListener = new ListObjectListener<User>(new TypeToken<List<User>>() {}.getType()) {
            @Override
            public void onResult(List<User> data, String error) {
                adapter.stopLoading();
                userList.addAll(data);
                if (data.size() < 50) can_load_more = false;
                else infiniteScrollListener.shouldLoadMore(true);
                page++;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(VolleyError error) {
                adapter.stopLoading();
            }
        };

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(infiniteScrollListener);
        recyclerView.setAdapter(adapter);

        loadMore(0);
    }

    private synchronized void loadMore(int i) {
        if(adapter.isLoading() || ! can_load_more)return;
        adapter.setLoading();
        if (type.equals(Type.FOLLOWING))
            User.findFollowing(this, user_id, page, listObjectListener);
        else User.findFollowers(this, user_id, page, listObjectListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public  enum Type {
        FOLLOWERS {
            @Override
            public String toString() {
                return "FOLLOWERS";
            }
        }, FOLLOWING {
            @Override
            public String toString() {
                return "FOLLOWING";
            }
        }
    }
}
