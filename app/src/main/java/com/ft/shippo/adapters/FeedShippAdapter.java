package com.ft.shippo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.holders.LoadingHolder;
import com.ft.shippo.holders.ShippHolder;
import com.ft.shippo.models.Action;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.models.User;
import com.ft.shippo.models.UserManagement;
import com.ft.shippo.utils.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 25/01/18.
 */

public abstract class FeedShippAdapter extends RecyclerView.Adapter {

    List<Action> actionList = new ArrayList<>();
    private Context ctx;
    private boolean loading = false;
    private boolean can_load_more = true;
    private int page = 0;

    public FeedShippAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void loadMore() {
        if (isLoading() || !can_load_more) return;
        if(page != 0)setLoading();
        loading = true;
        String date = page == 0 ? null : actionList.size()>= 2?  actionList.get(actionList.size()-2).getCreatedAt() : null;
        UserManagement.loadMoreShipps(ctx, page++, date, new ListObjectListener<Action>(Types.LIST_ACTION) {
            @Override
            public void onResult(List<Action> data, String error) {
                if(page == 1)actionList.clear();
                actionList.addAll(data);
                if (data.size() < 19) can_load_more = false;
                stopLoading();
                notifyDataSetChanged();
                onRefreshed(false, actionList.size() == 0);
                Log.d("feed",String.format("data page %d:%d items\nTotal: %d",page, data.size(), actionList.size()));
            }

            @Override
            public void onError(VolleyError error) {
                stopLoading();
                page--;
                onRefreshed(true, false);
            }
        });
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1) {
            return new LoadingHolder(LayoutInflater.from(ctx).inflate(R.layout.loading_holder, parent, false));
        } else
            return new ShippHolder(LayoutInflater.from(ctx).inflate(R.layout.shipp_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isLoading() && position == actionList.size() - 1) return;
        Action act = actionList.get(position);
        ((ShippHolder) holder).feedWith(ctx, act, true);
    }

    @Override
    public int getItemViewType(int position) {
        return actionList.get(position) == null ? -1 : 0;
    }

    @Override
    public int getItemCount() {
        return actionList.size();
    }

    public void setLoading() {
        loading = true;
        actionList.add(null);
        notifyItemInserted(actionList.indexOf(null));
    }

    public void stopLoading() {
        loading = false;
        if(actionList.remove(null))
        notifyItemRemoved(actionList.size());
    }

    public boolean isLoading() {
        return loading;
    }

    public void refresh() {
        page = 0;
        can_load_more = true;
        loadMore();
    }

    public abstract void onRefreshed(boolean hasError, boolean empty);
}
