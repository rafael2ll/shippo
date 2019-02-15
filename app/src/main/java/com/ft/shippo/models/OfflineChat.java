package com.ft.shippo.models;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ft.shippo.utils.OfflineData;
import com.ft.shippo.utils.Types;
import com.ft.shippo.utils.Utils;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rafael on 06/02/18.
 */

public class OfflineChat extends SugarRecord implements Comparable<OfflineChat>{
    @Unique
    String chat_id;
    String users = "";
    boolean silenced = false;
    boolean blocked = false;
    String createdAt;

    public OfflineChat() {
    }

    public OfflineChat(Chat chat) {
        this.chat_id = chat.getId();
        users = new Gson().toJson(chat.getUsers());
        createdAt = chat.getCreatedAt();
    }

    public static boolean isOpened(String chat_id) {
        return OfflineData.getPreferences().getString("open_chat", "").equals(chat_id);
    }

    public static boolean setOpened(String chat_id) {
        return OfflineData.getEditor().putString("open_chat", chat_id).commit();
    }

    public static boolean removeOpened() {
        return OfflineData.getEditor().remove("open_chat").commit();
    }

    public List<User> getUsers() {
        Gson gson = new Gson();
        List<User> userList = gson.fromJson(users, Types.LIST_USER);
        return userList;
    }

    public OfflineMessage getLastMessage() {
        try {
            return OfflineMessage.find(OfflineMessage.class, "chatid = ?", new String[]{chat_id}, null, "id desc", "1").get(0);
        } catch (Exception ignored) {
        }
        return null;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public User getOtherUser() {
        User otherUser;
        List<User> user = getUsers();
        if (user.get(0).getId().equals(User.getCurrentUser().getId())) otherUser = user.get(1);
        else otherUser = user.get(0);

        return otherUser;
    }

    public String getChatId() {
        return chat_id;
    }

    public boolean isSilenced() {
        return silenced;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public OfflineChat update(Chat chat) {
        this.users = new Gson().toJson(chat.getUsers());
        Log.d("users", chat.getUsers().toString());
        return this;
    }

    public Date getCreatedAt() {
        try {
            return new SimpleDateFormat().parse(Utils.toLocalDate(createdAt));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int compareTo(@NonNull OfflineChat outro) {
        OfflineMessage message1 = getLastMessage();
        OfflineMessage message2 = outro.getLastMessage();
        if(message1 == null && message2 != null) return 1;
        if(message2 == null && message1 != null) return  -1;
        if(message1 == null) return getCreatedAt().compareTo(outro.getCreatedAt());
        return - message1.getCreatedAt().compareTo(message2.getCreatedAt());

    }
}
