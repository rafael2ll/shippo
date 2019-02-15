package com.ft.shippo.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.text.emoji.widget.EmojiAppCompatTextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.UserActivity;
import com.ft.shippo.fragments.CommentFragment;
import com.ft.shippo.models.Action;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.Like;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Utils;
import com.google.gson.Gson;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;
import com.mindorks.butterknifelite.annotations.OnLongClick;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 13/03/18.
 */

public class TrendingShippHolder extends BaseHolder<Action> {
    @BindView(R.id.trending_shipp_tv_p1_country)
    private TextView mUser1Country;
    // Body views
    @BindView(R.id.trending_shipp_tv_p1_name)
    private EmojiAppCompatTextView mUser1name;
    @BindView(R.id.trending_shipp_tv_p1_age)
    private TextView mUser1age;
    @BindView(R.id.trending_shipp_civ_p1)
    private CircleImageView mUser1pic;
    @BindView(R.id.trending_shipp_ll_user1)
    private LinearLayout leftCard;

    @BindView(R.id.trending_shipp_tv_p2_name)
    private EmojiAppCompatTextView mUser2name;
    @BindView(R.id.trending_shipp_tv_p2_age)
    private TextView mUser2age;
    @BindView(R.id.trending_shipp_civ_p2)
    private CircleImageView mUser2pic;
    @BindView(R.id.trending_shipp_tv_p2_country)
    private TextView mUser2Country;

    @BindView(R.id.bubble)
    private ImageView bubble1;
    @BindView(R.id.bubble2)
    private ImageView bubble2;
    @BindView(R.id.shipp_bubble1_helper)
    ImageView bubbleHelper1;
    @BindView(R.id.shipp_bubble2_helper)
    ImageView bubbleHelper2;


    @BindView(R.id.trending_shipp_tv_summaries)
    private TextView mShippSummaries;
    @BindView(R.id.trending_shipp_iv_like)
    private ImageView mLike;
    @BindView(R.id.trending_shipp_iv_unlike)
    private ImageView mUnlike;
    @BindView(R.id.trending_shipp_ll_user2)
    private LinearLayout rightCard;


    @BindView(R.id.trending_trending_shipp_crown)
    ImageView imageViewCrown;
    @BindView(R.id.trending_shipp_civ_owner)
    CircleImageView mOwnerPic;
    @BindView(R.id.trending_trending_shipp_crown_left)
    View leftCrown;
    @BindView(R.id.trending_trending_shipp_crown_right)
    View rightCrown;
    @BindView(R.id.header)
    RelativeLayout crownView;
    @BindView(R.id.trending_shipp_tv_position)
    TextView textViewPosition;
    @BindView(R.id.linear_layout_owner)
    LinearLayout ownerView;
    @BindView(R.id.trending_shipp_tv_owner)
    TextView textViewOwner;
    private Shipp ship;
    private Context context;
    private Action action;

    private static final int bubbleThemes[] = new int[]{R.style.BlueBubble, R.style.RedBubble};//, R.style.GreenBubble, R.style.PurpleBubble,R.style.PinkBubble, R.style.CianBubble, R.style.BlueBubble,R.style.BrownBubble, R.style.BlackBubble, R.style.OrangeBubble};

