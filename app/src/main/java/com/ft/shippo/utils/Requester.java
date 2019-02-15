package com.ft.shippo.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.DownloadRequest;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ft.shippo.models.BaseObjectListener;
import com.ft.shippo.models.ObjectListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by rafael on 16/01/18.
 */

public class Requester {
    private static final String TAG = "Requester";
    private static Requester mInstance;
    private static Context mCtx;
    //private static String path = "http://192.168.1.8:3000";
    private RequestQueue mRequestQueue;

    private static String path= "https://shippo-br.herokuapp.com";
    private Requester(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized Requester getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Requester(context);
        }
        return mInstance;
    }

    public static Request createGetRequest(String subpath, ObjectListener listener) {
        return new StringRequest(Request.Method.GET, path.concat(subpath), listener, listener).setTag(subpath)
                .setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public static JsonObjectRequest createPostRequest(String subpath, final String data, BaseObjectListener listener) {
        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, path.concat(subpath),
                    new JSONObject(data), listener, listener);
            request.setTag(subpath);
            request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            return request;
        } catch (JSONException e) {
            return null;
        }
    }

    public static Request createFilePostRequest(String subpath, final String data, File file, final BaseObjectListener listener) {
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, path.concat(subpath), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    listener.onResponse(new JSONObject(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onErrorResponse(error);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(120 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(subpath);
        try {
            JSONObject object = new JSONObject(data);
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                request.addStringParam(key, String.valueOf(object.get(key)));
            }
            Log.d(TAG, Arrays.toString(request.getBody()));
        } catch (JSONException | AuthFailureError e) {
            e.printStackTrace();
        }
        request.addFile("file", file.getAbsolutePath());

        return request;
    }

    public static void downloader(Context ctx, String url, String path, DownloadListener listener) {
        DownloadRequest downloadRequest = new DownloadRequest(url, path, listener, listener);
        downloadRequest.setOnProgressListener(listener);
        downloadRequest.setTag(path);
        getInstance(ctx).addToRequestQueue(downloadRequest);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }


    public boolean cancelRequest(String tag) {
        getRequestQueue().cancelAll(tag);
        return true;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
