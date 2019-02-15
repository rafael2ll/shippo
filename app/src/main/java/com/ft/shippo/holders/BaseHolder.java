package com.ft.shippo.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ft.shippo.models.OfflineNotification;

/**
 * Created by rafael on 12/02/18.
 */

public abstract class BaseHolder<T> extends RecyclerView.ViewHolder {

    public BaseHolder(View itemView) {
        super(itemView);
    }

    public abstract void feedWith(Context ctx, T o, boolean isSelected);

    public abstract int layoutId();
}
