package com.ft.shippo.models;

import android.util.Log;

import com.android.volley.error.VolleyError;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by rafael on 16/01/18.
 */

public abstract class ObjectListener<T> extends BaseObjectListener {
    Class t;

    public ObjectListener(Class c) {
        this.t = c;
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            String error = response.get("error").toString();
            if (Objects.equals(error, "null") || error == null) {
                T object = (T) new Gson().fromJson(response.optJSONObject("data").toString(), t);
                Log.d("ObjectListener", new Gson().toJson(object));
                onResult(object, error);
            } else {
                onErrorResponse(new VolleyError(error));
            }
        } catch (Exception e) {
            e.printStackTrace();
            onErrorResponse(new VolleyError(e.getMessage()));
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        onError(error);
    }

    public abstract void onResult(T data, String error);

    public abstract void onError(VolleyError error);
}
