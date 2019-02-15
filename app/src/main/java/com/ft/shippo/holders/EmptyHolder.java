package com.ft.shippo.holders;

import android.content.Context;
import android.view.View;

import com.ft.shippo.R;
import com.ft.shippo.models.OfflineNotification;

/**
 * Created by rafael on 15/02/18.
 */

public class EmptyHolder extends BaseHolder {
    public EmptyHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void feedWith(Context ctx, Object o, boolean ignored) {

    }

    @Override
    public int layoutId() {
        return R.layout.holder_empty_item;
    }
}
