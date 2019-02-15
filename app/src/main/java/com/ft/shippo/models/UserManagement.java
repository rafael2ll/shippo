package com.ft.shippo.models;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.ft.shippo.utils.Requester;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by rafael on 25/01/18.
 */

public class UserManagement {
    private User mUser;

    public synchronized static void loadMoreShipps(Context ctx,int page, String date , ListObjectListener listener) {
        try {
            JSONObject data = new JSONObject();
            data.put("user_id", User.getCurrentUser().getId())
                    .put("page", page)
                    .put("date", date);
            Request t = Requester.createPostRequest("/user/getFeedShipps", data.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(t);
        } catch (Exception e) {
            FirebaseCrash.log(e.toString());
            e.printStackTrace();
        }
    }

    public abstract static class OnShippsLoadedListener extends ListObjectListener<Shipp> {
        private int page;

        public OnShippsLoadedListener(int page) {
            super(new TypeToken<List<Shipp>>() {
            }.getType());
            this.page = page;
        }

        @Override
        public void onResult(List<Shipp> data, String error) {
            onLoaded(data != null, page, data);
        }

        @Override
        public void onError(VolleyError error) {
            onLoaded(false, page, null);
        }

        public abstract void onLoaded(boolean success, int page, List<Shipp> results);
    }
}
