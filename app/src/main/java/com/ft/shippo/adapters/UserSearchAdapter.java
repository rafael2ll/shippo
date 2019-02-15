package com.ft.shippo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.holders.BaseHolder;
import com.ft.shippo.holders.EmptyHolder;
import com.ft.shippo.holders.UserHolder;
import com.ft.shippo.holders.UserSearchTypeHolder;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.User;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 13/02/18.
 */

public class UserSearchAdapter extends RecyclerView.Adapter<BaseHolder> {


    private String searchText;
    private List<User> followingList = new ArrayList<>();
    private List<User> followerList = new ArrayList<>();
    private List<User> otherList = new ArrayList<>();
    private List<UserItem> fullList = new ArrayList<>();
    private Context ctx;
    private UserHolder.OnItemClickListener listener;

    public UserSearchAdapter(Context ctx, UserHolder.OnItemClickListener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    public static List<UserItem> parseUserList(List<User> list) {
        List<UserItem> itemList = new ArrayList<>();
        for (User u : list) itemList.add(new UserItem(u).setType(0));
        return itemList;
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        if (viewType == 0)
            return new UserHolder(inflater.inflate(R.layout.user_item_holder, parent, false));
        else if (viewType < 4)
            return new UserSearchTypeHolder(inflater.inflate(R.layout.user_search_type, parent, false));
        else return new EmptyHolder(inflater.inflate(R.layout.holder_empty_item, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        UserItem userItem = fullList.get(position);
        if (userItem.isType() == 0) {
            ((UserHolder) holder).feedWith(ctx, fullList.get(position).user, false);
            ((UserHolder) holder).setOnClick(listener);
        } else if (userItem.isType() == 1)
            ((UserSearchTypeHolder) holder).feedWith(ctx, ctx.getResources().getString(R.string.following), false);
        else if (userItem.isType() == 2)
            ((UserSearchTypeHolder) holder).feedWith(ctx, ctx.getResources().getString(R.string.followers), false);
        else if (userItem.isType() == 3)
            ((UserSearchTypeHolder) holder).feedWith(ctx, ctx.getResources().getString(R.string.people), false);
    }

    @Override
    public int getItemViewType(int position) {
        return fullList.get(position).isType();
    }

    @Override
    public int getItemCount() {
        int main_count = fullList.size();
        return main_count;

    }

    public synchronized void loadMore(String text, final int page, final OnLoadMoreListener listener) {
        this.searchText = text;

        User.search(ctx, page, searchText, new ListObjectListener<List<User>>(new TypeToken<List<List<User>>>() {
        }.getType()) {
            @Override
            public void onResult(List<List<User>> data, String error) {
                if (page == 0) {
                    followerList.clear();
                    followingList.clear();
                    otherList.clear();
                    fullList.clear();
                }
                try {
                    fullList.add(new UserItem(User.getCurrentUser()));
                    followerList = data.get(1);
                    followingList = data.get(0);
                    otherList = data.get(2);

                    fullList.add(new UserItem().setType(1));
                    if (followingList.size() > 0) fullList.addAll(parseUserList(followingList));
                    else fullList.add(new UserItem().setType(4));

                    fullList.add(new UserItem().setType(2));
                    if (followerList.size() > 0) fullList.addAll(parseUserList(followerList));
                    else fullList.add(new UserItem().setType(4));

                    fullList.add(new UserItem().setType(3));
                    if (otherList.size() > 0) fullList.addAll(parseUserList(otherList));
                    else fullList.add(new UserItem().setType(4));

                    listener.onLoaded(fullList.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                notifyDataSetChanged();
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
                listener.onFail();
            }
        });
    }

    public interface OnLoadMoreListener {
        void onLoaded(int count);

        void onFail();
    }

    public static class UserItem {
        private int type = 0;
        private User user;

        public UserItem(User u) {
            this.user = u;
        }

        public UserItem() {

        }

        public int isType() {
            return type;
        }

        public UserItem setType(int type) {
            this.type = type;
            return this;
        }
    }
}
