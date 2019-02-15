package com.ft.shippo.holders;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.OfflineNotification;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

/**
 * Created by rafael on 08/02/18.
 */

public class MessageHolder extends BaseHolder<OfflineMessage> {
    @BindView(R.id.holder_message_iv_sent_state)
    ImageView sendState;
    @BindView(R.id.holder_message_iv_selected)
    ImageView imageViewSelected;
    @BindView(R.id.holder_message_date)
    private TextView textViewDate;
    @BindView(R.id.holder_message_text)
    private TextView textViewText;
    private OfflineMessage offlineMessage;
    private Context ctx;


    public MessageHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    @Override
    public int layoutId() {
        return R.layout.holder_message;
    }

    public void feedWith(Context ctx, OfflineMessage model, boolean isSelected) {
        this.offlineMessage = model;
        this.ctx = ctx;
        fill(isSelected);
    }

    private void fill(boolean isSelected) {
        ((LinearLayout) itemView).setGravity(offlineMessage.isMine() ? Gravity.END : Gravity.START);
        textViewText.setText(offlineMessage.getBody());
        textViewDate.setText(offlineMessage.getLocalCreatedAt().toString("HH:mm"));
        if (offlineMessage.isMine()) {
            sendState.setVisibility(View.VISIBLE);
            sendState.setImageResource(isSelected ? R.drawable.ic_cancel_accent : offlineMessage.getIsSent() ? (offlineMessage.otherSaw() ? R.drawable.ic_check_circle_green : R.drawable.ic_check_circle_dark) : R.drawable.ic_watch_later);
        } else {
            sendState.setVisibility(View.GONE);
            if (isSelected) imageViewSelected.setVisibility(View.VISIBLE);
            else imageViewSelected.setVisibility(View.GONE);
        }
    }

}
