package com.ft.shippo.models;

import android.util.Log;

import com.android.volley.error.VolleyError;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by rafael on 25/01/18.
 */

public abstract class ListObjectListener<T> extends BaseObjectListener {
    private Type t;

    public ListObjectListener(Type c) {

        this.t = c;
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            Log.d("ObjectListener", new Gson().toJson(response));
            String error = response.get("error").toString();
            List<T> object = (List<T>) new Gson().fromJson(response.get("data").toString(), t);
            Log.d("ObjectListener", new Gson().toJson(object));
            onResult(object, error);
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrash.log(e.toString());
            onErrorResponse(new VolleyError(e.getMessage()));
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        onError(error);
    }

    public abstract void onResult(List<T> data, String error);

    public abstract void onError(VolleyError error);
}
