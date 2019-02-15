package com.ft.shippo.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.facebook.AccessToken;
import com.ft.shippo.R;
import com.ft.shippo.UserActivity;
import com.ft.shippo.fragments.CommentFragment;
import com.ft.shippo.models.Action;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.Like;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Utils;
import com.google.gson.Gson;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 20/02/18.
 */

public class LikedFeedHolder extends BaseHolder<Action> {
    @BindView(R.id.holder_feed_tv_p1_country)
    TextView mUser1Country;
    // Like Header views
    @BindView(R.id.holder_feed_civ_orig)
    private CircleImageView mOwnerPic;
    @BindView(R.id.holder_feed_tv_orig_name)
    private TextView mOwnerName;
    @BindView(R.id.holder_feed_tv_orig_date)
    private TextView mPostDate;
    //Sub like header views
    @BindView(R.id.holder_feed_civ_sub)
    private CircleImageView subCiv;
    @BindView(R.id.holder_feed_tv_sub_name)
    private TextView subTextView;
    // Body views
    @BindView(R.id.holder_feed_tv_p1_name)
    private TextView mUser1name;
    @BindView(R.id.holder_feed_tv_p1_age)
    private TextView mUser1age;
    @BindView(R.id.holder_feed_civ_p1)
    private CircleImageView mUser1pic;
    @BindView(R.id.right_card_shipp)
    private CardView rightCard;

    @BindView(R.id.holder_feed_tv_p2_name)
    private TextView mUser2name;
    @BindView(R.id.holder_feed_tv_p2_age)
    private TextView mUser2age;
    @BindView(R.id.holder_feed_civ_p2)
    private CircleImageView mUser2pic;
    @BindView(R.id.holder_feed_tv_p2_country)
    private TextView mUser2Country;
    @BindView(R.id.left_card_shipp)
    private CardView leftCard;

    @BindView(R.id.bubble)
    private ImageView bubble1;
    @BindView(R.id.bubble2)
    private ImageView bubble2;
    @BindView(R.id.shipp_bubble1_helper)
    private ImageView bubbleHelper1;
    @BindView(R.id.shipp_bubble2_helper)
    private ImageView bubbleHelper2;



    @BindView(R.id.holder_feed_tv_text)
    private TextView mText;
    @BindView(R.id.holder_feed_tv_summaries)
    private TextView mShippSummaries;
    @BindView(R.id.holder_feed_iv_like)
    private ImageView mLike;
    @BindView(R.id.holder_feed_iv_unlike)
    private ImageView mUnlike;

    private Shipp ship;
    private Context context;
    private boolean isLike = false;
    private Action action;
    private static final int bubbleThemes[] = new int[]{R.style.BlueBubble, R.style.RedBubble, R.style.GreenBubble, R.style.PurpleBubble,R.style.PinkBubble, R.style.CianBubble, R.style.BlueBubble};

    public LikedFeedHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    @Override
    public void feedWith(Context ctx, Action action, boolean isLike) {
        this.context = ctx;
        this.action = action;
        this.ship = action.getShipp();
        this.isLike = isLike;
        loadImages();
        configColours();
        loadLabels();
        loadSummaries();
        loadItemsColors();
    }

    private void loadItemsColors() {
        mLike.setActivated(false);
        mUnlike.setActivated(false);
    }

    private void configColours() {
        final ContextThemeWrapper wrapper1 = new ContextThemeWrapper(context, bubbleThemes[ship.getUser1().isMan() ? 0 : 1]);
        final Drawable bubble1Drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_bubble, wrapper1.getTheme());
        // final Drawable backgroundDrawable1 = VectorDrawableCompat.create(context.getResources(), R.drawable.shipp_round_left, wrapper1.getTheme());

