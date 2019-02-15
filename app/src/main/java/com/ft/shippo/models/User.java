package com.ft.shippo.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.ft.shippo.SettingActivity;
import com.ft.shippo.utils.OfflineData;
import com.ft.shippo.utils.Requester;
import com.ft.shippo.utils.Types;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 16/01/18.
 */

public class User extends BaseObject {

    private String username = "";
    private String birth = "";
    private String photoUri = "";
    private String bio = "";
    private String password;
    private double location[] = new double[2];
    private List<User> following = new ArrayList<>();
    private List<User> followers = new ArrayList<>();
    private List<Shipp> mine = new ArrayList<>();
    private List<Shipp> favorites = new ArrayList<>();
    private String email;
    private String country;
    private String city;
    private String city_name;
    private String country_name;
    private String fb_token;
    private String lastOnline;
    private String gender;
    private boolean following_me = false;
    private boolean is_private = false;
    private int following_count;
    private int follower_count;
    private String fcm_token;
    private String fb_id;
    private boolean is_pending = false;
    private boolean is_online = false;

    public User(String username, String email, String fb_token, String birth, String photoUri, String bio, boolean gender) {
        this.username = username;
        this.birth = birth;
        this.email = email;
        followers.add(null);

        this.fb_token = fb_token;
        this.photoUri = photoUri;
        this.bio = bio;
        this.gender = Boolean.toString(gender);
    }

    public User() {

    }

    public static User getCurrentUser() {
        return (User) OfflineData.read(Types.CURRENT_USER, User.class);
    }

