package com.ft.shippo.models;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.ft.shippo.utils.Requester;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by rafael on 23/02/18.
 */

public class Notification  extends BaseObject {
    private int type;
    private User users_id[];
    private Shipp shipp_id;
    private User latest[];
    private int total_count = 0;
    public Notification() {}

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public User[] getUsers() {
        return users_id;
    }

    public void setUsers(User[] users) {
        this.users_id = users;
    }

    public Shipp getShipp() {
        return shipp_id;
    }


    public static void getNotifcation(Context ctx, ListObjectListener<Notification> listObjectListener){
        try{
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
                    //.put("start_at", OfflineNotification.last(OfflineNotification.class).getID());

            Request request = Requester.createPostRequest("/user/notifications", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        }catch (Exception ignored){}
    }

    public User[] getRelated() {
        return latest;
    }

    public int getRelatedCount() {
        return total_count;
    }

    public static void updateNotfications(Context mainActivity) {
        getNotifcation(mainActivity, new ListObjectListener<Notification>(new TypeToken<List<Notification>>(){}.getType()) {
            @Override
            public void onResult(List<Notification> data, String error) {
                for(Notification notification : data) {
                    OfflineNotification offlineNotification = new OfflineNotification(notification);
                    List<OfflineNotification> offlineNotifications = OfflineNotification.findByID(notification.getId());
                    if (offlineNotifications.size() > 0) {
                        offlineNotification = offlineNotifications.get(0);
                        offlineNotification.update(notification);
                        offlineNotification.save();
                    } else {
                        offlineNotification.save();
                    }
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }
}