        final ContextThemeWrapper wrapper2 = new ContextThemeWrapper(context, bubbleThemes[ship.getUser2().isMan() ? 0 : 1]);
        final Drawable bubble2Drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_bubble, wrapper2.getTheme());
        //VectorDrawableCompat.create(context.getResources(), R.drawable.shipp_round_right, wrapper2.getTheme());

        bubble1.setImageDrawable(bubble1Drawable);
        bubble2.setImageDrawable(bubble2Drawable);

        int color1 = Utils.getAttributeColor(context, wrapper1.getTheme(), R.attr.bubble_color);
        int color2 = Utils.getAttributeColor(context, wrapper2.getTheme(), R.attr.bubble_color);

        Drawable backgroundDrawable2 = Utils.rightShippDrawable(color2);
        Drawable backgroundDrawable1 = Utils.leftShippDrawable(color1);
        leftCard.setBackground(backgroundDrawable1);
        bubbleHelper1.setImageDrawable(new ColorDrawable(color1));
        bubbleHelper2.setImageDrawable(new ColorDrawable(color2));
        rightCard.setBackground(backgroundDrawable2);
    }

    private void loadImages() {
        User user = action.getUser();
        if (user != null) {
            GlideRequestManager.get()
                    .load(user.getPhotoUri())
                    .centerCrop()
                    .placeholder(R.drawable.ic_boy_15)
                    .into(mOwnerPic);
        }
        GlideRequestManager.get()
                .load(ship.getOwner().getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .into(subCiv);

        GlideRequestManager.get()
                .load(ship.getUser1().getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .into(mUser1pic);

        GlideRequestManager.get()
                .load(ship.getUser2().getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .into(mUser2pic);
    }

    private void loadSummaries() {
        mLike.setScaleX(1);
        mLike.setScaleY(1);
        mUnlike.setScaleX(1);
        mUnlike.setScaleY(1);
        if (ship.iLiked()) {
            mLike.setSelected(true);
            handleThumbState(true, true);
        }
        if (ship.iDisliked()) {
            mUnlike.setSelected(true);
            handleThumbState(false, true);
        }
        String summary = context.getString(R.string.shipp_summaries, ship.getLikeCount(), ship.getUnlikeCount(), ship.getCommentCount());
        mShippSummaries.setText(summary);
    }

    private void loadLabels() {

        String you = context.getString(R.string.you);
        int user1age = ship.getUser1().getAge();
        int user2age = ship.getUser2().getAge();


        mText.setVisibility(ship.getLabel() != null ? View.VISIBLE : View.GONE);
        if (ship.getLabel() != null) mText.setText(ship.getLabel());
        mUser1name.setText(ship.getUser1().getUsername());
        mUser1age.setText(context.getString(R.string.years, user1age));
        mUser1Country.setText(ship.getUser1().getCityName());

        mUser2name.setText(ship.getUser2().getUsername());
        mUser2age.setText(context.getString(R.string.years, user2age));
        mUser2Country.setText(ship.getUser2().getCityName());
        mOwnerName.setText(Html.fromHtml(String.format(context.getText(isLike ? R.string.user_liked_to_format : R.string.user_disliked_to_format).toString(), User.isMe(action.getUser())?  you: action.getUser().getFirstname(), User.isMe(ship.getUser1())? you : ship.getUser1().getFirstname(), User.isMe(ship.getUser2())? you : ship.getUser2().getFirstname())));
        subTextView.setText(Html.fromHtml(String.format(context.getText(R.string.shipp_created_to_format).toString(), User.isMe(ship.getOwner())? you : ship.getOwner().getFirstname(),
                User.isMe(ship.getUser1())? you : ship.getUser1().getFirstname(), User.isMe(ship.getUser2())? you : ship.getUser2().getFirstname())));

        DateTime dateTime = DateTime.parse(ship.getCreatedAt());
        dateTime.plus(TimeZone.getDefault().getOffset(DateTime.now().getMillis()));
        int hours = Hours.hoursBetween(dateTime, DateTime.now()).getHours();
        int days = Days.daysBetween(dateTime, DateTime.now()).getDays();

        if (hours < 1) {
            int minutes = Minutes.minutesBetween(dateTime, DateTime.now()).getMinutes();
            if (minutes > 5) mPostDate.setText(context.getString(R.string.minutes_ago, minutes));
            else mPostDate.setText(context.getString(R.string.just_now));
        } else if (hours < 24) mPostDate.setText(context.getString(R.string.hours_ago, hours));
        else mPostDate.setText(context.getString(R.string.days_ago, days));
    }

    @OnClick(R.id.holder_feed_iv_like)
    public void like() {
        final boolean action = !mLike.isActivated();
        mLike.setActivated(action);
        handleThumbState(true, true);
        Like.likeShipp(context, !action, ship.getId(), User.getCurrentUser().getId(), new BooleanListener() {
            @Override
            public void onResult(boolean data, String error) {
                if (data) {
                    mLike.setActivated(action);
                } else mLike.setActivated(!action);
                handleThumbState(true, false);
            }

            @Override
            public void onError(VolleyError error) {
                mLike.setActivated(!action);
            }
        });
    }

    @OnClick(R.id.holder_feed_iv_unlike)
    public void unlike() {
        final boolean action = !mUnlike.isActivated();
        mUnlike.setActivated(action);
        handleThumbState(false, true);
        Like.dislikeShipp(context, !action, ship.getId(), User.getCurrentUser().getId(), new BooleanListener() {
            @Override
            public void onResult(boolean data, String error) {
                if (data) {
                    mUnlike.setActivated(action);
                } else mUnlike.setActivated(!action);
                handleThumbState(false, false);
            }

            @Override
            public void onError(VolleyError error) {
                mUnlike.setActivated(!action);
            }
        });
    }

    private void handleThumbState(boolean isLikeImageView, boolean shoulAnimate) {
        if (isLikeImageView) {
            mLike.setImageResource(mLike.isActivated() ? R.drawable.ic_thumb_up_active : R.drawable.ic_thumb_up);
            if (shoulAnimate) {
                if (mLike.isActivated()) mLike.animate()
                        .setDuration(500)
                        .scaleY(1.1f)
                        .scaleX(1.1f)
                        .start();
                else mLike.animate()
                        .setDuration(500)
                        .scaleY(1f)
                        .scaleX(1f)
                        .start();
            }
        } else {
            mUnlike.setImageResource(mUnlike.isActivated() ? R.drawable.ic_thumb_down_active : R.drawable.ic_thumb_down);
            if (shoulAnimate) {
                if (mUnlike.isActivated())
                    mUnlike.animate()
                            .setDuration(500)
                            .scaleY(1.1f)
                            .scaleX(1.1f)
                            .start();
                else
                    mUnlike.animate()
                            .setDuration(500)
                            .scaleY(1f)
                            .scaleX(1f)
                            .start();
            }
        }
    }

    @OnClick(R.id.holder_feed_iv_comment)
    public void comment() {
        Bundle b = new Bundle();
        b.putString(CommentFragment.SHIPP_KEY, new Gson().toJson(ship));
        CommentFragment dialog = new CommentFragment();
        dialog.setArguments(b);
        dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }

    @OnClick(R.id.holder_feed_iv_share)
    public void share() {

    }

    @OnClick(R.id.holder_feed_im_more)
    public void more() {

    }

    @OnClick(R.id.holder_feed_ll_user1)
    public void user1Click() {
        userClick(ship.getUser1().getId());
    }

    @OnClick(R.id.holder_feed_ll_user2)
    public void user2Click() {
        userClick(ship.getUser2().getId());
    }

    @OnClick(R.id.holder_feed_civ_sub)
    public void ownerPicClick() {
        ownerClick();
    }

    @OnClick(R.id.holder_feed_ll_owner)
    public void ownerClick() {
        userClick(ship.getOwner().getId());
    }

    public void userClick(String id) {
        ContextCompat.startActivity(context,
                new Intent(context, UserActivity.class).putExtra(UserActivity.USER_KEY, id), null);

    }

    @Override
    public int layoutId() {
        return R.layout.holder_feed_liked;
    }
}
