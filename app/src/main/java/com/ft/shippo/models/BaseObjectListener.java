package com.ft.shippo.models;


import com.android.volley.Response;
import com.android.volley.error.VolleyError;

import org.json.JSONObject;

/**
 * Created by rafael on 25/01/18.
 */

public class BaseObjectListener implements Response.Listener<JSONObject>, Response.ErrorListener {
    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {

    }
}
