package com.ft.shippo.holders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.models.OfflineNotification;

/**
 * Created by rafael on 13/02/18.
 */

public class UserSearchTypeHolder extends BaseHolder<String> {
    public UserSearchTypeHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void feedWith(Context ctx, String string, boolean ignored) {
        ((TextView) itemView.findViewById(R.id.user_search_type_tv)).setText(string);
    }

    @Override
    public int layoutId() {
        return 0;
    }
}