    public synchronized static void findUserByName(Context ctx, int page, String search, ListObjectListener<User> listObjectListener) {
        try {
            JSONObject object = new JSONObject();
            object.put("user_id", User.getCurrentUser().getId())
                    .put("text", search)
                    .put("page", page);

            Request request = Requester.createPostRequest("/user/listUsersByName", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public synchronized static void search(Context ctx, int page, String search, BaseObjectListener listObjectListener) {
        try {
            JSONObject object = new JSONObject();
            object.put("user_id", User.getCurrentUser().getId())
                    .put("text", search)
                    .put("page", page);
            Request request = Requester.createPostRequest("/user/search", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onErrorResponse(new VolleyError(e.getMessage()));
        }
    }

    public static void findOneByField(Context ctx, String child, String value, String params, ObjectListener<User> objectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("child", child)
                    .put("value", value);
            if (User.getCurrentUser() != null) object.put("user_id", User.getCurrentUser().getId());
            if (params != null) object.put("params", params);
            Request request = Requester.createPostRequest("/user/findByFieldAndValue", object.toString(), objectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            FirebaseCrash.report(e);
            objectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void findFollowing(Context ctx, String user_id, int page, ListObjectListener<User> listObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("my_id", User.getCurrentUser().getId())
                    .put("page", page)
                    .put("finding_id", user_id);
            Request request = Requester.createPostRequest("/user/getFollowing", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void findFollowers(Context ctx, String user_id, int page, ListObjectListener<User> listObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("my_id", User.getCurrentUser().getId())
                    .put("page", page)
                    .put("finding_id", user_id);
            Request request = Requester.createPostRequest("/user/getFollowers", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void logOff() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (firebaseUser != null) FirebaseAuth.getInstance().signOut();
        if (accessToken != null) LoginManager.getInstance().logOut();
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        User.removeLocally();
    }

    private static void removeLocally() {
        OfflineData.write(Types.CURRENT_USER, null, User.class);
    }

    public static void silence(Context ctx, String id, int silence_option, ObjectListener<User> listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("silence_id", id)
                    .put("priority", silence_option);
            Request request = Requester.createPostRequest("/user/silence", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void setOnline(Context ctx) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/user/online", object.toString(), null);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {

        }
    }

    public boolean isPending() {
        return is_pending;
    }

    public void setIsPending(boolean is_pending) {
        this.is_pending = is_pending;
    }

    public static void setOffline(Context ctx) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/user/offline", object.toString(), null);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {

        }
    }

    public String getFirstname() {
        return username.split(" ")[0];
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public User setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
        return this;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(String lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean getMan() {
        return Boolean.parseBoolean(gender);
    }

    public String getCountryName() {
        return country_name;
    }

    public boolean isFollowingMe() {
        return following_me;
    }

    public int getFollowingCount() {
        return following_count;
    }

    public int getFollowerCount() {
        return follower_count;
    }

    public double[] getLocation() {
        return location;
    }

    public void setLocation(double[] location) {
        this.location = location;
    }

    public List<User> getFollowing() {
        return following;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public List<Shipp> getMine() {
        return mine;
    }

    public List<Shipp> getFavorites() {
        return favorites;
    }

    public void save(Context ctx, ObjectListener<User> mListener) {
        Requester.getInstance(ctx).addToRequestQueue(Requester.createPostRequest("/user/createUser",
                new Gson().toJson(User.this), mListener));
    }

    public void updatePrivateAccount(Context ctx, final boolean is_private) {
        this.is_private = is_private;
        saveLocally();
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", getId())
                    .put("is_private", is_private);
            Request request = Requester.createPostRequest("/user/updatePrivate", object.toString(), new BooleanListener() {

                @Override
                public void onResult(boolean data, String error) {

                }

                @Override
                public void onError(VolleyError error) {
                    User u = getCurrentUser();
                    u.is_private = !u.is_private;
                    u.saveLocally();
                    error.printStackTrace();
                }
            });
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception ignored) {
        }

    }

    public void updateLocation(Context ctx, Location location) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", getId())
                    .put("lat", location.getLatitude())
                    .put("lng", location.getLongitude());
            Request request = Requester.createPostRequest("/user/location", object.toString(), new ObjectListener<User>(User.class) {
                @Override
                public void onResult(User data, String error) {
                    if (data != null) data.saveLocally();
                }

                @Override
                public void onError(VolleyError error) {
                    error.printStackTrace();
                }
            });
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLocally() {
        OfflineData.write(Types.CURRENT_USER, User.this, User.class);
    }

    public boolean gender() {
        return getMan();
    }

    public void setMan(boolean man) {
        gender = Boolean.toString(man);
    }

    public int getAge() {
        int[] fields = new int[]{Integer.parseInt(birth.split("/")[0]), Integer.parseInt(birth.split("/")[1]), Integer.parseInt(birth.split("/")[2])};
        int age = Years.yearsBetween(new LocalDate().withDayOfMonth(fields[0])
                .withMonthOfYear(fields[1]).withYear(fields[2]), LocalDate.now()).getYears();
        return age;
    }

    public void setIsFollowingMe(boolean isFollowingMe) {
        this.following_me = isFollowingMe;
    }

    public void follow(Context ctx, String id, BooleanListener booleanListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("follow_id", id);
            Request request = Requester.createPostRequest("/follow/startFollow", object.toString(), booleanListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            FirebaseCrash.report(e);
            booleanListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public void unfollow(Context ctx, String id, BooleanListener booleanListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("follow_id", id);
            Request request = Requester.createPostRequest("/follow/stopFollow", object.toString(), booleanListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            FirebaseCrash.report(e);
            booleanListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public String getCityName() {
        return city_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFBToken(AccessToken FBToken) {
        this.fb_token = FBToken.getToken();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        if (country != null) {
            this.country = country.getId();
            this.country_name = country.getName();
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city.getId();
        this.city_name = city.getName();
    }

    public String getFollowingCountFormatted() {
        String text;
        int count = getFollowingCount();
        if (count < 1000) text = "" + count;
        else if (count < 1000000) text = "" + count / 1000 + "K";
        else text = "" + count / 1000000 + "M";
        return text;
    }

    public String getFollowerCountFormatted() {
        String text;
        int count = getFollowerCount();
        if (count < 1000) text = "" + count;
        else if (count < 1000000) text = "" + count / 1000 + "K";
        else text = "" + count / 1000000 + "M";
        return text;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public boolean isPrivate() {
        return is_private;
    }

    public void setFCMToken(Context ctx, String FCMToken, ObjectListener<User> listener) {
        this.fcm_token = FCMToken;
        saveLocally();
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("fcm_token", fcm_token);
            Request request = Requester.createPostRequest("/user/updateFCM", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void setFBasPhoto(Context ctx, BooleanListener listener) {

        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("photo","https://graph.facebook.com/" + getCurrentUser().getFbId() + "/picture?height=500");
            Request request = Requester.createPostRequest("/user/setFBasPhoto", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public String getFCMToken() {
        return fcm_token;
    }

    public static boolean isMe(User owner) {
        return getCurrentUser().getId().equals(owner.getId());
    }

    public void setFbId(String fbId) {
        this.fb_id = fbId;
    }

    public Object getFbId() {
        return fb_id;
    }

    public boolean isMan() {
        return gender();
    }

    public static void updateProfilePhoto(Context ctx, File file, BooleanListener objectListener) {
        try {

            Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
            File newFile = new File(ctx.getCacheDir(), "PROFILE_PIC_" + System.currentTimeMillis() + ".png");
            image.compress(Bitmap.CompressFormat.PNG, 90, new FileOutputStream(newFile));
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createFilePostRequest("/user/uploadPhoto", object.toString(), newFile, objectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            objectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void setNoPhoto(Context ctx, BooleanListener listener) {

        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("photo", "none");
            Request request = Requester.createPostRequest("/user/setFBasPhoto", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void updateBio(Context ctx, String bio, BooleanListener listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("bio", bio);
            Request request = Requester.createPostRequest("/user/updateBio", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void reportShipp(Context ctx, String id) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId())
                    .put("shipp_id", id);
            Request request = Requester.createPostRequest("/shipp/report", object.toString(), null);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
        }
    }

    public static void imLucky(Context ctx, ListObjectListener<User> listObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/user/imLucky", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void login(Context ctx, String email, String password, ObjectListener<User> listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("email", email)
                    .put("password", password);
            Request request = Requester.createPostRequest("/user/login", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {

        }
    }

    public static void deleteAccount(Context ctx, String password, BooleanListener listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("password", password)
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/user/deleteAccount", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {

        }
    }

    public static void acceptFollow(Context ctx, String user_id, BooleanListener listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("accept_id", user_id)
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/follow/acceptRequest", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {

        }
    }

    public static void requestFollow(Context ctx, String user_id, BooleanListener listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("follow_id", user_id)
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/follow/requestFollow", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {

        }
    }

    public void setIsOnline(boolean isOnline) {
        this.is_online = isOnline;
    }

    public boolean isOnline() {
        return is_online;
    }

    public static void linkWithFacebook(Context ctx,String fb_id, AccessToken fb_token, BooleanListener listener) {
        try {
            User u = getCurrentUser();
            u.setFbId(fb_id);
            u.setPhotoUri("https://graph.facebook.com/" + u.getFbId() + "/picture?height=500");
            u.setFBToken(fb_token);
            u.saveLocally();
            JSONObject object = new JSONObject()
                    .put("fb_id", fb_id)
                    .put("fb_token", fb_token.getToken())
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/user/linkWithFacebook", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception ignored) {

        }
    }

    public static void unlinkWithFacebook(Context ctx, BooleanListener listener) {
        try {
            JSONObject object = new JSONObject()
                    .put("user_id", User.getCurrentUser().getId());
            Request request = Requester.createPostRequest("/user/unlinkWithFacebook", object.toString(), listener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception ignored) {

        }
    }
}
