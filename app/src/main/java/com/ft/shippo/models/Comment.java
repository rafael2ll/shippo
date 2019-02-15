package com.ft.shippo.models;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.utils.Requester;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rafael on 25/01/18.
 */

public class Comment extends BaseObject {
    private User user;
    private String text;
    @SerializedName("unlikeCount")
    private int unlike_count = 0;
    @SerializedName("likeCount")
    private int like_count = 0;
    private int reports = 0;
    private boolean i_liked =false;
    private boolean i_disliked = false;
    private List<Comment> sub_comments = new ArrayList<>();
    private boolean nested = false;

    public Comment(User user, String text) {
        this.user = user;
        this.text = text;
    }

    public static void sendComment(Context ctx, String shipp_id, String text, OnSaveListener listener) {
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", User.getCurrentUser().getId())
                    .put("shipp_id", shipp_id)
                    .put("text", text);
            Request request = Requester.createPostRequest("/comment/createComment", data.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void comment(Context ctx, String shipp_id, String text, boolean nested, String reply_id, ObjectListener<Comment> listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("shipp_id", shipp_id)
                    .put("text", text);
            if (nested) {
                object.put("nested", true)
                        .put("parent_id", reply_id);
            }
            Request request = Requester.createPostRequest("/comment/createComment", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public boolean iLiked() {
        return i_liked;
    }

    public boolean iDisliked() {
        return i_disliked;
    }

    public static void loadComment(Context ctx, String shipp_id, int page, ListObjectListener<Comment> listObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("page", page)
                    .put("user_id", User.getCurrentUser().getId())
                    .put("shipp_id", shipp_id);
            Request request = Requester.createPostRequest("/comment/getComments", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public String getPostedFromNow(Context context) {
        String date;
        DateTime dateTime = DateTime.parse(getCreatedAt());
        dateTime.plus(TimeZone.getDefault().getOffset(DateTime.now().getMillis()));
        int hours = Hours.hoursBetween(dateTime, DateTime.now()).getHours();
        if (hours < 1) {
            int minutes = Minutes.minutesBetween(dateTime, DateTime.now()).getMinutes();
            date = context.getString(R.string.m_ago, minutes);
        } else if (hours < 24) date = context.getString(R.string.h_ago, hours);
        else date = context.getString(R.string.d_ago, hours / 24);

        return date;
    }

    public String getLikeCountFormatted() {
        String text;
        if (like_count < 1000) text = Integer.toString(like_count);
        else if (like_count < 1000000) text = (like_count / 1000f) + "K";
        else text = (like_count / 1000000f) + "M";
        return text;
    }

    public String getUnlikeCountFormatted() {
        String text;
        if (unlike_count < 1000) text = Integer.toString(unlike_count);
        else if (unlike_count < 1000000) text = (unlike_count / 1000f) + "K";
        else text = (unlike_count / 1000000f) + "M";
        return text;
    }

    @Override
    public String toString() {
        return String.format("%s %s", getText(), getLikeCountFormatted());
    }

    public List<Comment> getSubcomments() {
        return sub_comments;
    }


    public abstract static class OnSaveListener extends ObjectListener<Comment> {

        public OnSaveListener() {
            super(Comment.class);
        }

        @Override
        public void onResult(Comment data, String error) {
            if (data != null) onResult(data);
            else onResult(null);
        }

        public abstract void onResult(Comment success);
    }
}
