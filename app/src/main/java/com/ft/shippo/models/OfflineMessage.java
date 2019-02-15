package com.ft.shippo.models;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;

import com.android.volley.error.VolleyError;
import com.ft.shippo.utils.Types;
import com.ft.shippo.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;
import com.orm.dsl.NotNull;
import com.orm.dsl.Unique;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rafael on 06/02/18.
 */

public class OfflineMessage extends SugarRecord {
    String body;
    @NotNull
    String userid;
    @NotNull
    String chat_id;
    @Unique
    String _id;
    String seen_by;
    String created_at;
    boolean isSent = false;
    boolean isSending = false;
    boolean iSaw = false;
    String content_type;
    boolean downloaded = false;
    String local_path;
    String metadata;

    public OfflineMessage() {
    }

    public OfflineMessage(String body, String userid, String chat_id, String content_type, String metadata, DateTime now) {
        this.body = body;
        this.userid = userid;
        seen_by = "";
        this.chat_id = chat_id;
        this.metadata = metadata;
        this.created_at = now.toDateTimeISO().toDateTime(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm'Z'");
        this.content_type = content_type;
    }

    public OfflineMessage(Message message) {
        this._id = message.getId();
        this.body = message.getBody();
        this.userid = message.getUser().getId();
        this.created_at = message.getCreatedAt();
        this.seen_by = new Gson().toJson(message.getSeenBy());
        this.iSaw = seen_by.contains(User.getCurrentUser().getId());
        this.chat_id = message.getChatId();
        if (!message.getContentType().equals(Types.MESSSAGE_TEXT))
            this.metadata = message.getMetadata().toString();
        this.content_type = message.getContentType();
    }

    public String getBody() {
        return body;
    }

    public void addSeenBy(String user) {
        List<String> list = new Gson().fromJson(seen_by, new TypeToken<List<String>>() {
        }.getType());
        list.add(user);
        seen_by = new Gson().toJson(list);
    }


    public void update(Message message) {
        this._id = message.getId();
        this.body = message.getBody();
        this.userid = message.getUser().getId();
        this.created_at = message.getCreatedAt();
        this.seen_by = new Gson().toJson(message.getSeenBy());
        this.iSaw = message.getSeenBy().contains(User.getCurrentUser().getId());
        if (!message.getContentType().equals(Types.MESSSAGE_TEXT))
            this.metadata = message.getMetadata().toString();
    }

    public Date getCreatedAt() {
        try {
            return new SimpleDateFormat().parse(Utils.toLocalDate(created_at));
        } catch (ParseException e) {
            e.printStackTrace();
            return  null;
        }
    }

    public boolean isMine() {
        return userid.equals(User.getCurrentUser().getId());
    }

    public void isSent(boolean b) {
        this.isSent = b;
    }

    public String getChatId() {
        return chat_id;
    }

    public String getContentType() {
        return content_type;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getLocalPath() {
        return local_path;
    }

    public void setLocalPath(String local_path) {
        this.local_path = local_path;
    }

    public JSONObject getMetadata() {
        try {
            return new JSONObject(metadata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void loadMetadata(Context ctx, File audioFile) {
        setLocalPath(audioFile.getAbsolutePath());
        try {
            JSONObject metadata = new JSONObject()
                    .put("name", audioFile.getName())
                    .put("size", audioFile.length())
                    .put("duration", MediaPlayer.create(ctx, Uri.fromFile(audioFile)).getDuration());
            setMetadata(metadata.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isNotSeen() {
        return !iSaw;
    }

    public boolean otherSaw() {
        List<String> list = new Gson().fromJson(seen_by, new TypeToken<List<String>>() {
        }.getType());
        if (list == null) return false;
        else return list.size() > 1;
    }

    public void setRead(Context read) {
        iSaw = true;
        this.save();
        final OfflineMessage message = this;
        Message.setRead(read, this, new ObjectListener<Message>(Message.class) {
            @Override
            public void onResult(Message data, String error) {
                message.update(data);
                message.save();
            }

            @Override
            public void onError(VolleyError error) {
                message.iSaw = false;
                message.save();
            }
        });
    }

    public String getID() {
        return _id;
    }

    public String getUser() {
        return userid;
    }

    public boolean getIsSent() {
        return isSent;
    }

    public DateTime getLocalCreatedAt() {
        DateTime dateTime = new DateTime(getCreatedAt());
        return dateTime;
    }

    public boolean isAudio() {
        return content_type.equals(Types.MESSSAGE_AUDIO);
    }

    public boolean isPicture() {
        return content_type.equals(Types.MESSSAGE_PICTURE);
    }

    public boolean isText() {
        return content_type.equals(Types.MESSSAGE_TEXT);
    }

    public boolean isSending() {
        return isSending;
    }

    public void setSending(boolean sending) {
        isSending = sending;
    }

    public void loadImageMetadata(Context ctx, File image) {
        setLocalPath(image.getPath());
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(image.getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            JSONObject metadata = new JSONObject()
                    .put("name", image.getName())
                    .put("size", image.length())
                    .put("width", imageWidth)
                    .put("height", imageHeight);
            setMetadata(metadata.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
