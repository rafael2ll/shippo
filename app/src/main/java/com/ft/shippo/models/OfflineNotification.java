package com.ft.shippo.models;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;

import com.ft.shippo.R;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.orm.dsl.NotNull;
import com.orm.dsl.Unique;

import java.util.List;
import java.util.Objects;

/**
 * Created by rafael on 23/02/18.
 */

public class OfflineNotification extends SugarRecord {
    @Unique
    String _id;
    @NotNull
    String notification;
    String date;
    boolean is_seen = false;

    public OfflineNotification(){}

    public OfflineNotification(Notification notification){
        this._id = notification.getId();
        this.notification = new Gson().toJson(notification);
        date = notification.getUpdatedAt();
        is_seen = false;
    }

    public String getID() {
        return _id;
    }

    public Notification getNotification(){
        return new Gson().fromJson(notification, Notification.class);
    }

    public static List<OfflineNotification> findByID(String id) {
        return find(OfflineNotification.class, "_id = ?", id);
    }

    public void update(Notification notification) {
        this._id = notification.getId();
        if(!Objects.equals(date, notification.getUpdatedAt())) is_seen = false;
        this.notification = new Gson().toJson(notification);
        date = notification.getUpdatedAt();
    }

    public Shipp getShipp() {
        return getNotification().getShipp();
    }

    public Spanned likeText(Context ctx, boolean isLike) {
        StringBuilder text = new StringBuilder();
        String you = ctx.getString(R.string.you);
        Shipp shipp = getNotification().getShipp();
        User related[] = getNotification().getRelated();
        int i = related.length >= 3 ? 3 : related.length;

        if(i == 1){
            boolean isme = User.isMe(related[0]);
            text.append("<b>").append(isme? you : related[0].getFirstname()).append("</b>");
        }else if(i ==2){
            boolean isme = User.isMe(related[0]);
            boolean isme2 = User.isMe(related[1]);
            text.append("<b>").append(isme? you : related[0].getFirstname()).append("</b>");
            text.append(" ").append(ctx.getString(R.string.and)).append(" ");
            text.append("<b>").append(isme2? you : related[1].getFirstname()).append("</b>");
        }else if(i ==3 ){
            boolean isme = User.isMe(related[0]);
            boolean isme2 = User.isMe(related[1]);
            boolean isme3 = User.isMe(related[2]);
            text.append("<b>").append(isme? you: related[0].getFirstname()).append("</b>").append(", ");
            text.append("<b>").append(isme2? you: related[1].getFirstname()).append("</b>");
            if(getNotification().getRelatedCount() > 3) text.append(", ");
            else text.append(" ").append(ctx.getString(R.string.and)).append(" ");
            text.append("<b>").append(isme ? you : related[2].getFirstname()).append("</b>");
        }
        int rest = getNotification().getRelatedCount() - i;
        if(rest > 0) text.append(" ").append(ctx.getString(isLike? R.string.like_shipp : R.string.unlike_shipp, rest, User.isMe(shipp.getUser1()) ? you : shipp.getUser1().getFirstname(),
                User.isMe(shipp.getUser2()) ? you : shipp.getUser2().getFirstname()));
        else text.append(" ").append(String.format(ctx.getResources().getQuantityText(isLike ? R.plurals.like_plural: R.plurals.unlike_plural, i).toString(), User.isMe(shipp.getUser1()) ? you : shipp.getUser1().getFirstname(),
                User.isMe(shipp.getUser2()) ? you : shipp.getUser2().getFirstname()));
        return Html.fromHtml(text.toString());
    }

    public Spanned commentText(Context ctx) {
        StringBuilder text = new StringBuilder();
        String you = ctx.getString(R.string.you);
        Shipp shipp = getNotification().getShipp();
        User related[] = getNotification().getRelated();
        int i = related.length;

        if(i == 1){
            text.append("<b>").append(related[0].getFirstname()).append("</b>");
        }else if(i ==2){
            text.append("<b>").append(related[0].getFirstname()).append("</b>");
            text.append(" ").append(ctx.getString(R.string.and)).append(" ");
            text.append("<b>").append(related[1].getFirstname()).append("</b>");
        }else if(i ==3 ){
            text.append("<b>").append(related[0].getFirstname()).append("</b>").append(", ");
            text.append("<b>").append(related[1].getFirstname()).append("</b>");
            if(getNotification().getRelatedCount() > 3) text.append(", ");
                else text.append(" ").append(ctx.getString(R.string.and)).append(" ");
            text.append("<b>").append(related[2].getFirstname()).append("</b>");
        }
        int rest = getNotification().getRelatedCount() - i;
        if(rest > 0) text.append(" ").append(String.format(ctx.getText(R.string.commented_shipp).toString(), rest, User.isMe(shipp.getUser1()) ? you : shipp.getUser1().getFirstname(),
                User.isMe(shipp.getUser2()) ? you : shipp.getUser2().getFirstname()));
        else text.append(" ").append(String.format(ctx.getResources().getQuantityText(R.plurals.comments_plural, i).toString(), User.isMe(shipp.getUser1()) ? you : shipp.getUser1().getFirstname(),
                User.isMe(shipp.getUser2()) ? you : shipp.getUser2().getFirstname()));
        return Html.fromHtml(text.toString());
    }

    public boolean isShipp() {
        return getNotification().getType() == 0;
    }

    public void setSeen() {
        is_seen = true;
    }

    public boolean isSeen() {
        return is_seen;
    }
}
