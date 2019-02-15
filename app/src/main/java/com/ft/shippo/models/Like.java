package com.ft.shippo.models;

import android.content.Context;

import com.android.volley.Request;
import com.ft.shippo.utils.Requester;

/**
 * Created by rafael on 21/01/18.
 */

public class Like {
    private String user_id;
    private String shipp_id;

    public Like(String user_id, String shipp_id) {
        this.user_id = user_id;
        this.user_id = user_id;
    }

    public static void likeShipp(Context ctx, boolean isLiked, String shipp_id, String user_id, BooleanListener listener) {
        Request request = Requester.createPostRequest(isLiked ? "/like/notlike" : "/like/like", String.format("{user_id : %s, shipp_id: %s}", user_id, shipp_id), listener);
        Requester.getInstance(ctx).addToRequestQueue(request);
    }

    public static void dislikeShipp(Context ctx, boolean isDisliked, String shipp_id, String user_id, BooleanListener listener) {
        Request request = Requester.createPostRequest(isDisliked ? "/like/notdislike" : "/like/dislike", String.format("{user_id : %s, shipp_id: %s}", user_id, shipp_id), listener);
        Requester.getInstance(ctx).addToRequestQueue(request);
    }

    public static void likeComment(Context ctx, boolean isLiked, String shipp_id, String user_id, BooleanListener listener) {
        Request request = Requester.createPostRequest(isLiked ? "/comment/notlike" : "/comment/like", String.format("{user_id : %s, comment_id: %s}", user_id, shipp_id), listener);
        Requester.getInstance(ctx).addToRequestQueue(request);
    }

    public static void dislikeComment(Context ctx, boolean isDisliked, String shipp_id, String user_id, BooleanListener listener) {
        Request request = Requester.createPostRequest(isDisliked ? "/comment/notdislike" : "/comment/dislike", String.format("{user_id : %s, comment_id: %s}", user_id, shipp_id), listener);
        Requester.getInstance(ctx).addToRequestQueue(request);
    }


    public static void isLiked(Context ctx, String shipp_id, String user_id, BooleanListener listener) {
        Request request = Requester.createPostRequest("/like/isLiked", String.format("{user_id : %s, shipp_id: %s}", user_id, shipp_id), listener);
        Requester.getInstance(ctx).addToRequestQueue(request);
    }

    public static void isDisliked(Context ctx, String user_id, String shipp_id, BooleanListener listener) {
        Request request = Requester.createPostRequest("/like/isDisliked", String.format("{user_id : %s, shipp_id: %s}", user_id, shipp_id), listener);
        Requester.getInstance(ctx).addToRequestQueue(request);
    }

}
