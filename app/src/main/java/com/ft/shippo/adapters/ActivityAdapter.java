package com.ft.shippo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.holders.LikedFeedHolder;
import com.ft.shippo.holders.LoadingHolder;
import com.ft.shippo.holders.ShippHolder;
import com.ft.shippo.models.Action;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.utils.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 20/02/18.
 */

public class ActivityAdapter extends RecyclerView.Adapter {
    List<Action> actionList = new ArrayList<>();
    private Context ctx;
    private String user_id;
    private boolean loading = false;
    private boolean can_load_more = true;
    private int page = 0;

    public ActivityAdapter(Context ctx, String user_id) {
        this.ctx = ctx;
        this.user_id = user_id;
        loadMore();
    }

    public void loadMore() {
        if (isLoading() || !can_load_more) return;
        setLoading();
        Action.getActivity(ctx, user_id, page++, new ListObjectListener<Action>(Types.LIST_ACTION) {
            @Override
            public void onResult(List<Action> data, String error) {
                actionList.addAll(data);
                if (data.size() < 20) can_load_more = false;
                stopLoading();
                notifyDataSetChanged();
            }

            @Override
            public void onError(VolleyError error) {
                stopLoading();
                page--;
            }
        });
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1) {
            return new LoadingHolder(LayoutInflater.from(ctx).inflate(R.layout.loading_holder, parent, false));
        } else
           /* switch (Action.Type.values[viewType]) {
                case SHIPP:
                  */  return new ShippHolder(LayoutInflater.from(ctx).inflate(R.layout.shipp_holder, parent, false));
               /* case LIKE:
                case DISLIKE:
                    return new LikedFeedHolder(LayoutInflater.from(ctx).inflate(R.layout.holder_feed_liked, parent, false));
            }*/
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
        notifyItemInserted(actionList.size() - 1);
    }

    public void stopLoading() {
        loading = false;
        actionList.remove(null);
        notifyItemRemoved(actionList.size());
    }

    public boolean isLoading() {
        return loading;
    }
}
