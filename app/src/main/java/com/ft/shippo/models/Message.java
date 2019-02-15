package com.ft.shippo.models;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.ft.shippo.utils.Requester;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 06/02/18.
 */

public class Message extends BaseObject {
    private String body;
    private User owner;
    private List<String> seen_by = new ArrayList<>();
    private String chat_id;
    private String content_type;
    private String metadata;

    public Message(String body, User owner, String chat_id, String content_type) {
        this.body = body;
        this.owner = owner;
        this.chat_id = chat_id;
        this.content_type = content_type;
    }

    public static void sendMessage(Context ctx, String body, String c_type, String chat_id, ObjectListener<Message> listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("owner", User.getCurrentUser().getId())
                    .put("text", body.trim())
                    .put("content_type", c_type)
                    .put("chat_id", chat_id);
            Request request = Requester.createPostRequest("/chat/createMessage", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void setRead(Context ctx, OfflineMessage message, ObjectListener<Message> listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("message_id", message.getID())
                    .put("chat_id", message.getChatId());
            Request request = Requester.createPostRequest("/chat/messageSeen", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public void send(Context ctx, ObjectListener<Message> listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("owner", owner.getId())
                    .put("text", body)
                    .put("chat_id", chat_id)
                    .put("content_type", content_type);
            Request request = Requester.createPostRequest("/chat/createMessage", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public JSONObject getMetadata() {
        try {
            if (metadata != null) return new JSONObject(metadata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public void sendAsAudio(Context ctx, File audioFile, ObjectListener<Message> listener) {
        try {
            JSONObject metadata = new JSONObject()
                    .put("name", audioFile.getName())
                    .put("size", audioFile.length())
                    .put("duration", MediaPlayer.create(ctx, Uri.fromFile(audioFile)).getDuration());

            JSONObject object = new JSONObject()
                    .put("owner", User.getCurrentUser().getId())
                    .put("content_type", content_type)
                    .put("chat_id", chat_id)
                    .put("metadata", metadata.toString());
            Request request = Requester.createFilePostRequest("/chat/newAudioMessage", object.toString(), audioFile, listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public void sendAsImage(Context ctx, File imageFile, ObjectListener<Message> listener) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            JSONObject metadata = new JSONObject()
                    .put("name", imageFile.getName())
                    .put("size", imageFile.length())
                    .put("width", imageWidth)
                    .put("height", imageHeight);

            JSONObject object = new JSONObject()
                    .put("owner", User.getCurrentUser().getId())
                    .put("content_type", content_type)
                    .put("chat_id", chat_id)
                    .put("metadata", metadata.toString());
            Request request = Requester.createFilePostRequest("/chat/newImageMessage", object.toString(), imageFile, listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public User getUser() {
        return owner;
    }

    public String getBody() {
        return body;
    }

    public String getChatId() {
        return chat_id;
    }

    public List<String> getSeenBy() {
        return seen_by;
    }

    public String getContentType() {
        return content_type;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