    public TrendingShippHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }


    @Override
    public void feedWith(Context ctx, Action action, boolean isSelected) {
        this.context = ctx;
        this.ship = action.getShipp();
        this.action = action;
        loadImages();
        configPosition();
        configColours();
        loadLabels();
        loadSummaries();
    }

    private void configPosition() {
        int pos = getAdapterPosition();
        if (pos < 3) {
            int color;
            textViewPosition.setVisibility(View.GONE);
            crownView.setVisibility(View.VISIBLE);
            Drawable crown = ContextCompat.getDrawable(context, pos == 0 ? R.drawable.ic_crown_gold : pos == 1 ? R.drawable.ic_crown_silver : R.drawable.ic_crown_broze);
            color = ContextCompat.getColor(context, pos == 0 ? R.color.gold : pos == 1 ? R.color.silver : R.color.bronze);
            leftCrown.setBackgroundColor(color);
            rightCrown.setBackgroundColor(color);
            imageViewCrown.setImageDrawable(crown);

        } else {
            crownView.setVisibility(View.GONE);
            textViewPosition.setVisibility(View.VISIBLE);
            textViewPosition.setText(context.getString(R.string.position, pos + 1));

        }
    }

    private void configColours() {

        final ContextThemeWrapper wrapper1 = new ContextThemeWrapper(context, bubbleThemes[ship.getUser1().isMan() ? 0 : 1]);
        final Drawable bubble1Drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_bubble, wrapper1.getTheme());

        final ContextThemeWrapper wrapper2 = new ContextThemeWrapper(context, bubbleThemes[ship.getUser2().isMan() ? 0 : 1]);
        final Drawable bubble2Drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_bubble, wrapper2.getTheme());

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
        GlideRequestManager.get()
                .load(ship.getOwner().getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .override(100)
                .into(mOwnerPic);

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
        //Cleaning garbage
        mLike.setActivated(false);
        mUnlike.setActivated(false);
        // --- //

        mLike.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLike.setScaleX(1);
                mLike.setScaleY(1);
                mUnlike.setScaleX(1);
                mUnlike.setScaleY(1);
                if (ship.iLiked()) {

                    mLike.setActivated(true);
                    handleThumbState(true, true);
                }
                if (ship.iDisliked()) {
                    mUnlike.setActivated(true);
                    handleThumbState(false, true);
                }
                String summary = context.getString(R.string.shipp_summaries, ship.getLikeCount(), ship.getUnlikeCount(), ship.getCommentCount());
                mShippSummaries.setText(summary);
            }
        }, 10);

    }

    private void loadLabels() {

        String you = context.getString(R.string.you);
        int user1age = ship.getUser1().getAge();
        int user2age = ship.getUser2().getAge();

        mUser1name.setText(ship.getUser1().getUsername());
        mUser1age.setText(context.getString(R.string.years, user1age));
        mUser1Country.setText(ship.getUser1().getCityName());

        mUser2name.setText(ship.getUser2().getUsername());
        mUser2age.setText(context.getString(R.string.years, user2age));
        mUser2Country.setText(ship.getUser2().getCityName());
        textViewOwner.setText(ship.getOwner().getUsername());

        DateTime dateTime = DateTime.parse(action.getCreatedAt());
        dateTime.plus(TimeZone.getDefault().getOffset(DateTime.now().getMillis()));
        int hours = Hours.hoursBetween(dateTime, DateTime.now()).getHours();
        int days = Days.daysBetween(dateTime, DateTime.now()).getDays();

    }

    @OnClick(R.id.trending_shipp_iv_like)
    public void like() {
        final boolean action = !mLike.isActivated();
        mLike.setActivated(action);
        if (mUnlike.isActivated()) {
            mUnlike.setActivated(false);
            Like.dislikeShipp(context, true, ship.getId(), User.getCurrentUser().getId(), new BooleanListener() {
                @Override
                public void onResult(boolean data, String error) {
                    if (data) {
                        mUnlike.setActivated(false);
                    } else mUnlike.setActivated(true);
                    handleThumbState(false, false);
                }

                @Override
                public void onError(VolleyError error) {
                    mUnlike.setActivated(!action);
                }
            });
        }
        handleThumbState(false, true);
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
                mLike.setActivated(true);
            }
        });
    }

    @OnClick(R.id.trending_shipp_iv_unlike)
    public void unlike() {
        final boolean action = !mUnlike.isActivated();
        mUnlike.setActivated(action);
        if (mLike.isActivated()) {
            mLike.setActivated(false);
            Like.likeShipp(context, true, ship.getId(), User.getCurrentUser().getId(), new BooleanListener() {
                @Override
                public void onResult(boolean data, String error) {
                    if (data) {
                        mLike.setActivated(false);
                    } else mLike.setActivated(true);
                    handleThumbState(true, true);
                }

                @Override
                public void onError(VolleyError error) {
                    mLike.setActivated(true);
                }
            });
        }
        handleThumbState(false, true);
        handleThumbState(true, true);
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

    @OnClick(R.id.trending_shipp_iv_comment)
    public void comment() {
        Bundle b = new Bundle();
        b.putString(CommentFragment.SHIPP_KEY, new Gson().toJson(ship));
        CommentFragment dialog = new CommentFragment();
        dialog.setArguments(b);
        dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }

    @OnClick(R.id.linear_layout_owner)
    public void ownerViewClick() {
        userClick(ship.getOwner().getId());
    }


    @OnLongClick(R.id.trending_shipp_iv_share)
    public void share() {
        Snackbar.make(itemView, R.string.sharing_shipp, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
        Shipp.share(context, ship.getId(), new BooleanListener() {
            @Override
            public void onResult(boolean data, String error) {
                if (data)
                    Snackbar.make(itemView, R.string.shipp_shared, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
            }

            @Override
            public void onError(VolleyError error) {
                Snackbar.make(itemView, R.string.error_try_again, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
            }
        });
    }

    @OnClick(R.id.trending_shipp_iv_share)
    public void shareClick() {
        Snackbar.make(itemView, R.string.long_press_to_share, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
    }

    @OnClick(R.id.trending_shipp_ll_user1)
    public void user1Click() {
        userClick(ship.getUser1().getId());
    }

    @OnClick(R.id.trending_shipp_ll_user2)
    public void user2Click() {
        userClick(ship.getUser2().getId());
    }


    public void userClick(String id) {
        ContextCompat.startActivity(context,
                new Intent(context, UserActivity.class).putExtra(UserActivity.USER_KEY, id), null);

    }

    @Override
    public int layoutId() {
        return R.layout.trending_shipp_holder;
    }
}
