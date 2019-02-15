package com.ft.shippo.utils;

import com.ft.shippo.models.Action;
import com.ft.shippo.models.Chat;
import com.ft.shippo.models.Message;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.models.User;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Types {
    public static final Type LIST_USER = new TypeToken<List<User>>() {
    }.getType();
    public static final Type LIST_CHAT = new TypeToken<List<Chat>>() {
    }.getType();
    public static final Type LIST_ACTION = new TypeToken<List<Action>>() {
    }.getType();
    public static final String MESSSAGE_TEXT = "TEXT";
    public static final Type LIST_MESSAGE = new TypeToken<List<Message>>() {
    }.getType();
    public static final Type LIST_SHIPP = new TypeToken<List<Shipp>>() {
    }.getType();

    public static final String MESSSAGE_AUDIO = "AUDIO";
    public static final String MESSSAGE_PICTURE = "PICTURE";
    public static String CURRENT_USER = "current_user";
}
