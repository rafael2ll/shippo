package com.ft.shippo.utils;

import android.content.Context;

/**
 * Created by rafae on 27/03/2017.
 */

public class GlideRequestManager {
    public static Context mContext;
    public static GlideRequests glideRequests;

    public static void init(Context ctx) {
        mContext = ctx;
        glideRequests = GlideApp.with(ctx);
    }

    public static GlideRequests get() {
        return glideRequests;
    }
}
