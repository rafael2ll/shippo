package com.ft.shippo.models;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.ft.shippo.utils.Requester;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 21/01/18.
 */

public class Shipp extends BaseObject {

    private User owner;
    private User user1;
    private User user2;
    private String label;

    private int likeCount;
    private int unlikeCount;
    private int commentCount;

    private String country_id[] = new String[3];
    private double location[] = new double[2];


    private List<Tag> tags = new ArrayList<>();
    private boolean i_disliked = false;
    private boolean i_liked = false;


    public Shipp(User owner, User user1, User user2, String label) {
        this.owner = owner;
        this.user1 = user1;
        this.user2 = user2;
        this.label = label;
        this.country_id[0] = owner.getCountry();
        this.country_id[1] = user1.getCountry();
        this.country_id[2] = user2.getCountry();
        this.location = owner.getLocation();
    }

    public Shipp() {

    }

    public static void findUserShipps(Context ctx, String id, int page, ListObjectListener listObjectListener) {
        JSONObject data = new JSONObject();
        try {
            data.put("page", page)
                    .put("user_id", id)
                    .put("locale", ctx.getResources().getConfiguration().locale.getCountry());
            Request request = Requester.createPostRequest("/shipp/findUserShipps", data.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }
    public static void findShipps(Context ctx,String text, int page, ListObjectListener listObjectListener) {
        JSONObject data = new JSONObject();
        try {
            data.put("page", page)
                    .put("user_id", User.getCurrentUser().getId())
                    .put("locale", User.getCurrentUser().getCountry())
                    .put("usernames", text);
            Request request = Requester.createPostRequest("/shipp/find", data.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void trending(Context ctx, int page, int type,  String country, boolean isFromMyFriends, ListObjectListener<Shipp> listObjectListener) {
        JSONObject data = new JSONObject();
        try {
            data.put("page", page)
                    .put("user_id", User.getCurrentUser().getId())
                    .put("my_friends", isFromMyFriends);
            if(type == 1) data.put("country", User.getCurrentUser().getCountry());
            if(type == 2) data.put("lat", User.getCurrentUser().getLocation()[0])
                            .put("lng", User.getCurrentUser().getLocation()[1]);
            if(type == 3) data.put("city", User.getCurrentUser().getCity());
            if(type == 4) data.put("country", country);
            Request request = Requester.createPostRequest("/shipp/trending", data.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
        this.location = owner.getLocation();
        this.country_id[0] = owner.getCountry();
        Log.d(getClass().getSimpleName(), String.format("%f%f %s", location[0], location[1], country_id[0]));
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
        this.country_id[1] = user1.getCountry();
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
        this.country_id[2] = user2.getCountry();
    }

    public String getLabel() {
        return label == null ? "" : label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getUnlikeCount() {
        return unlikeCount;
    }

    public void setUnlikeCount(int unlikeCount) {
        this.unlikeCount = unlikeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void save(Context ctx, BooleanListener listener) {
        JSONObject data = new JSONObject();
        try {
            data.put("owner", getOwner().getId())
                    .put("user1", getUser1().getId())
                    .put("user2", getUser2().getId())
                    .put("lat", location[0])
                    .put("lng", location[1])
                    .put("country_0", country_id[0])
                    .put("country_1", country_id[1])
                    .put("country_2", country_id[2])
                    .put("label", getLabel())
                    .put("usernames", String.format("%s %s %s", getOwner().getUsername(), getUser1().getUsername(), getUser2().getUsername()));
            Request request = Requester.createPostRequest("/shipp/create", data.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public boolean iDisliked() {
        return i_disliked;
    }

    public boolean iLiked() {
        return i_liked;
    }

    public static void delete(Context ctx, String id) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("shipp_id", id);
            Request request = Requester.createPostRequest("/shipp/delete", object.toString(), null);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
        }
    }

    public static void share(Context ctx, String id, BooleanListener listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("shipp_id", id);
            Request request = Requester.createPostRequest("/shipp/share", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
        }
    }
}
