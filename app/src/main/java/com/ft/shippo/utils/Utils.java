package com.ft.shippo.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.Console;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rafael on 16/01/18.
 */

public class Utils {
    public static boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //Check password with minimum requirement here(it should be minimum 6 characters)
    public static boolean isPasswordValid(String password, String username) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "(?=\\S+$).{6,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches() && password.length() >= 6 && !password.trim().contains(username.trim());
    }

    public static boolean isAnyNull(String... params) {
        for (String param : params) {
            if (TextUtils.isEmpty(param)) return true;
        }
        return false;
    }


    public static void expandOrCollapse(final View v, String exp_or_colpse) {
        TranslateAnimation anim = null;
        if (exp_or_colpse.equals("expand")) {
            anim = new TranslateAnimation(0.0f, 0.0f, -v.getHeight(), 0.0f);


            v.setVisibility(View.VISIBLE);
        } else {
            anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, -v.getHeight());
            Animation.AnimationListener collapselistener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.GONE);
                }
            };
            anim.setAnimationListener(collapselistener);
        }
        // To Collapse
        anim.setDuration(300);
        anim.setInterpolator(new AccelerateInterpolator(0.5f));
        v.startAnimation(anim);
    }

    public static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    public static void clearApplicationData(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s + " DELETED");
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static int getCountryImage(Context context, String countryName) {
        String json;
        try {
            InputStream inputStream = context.getAssets().open("countries.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
            JSONObject object = new JSONObject(json);
            Iterator<String> countryIterator = object.getJSONObject("countries").keys();
            while (countryIterator.hasNext()) {
                String country = countryIterator.next();
                JSONObject countryJSON = object.getJSONObject("countries").getJSONObject(country);
                if (Objects.equals(country, "DO")) country = "doo";
                if (countryJSON.getString("name").equals(countryName))
                    return context.getResources().getIdentifier(country.toLowerCase().replace("-", "_"), "raw", context.getPackageName());

            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    public static Typeface getShippoTypeface(FragmentActivity ctx) {
        return Typeface.createFromAsset(ctx.getAssets(), "fonts/pacifico_regular.ttf");
    }

    public static String formatSeconds(long l) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss", Locale.getDefault());
        return formatter.format(l * 1000L);
    }

    public static int getAttributeColor(Context context, Resources.Theme theme, int attributeId) {
        TypedValue typedValue = new TypedValue();
        if(theme == null)context.getTheme().resolveAttribute(attributeId, typedValue, true);
        else theme.resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("Utils", "Not found color resource by id: " + colorRes);
        }
        return color;
    }

    public static int[] getColors(Context ctx, int... colorsRes) {
        int colors[] = new int[colorsRes.length];
        int i =0;
        for(int color : colorsRes) colors[i++]= ContextCompat.getColor(ctx, color);
        return colors;
    }

    public static Drawable getDrawableThemed(Context ctx, @DrawableRes int res, Resources.Theme theme) {
        Drawable drawable = ContextCompat.getDrawable(ctx, res);
        DrawableCompat.applyTheme(drawable, theme);
        drawable.invalidateSelf();
        return drawable;
    }

    public static Drawable rightShippDrawable(int color) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setDither(true);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(8);
        gradientDrawable.setColor(color);
        gradientDrawable.setStroke(3, Color.WHITE);
        return gradientDrawable;
    }

    public static Drawable leftShippDrawable(int color) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setDither(true);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(8);
        gradientDrawable.setColor(color);
        gradientDrawable.setStroke(3, Color.WHITE);
        InsetDrawable drawable = new InsetDrawable(gradientDrawable, 0, 0, -5, 0);
        return drawable;
    }

    public static String toLocalDate(String createdAt) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(tz);
        Date date = new DateTime(createdAt).toDate();
        Log.d("date", date.toString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        return simpleDateFormat.format(date);
    }
}
