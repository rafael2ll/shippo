package com.ft.shippo.holders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.models.OfflineNotification;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.Locale;

/**
 * Created by rafael on 12/02/18.
 */

public class DateHolder extends BaseHolder<Date> {
    @BindView(R.id.holder_date_tv)
    TextView date;

    public DateHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    @Override
    public void feedWith(Context ctx, Date o, boolean ignored) {
        LocalDate message_date = new DateTime(o).toLocalDate();
        if (message_date.equals(LocalDate.now())) {
            date.setText(R.string.today);
        } else if (message_date.equals(LocalDate.now().minusDays(1))) {
            date.setText(R.string.yesterday);
        } else {
            date.setText(message_date.toString("EEE dd/MM/YYYY", Locale.getDefault()));
        }
    }

    @Override
    public int layoutId() {
        return R.layout.holder_audio;
    }

}
