package com.ft.shippo.utils;

import com.android.volley.Response;

/**
 * Created by rafael on 12/02/18.
 */

public abstract class DownloadListener implements Response.Listener<String>, Response.ErrorListener, Response.ProgressListener {
    public DownloadListener() {
    }
}

