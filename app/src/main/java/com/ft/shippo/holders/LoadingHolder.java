package com.ft.shippo.holders;

import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.ft.shippo.R;
import com.ft.shippo.utils.Utils;

/**
 * Created by rafael on 05/03/18.
 */

public class LoadingHolder extends RecyclerView.ViewHolder {
    public LoadingHolder(View inflate) {
        super(inflate);
        CircularProgressDrawable circular = new CircularProgressDrawable(inflate.getContext());
        circular.setColorSchemeColors(Utils.getColors(inflate.getContext(), R.color.red_A200, R.color.green_A200, R.color.blue_A200, R.color.yellow_A200, R.color.red_A200, R.color.pink_A200));
        circular.setStrokeWidth(7f);
        ((ProgressBar)itemView.findViewById(R.id.progress_bar)).setProgressDrawable(circular);
        ((ProgressBar)itemView.findViewById(R.id.progress_bar)).setIndeterminateDrawable(circular);
    }
}
