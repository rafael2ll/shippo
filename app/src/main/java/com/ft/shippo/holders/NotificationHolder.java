package com.ft.shippo.holders;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.Notification;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Utils;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Weeks;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 23/02/18.
 */

public class NotificationHolder extends BaseHolder<OfflineNotification> {

    @BindView(R.id.civ)
    CircleImageView civProfilePic;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.iv_type)
    ImageView imageViewType;
    @BindView(R.id.holder_notification_follow)
    TextView followRequest;
    @BindView(R.id.holder_notification_date)
    TextView textViewDate;
    @BindView(R.id.holder_notification_new)
    ImageView imageViewNew;
    Context ctx;
    OfflineNotification notification;

    public NotificationHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    @Override
    public void feedWith(Context ctx, OfflineNotification o, boolean isSelected) {
        notification = o;
        this.ctx = ctx;

        GlideRequestManager.get()
                .load(o.isShipp() ? o.getNotification().getShipp().getOwner().getPhotoUri() : o.getNotification().getRelated()[0].getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .into(civProfilePic);
        followRequest.setVisibility(View.GONE);
        textViewDate.setVisibility(View.VISIBLE);
        Notification not = o.getNotification();
        LocalDateTime dateTime;
        try {
            dateTime = new DateTime(new SimpleDateFormat().parse(Utils.toLocalDate(not.getUpdatedAt()))).toLocalDateTime();
        } catch (ParseException e) {
            e.printStackTrace();
            dateTime = LocalDateTime.now();
        }
        LocalDateTime now = LocalDateTime.now();
        int minutes = Minutes.minutesBetween(dateTime, now).getMinutes();
        String dateText;
        if(minutes == 0) dateText = ctx.getString(R.string.just_now);
        else if(minutes < 60) dateText = ctx.getString(R.string.m_ago, minutes);
        else{
            int hour = Hours.hoursBetween(dateTime, now).getHours();
            if(hour < 24) dateText = ctx.getString(R.string.h_ago, hour);
            else{
                int days = Days.daysBetween(dateTime, now).getDays();
                if(days < 31) dateText = ctx.getString(R.string.d_ago, days);
                else{
                    int weeks = Weeks.weeksBetween(dateTime, now).getWeeks();
                    dateText = ctx.getString(R.string.w_ago, weeks);
                }
            }
        }
        textViewDate.setText(dateText);

        switch (o.getNotification().getType()) {
            case 0:
                Shipp ship = notification.getShipp();
                String you = ctx.getString(R.string.you);
                imageViewType.setImageResource(R.drawable.ic_add_white);
                textView.setText(Html.fromHtml(String.format(ctx.getText(R.string.shipp_created_to_format).toString(), User.isMe(ship.getOwner()) ? you : ship.getOwner().getFirstname(),
                        User.isMe(ship.getUser1()) ? you : ship.getUser1().getFirstname(), User.isMe(ship.getUser2()) ? you : ship.getUser2().getFirstname())));
                break;
            case 1:
                textView.setText(notification.likeText(ctx, true));
                imageViewType.setImageResource(R.drawable.ic_thumb_up_white);
                break;
            case 2:
                textView.setText(notification.likeText(ctx, false));
                imageViewType.setImageResource(R.drawable.ic_thumb_down_white);
                break;
            case 3:
                textView.setText(notification.commentText(ctx));
                imageViewType.setImageResource(R.drawable.ic_comment_white);
                break;
            case 4:
            case 5:
                textView.setText(Html.fromHtml(String.format(ctx.getText(R.string.started_follow_to_format).toString(), o.getNotification().getRelated()[0].getUsername())));
                imageViewType.setImageResource(R.drawable.ic_follow);
                break;
            case 6:
                enableFollowView();
                break;
            case 7:
                textView.setText(Html.fromHtml(String.format(ctx.getText(R.string.accepted_follow_to_format).toString(), o.getNotification().getRelated()[0].getUsername())));
                imageViewType.setImageResource(R.drawable.ic_add_white);
        }

        if (!notification.isSeen()) {
            imageViewNew.setVisibility(View.VISIBLE);
            notification.setSeen();
            notification.save();
        }else imageViewNew.setVisibility(View.GONE);
    }

    private void enableFollowView() {
        textViewDate.setVisibility(View.GONE);
        final User u = notification.getNotification().getRelated()[0];
        imageViewType.setImageResource(R.drawable.ic_follow);
        textView.setText(Html.fromHtml(String.format(ctx.getText(R.string.user_pending).toString(), u.getUsername())));
        followRequest.setVisibility(View.VISIBLE);
        followRequest.setText(R.string.accept);
        followRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followRequest.setClickable(false);
                followRequest.setBackgroundResource(R.drawable.round_square_filled_colored);
                followRequest.setText(R.string.accepted);
                followRequest.setTextColor(Color.WHITE);
                User.acceptFollow(ctx, u.getId(), new BooleanListener() {
                    @Override
                    public void onResult(boolean data, String error) {
                        if (data) {
                            notification.delete();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        followRequest.setClickable(true);
                        followRequest.setBackgroundResource(R.drawable.round_square_red);
                        followRequest.setText(R.string.accept);
                        followRequest.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
                    }
                });
            }
        });
    }

    @Override
    public int layoutId() {
        return R.layout.holder_notification;
    }
}
