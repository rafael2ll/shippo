package com.ft.shippo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class OfflineData {
    private static final String SHARED_NAME = "Shippo";
    private static Context ctx;
    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mEditor;
    private static Gson gson;

    public static void init(Context x) {
        ctx = x;
        mPreferences = ctx.getSharedPreferences(SHARED_NAME, x.MODE_PRIVATE);
        mEditor = mPreferences.edit();
        gson = new Gson();
    }

    public static SharedPreferences getPreferences() {
        return mPreferences;
    }

    public static SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    public static void write(String tag, Object object, Type p) {
        if (p == Boolean.class) mEditor.putBoolean(tag, (boolean) object);
        else
            mEditor.putString(tag, Base64.encodeToString(gson.toJson(object, p).getBytes(), Base64.DEFAULT));
        mEditor.commit();
    }

    public static Object read(String tag, Type p) {
        if (p == Boolean.class) {
            return mPreferences.getBoolean(tag, false);
        }
        try {
            return gson.fromJson(new String(Base64.decode(mPreferences.getString(tag, "").getBytes(), Base64.DEFAULT)), p);
        } catch (Exception e) {
            Log.w("FireCrasher.err", e);
            return null;
        }
    }

    public class ListTyper<T> {
        public Type get() {
            Type p = new TypeToken<List<T>>() {
            }.getType();
            return p;
        }
    }
}
