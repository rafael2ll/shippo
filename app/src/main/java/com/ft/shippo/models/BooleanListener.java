package com.ft.shippo.models;

import android.util.Log;

import com.android.volley.error.VolleyError;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by rafael on 26/01/18.
 */

public abstract class BooleanListener extends BaseObjectListener {


    @Override
    public void onResponse(JSONObject response) {
        try {
            String error = response.get("error").toString();
            boolean object = response.optBoolean("data", false);
            Log.d("ObjectListener", new Gson().toJson(object));
            onResult(object, error);
        } catch (Exception e) {
            e.printStackTrace();
            onErrorResponse(new VolleyError(e.getMessage()));
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        onError(error);
    }

    public abstract void onResult(boolean data, String error);

    public abstract void onError(VolleyError error);
}
