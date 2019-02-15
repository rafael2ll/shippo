package com.ft.shippo.models;

import android.content.Context;

import com.android.volley.Request;
import com.ft.shippo.utils.Requester;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

/**
 * Created by rafael on 20/02/18.
 */

public class Action extends BaseObject {
    @SerializedName("comment_id")
    Comment comment;
    private User[] user_id;
    private Shipp shipp_id;
    private int type;

    public Action() {
    }

    public Action(Shipp model) {
        this.shipp_id = model;
        type = 0;
        user_id= new User[]{model.getOwner()};
        created_at = model.getCreatedAt();
    }

    public static void getActivity(Context ctx, String user_id, int page, ListObjectListener<Action> listObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("my_id", User.getCurrentUser().getId())
                    .put("user_id", user_id)
                    .put("page", page);

            Request request = Requester.createPostRequest("/user/activity", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception ignored) {
        }
    }


    public Type getType() {
        return Type.values[type];
    }

    public Shipp getShipp() {
        return shipp_id;
    }

    public User getUser() {
        return user_id[0];
    }

    public enum Type {
        SHIPP, LIKE, DISLIKE, COMMENT, FOLLOW, FOLLOWER, REQUESTS, REQUEST_ACCEPTED, SHARE;
        public static Type values[] = values();
    }
}
