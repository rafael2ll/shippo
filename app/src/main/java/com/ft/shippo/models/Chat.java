package com.ft.shippo.models;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.ft.shippo.utils.Requester;
import com.ft.shippo.utils.Types;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 06/02/18.
 */

public class Chat extends BaseObject {
    private List<User> users = new ArrayList<>();
    private Message lastMessage;


    public Chat() {
    }

    public static void createConversation(Context ctx, ObjectListener<Chat> chatObjectListener, String... user_ids) {
        try {

            JSONObject object = new JSONObject()
                    .put("users_id", TextUtils.join(",", user_ids));
            Request request = Requester.createPostRequest("/chat/create", object.toString(), chatObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            chatObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void getMyConversations(Context ctx, ListObjectListener<Chat> chatObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/chat/list", object.toString(), chatObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            chatObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void updateMyConversations(Context ctx, ListObjectListener<Chat> chatObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/chat/list", object.toString(), chatObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            chatObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void getMessages(Context ctx) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/chat/messages", object.toString(), new ListObjectListener<Message>(Types.LIST_MESSAGE) {
                @Override
                public void onResult(List<Message> data, String error) {
                    for (Message message : data) {
                        new OfflineMessage(message).save();
                    }
                }

                @Override
                public void onError(VolleyError error) {

                }
            });
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception ignored) {
        }
    }

    public static void delete(Context ctx, String chatId, BooleanListener booleanListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("chat_id", chatId);
            Request request = Requester.createPostRequest("/chat/delete", object.toString(), booleanListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            booleanListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public static void updateChat(Context ctx, String chatId, ObjectListener<Chat> chatObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("chat_id", chatId);
            Request request = Requester.createPostRequest("/chat/update", object.toString(), chatObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            chatObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }
}
