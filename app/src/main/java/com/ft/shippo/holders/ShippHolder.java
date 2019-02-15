package com.ft.shippo.holders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.text.emoji.widget.EmojiAppCompatTextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import org.joda.time.Minutes;

import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 21/01/18.
 */

public class ShippHolder extends BaseHolder<Action> {
    @BindView(R.id.shipp_holder_tv_p1_country)
    private TextView mUser1Country;
    // Header views
    @BindView(R.id.shipp_holder_civ_orig)
    private CircleImageView mOwnerPic;
    @BindView(R.id.shipp_holder_tv_orig_name)
    private EmojiAppCompatTextView mOwnerName;
    @BindView(R.id.shipp_holder_tv_orig_date)
    private TextView mPostDate;
    // Body views
    @BindView(R.id.shipp_holder_tv_p1_name)
    private EmojiAppCompatTextView mUser1name;
    @BindView(R.id.shipp_holder_tv_p1_age)
    private TextView mUser1age;
    @BindView(R.id.shipp_holder_civ_p1)
    private CircleImageView mUser1pic;
    @BindView(R.id.shipp_holder_ll_user1)
    private LinearLayout leftCard;

    @BindView(R.id.shipp_holder_tv_p2_name)
    private EmojiAppCompatTextView mUser2name;
    @BindView(R.id.shipp_holder_tv_p2_age)
    private TextView mUser2age;
    @BindView(R.id.shipp_holder_civ_p2)
    private CircleImageView mUser2pic;
    @BindView(R.id.shipp_holder_tv_p2_country)
    private TextView mUser2Country;

    @BindView(R.id.bubble)
    private ImageView bubble1;
    @BindView(R.id.bubble2)
    private ImageView bubble2;
    @BindView(R.id.shipp_bubble1_helper)
    ImageView bubbleHelper1;
    @BindView(R.id.shipp_bubble2_helper)
    ImageView bubbleHelper2;

    @BindView(R.id.shipp_holder_tv_text)
    private EmojiAppCompatTextView mText;
    @BindView(R.id.shipp_holder_tv_summaries)
    private TextView mShippSummaries;
    @BindView(R.id.shipp_holder_iv_like)
    private ImageView mLike;
    @BindView(R.id.shipp_holder_iv_unlike)
    private ImageView mUnlike;
    @BindView(R.id.shipp_holder_ll_user2)
    private LinearLayout rightCard;

    @BindView(R.id.shipp_holder_im_more)
    ImageView more;

    @BindView(R.id.linear_layout_owner)
    LinearLayout ownerView;
    @BindView(R.id.shipp_holder_tv_owner)
    TextView textViewOwner;
    private Shipp ship;
    private Context context;
    private Action action;

    private static final int bubbleThemes[] = new int[]{R.style.BlueBubble, R.style.RedBubble};//, R.style.GreenBubble, R.style.PurpleBubble,R.style.PinkBubble, R.style.CianBubble, R.style.BlueBubble,R.style.BrownBubble, R.style.BlackBubble, R.style.OrangeBubble};

    public ShippHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }


    @Override
    public void feedWith(Context ctx, Action action, boolean isSelected) {
        this.context = ctx;
        this.ship = action.getShipp();
        this.action = action;
        loadImages();

        configColours();
        loadLabels();
        loadSummaries();
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
        GlideRequestManager.get()
                .load(action.getType() == Action.Type.SHIPP ? ship.getOwner().getPhotoUri() : action.getUser().getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
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
                    Log.d("shipp", new Gson().toJson(ship));
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

        mText.setVisibility(ship.getLabel() != null ? View.VISIBLE : View.GONE);
        if (ship.getLabel() != null) mText.setText(ship.getLabel());
        mUser1name.setText(ship.getUser1().getUsername());
        mUser1age.setText(context.getString(R.string.years, user1age));
        mUser1Country.setText(ship.getUser1().getCityName());

        mUser2name.setText(ship.getUser2().getUsername());
        mUser2age.setText(context.getString(R.string.years, user2age));
        mUser2Country.setText(ship.getUser2().getCityName());
        if(action.getType().equals(Action.Type.SHIPP)) ownerView.setVisibility(View.GONE);
        else {
            ownerView.setVisibility(View.VISIBLE);
            User u = action.getUser();
            textViewOwner.setText(ship.getOwner().getUsername());
        }
        switch (action.getType()) {
            case SHIPP:
                mOwnerName.setText(Html.fromHtml(String.format(context.getText(R.string.shipp_created_to_format).toString(), User.isMe(ship.getOwner()) ? you : ship.getOwner().getFirstname(),
                    User.isMe(ship.getUser1()) ? you : ship.getUser1().getFirstname(), User.isMe(ship.getUser2()) ? you : ship.getUser2().getFirstname())));
            break;
            case LIKE:
                mOwnerName.setText(Html.fromHtml(String.format(context.getText(R.string.user_liked_to_format).toString(), User.isMe(action.getUser())?  you: action.getUser().getFirstname(), User.isMe(ship.getUser1())? you : ship.getUser1().getFirstname(), User.isMe(ship.getUser2())? you : ship.getUser2().getFirstname())));
            break;
            case DISLIKE:
                mOwnerName.setText(Html.fromHtml(String.format(context.getText(R.string.user_disliked_to_format).toString(), User.isMe(action.getUser())?  you: action.getUser().getFirstname(), User.isMe(ship.getUser1())? you : ship.getUser1().getFirstname(), User.isMe(ship.getUser2())? you : ship.getUser2().getFirstname())));
                break;
            case SHARE:
                mOwnerName.setText(Html.fromHtml(String.format(context.getText(R.string.shared_to_format).toString(),User.isMe(action.getUser())?  you: action.getUser().getFirstname(), User.isMe(ship.getUser1())? you : ship.getUser1().getFirstname(), User.isMe(ship.getUser2())? you : ship.getUser2().getFirstname())));
        }
        DateTime dateTime = DateTime.parse(action.getCreatedAt());
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

    @OnClick(R.id.shipp_holder_iv_like)
    public void like() {
        final boolean action = !mLike.isActivated();
        mLike.setActivated(action);
        if (mUnlike.isActivated()) {
            mUnlike.setActivated(false);
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

    @OnClick(R.id.shipp_holder_iv_unlike)
    public void unlike() {
        final boolean action = !mUnlike.isActivated();
        mUnlike.setActivated(action);
        if (mLike.isActivated()) {
            mLike.setActivated(false);
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

    @OnClick(R.id.shipp_holder_iv_comment)
    public void comment() {
        Bundle b = new Bundle();
        b.putString(CommentFragment.SHIPP_KEY, new Gson().toJson(ship));
        CommentFragment dialog = new CommentFragment();
        dialog.setArguments(b);
        dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "");
    }

    @OnClick(R.id.linear_layout_owner)
    public void ownerViewClick(){
        userClick(ship.getOwner().getId());
    }
    @OnClick(R.id.shipp_holder_im_more)
    public void more() {
        PopupMenu menuCompat = new PopupMenu(context, more);
        if (User.isMe(ship.getOwner())) menuCompat.inflate(R.menu.report_or_delete);
        else menuCompat.inflate(R.menu.report);
        menuCompat.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.report) {
                    User.reportShipp(context, ship.getId());
                    Toast.makeText(context, "Shipp reported", Toast.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(itemView, R.string.confirm_delete_shipp, Snackbar.LENGTH_LONG).setAction(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Shipp.delete(context, ship.getId());
                        }
                    }).show();
                }
                return true;
            }
        });
        menuCompat.show();
    }

    @OnLongClick(R.id.shipp_holder_iv_share)
    public void share() {
        Snackbar.make(itemView, R.string.sharing_shipp, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
        Shipp.share(context, ship.getId(), new BooleanListener(){
            @Override
            public void onResult(boolean data, String error) {
                if(data)
                    Snackbar.make(itemView, R.string.shipp_shared, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
            }

            @Override
            public void onError(VolleyError error) {
                Snackbar.make(itemView, R.string.error_try_again, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
            }
        });
    }

    @OnClick(R.id.shipp_holder_iv_share)
    public void shareClick() {
        Snackbar.make(itemView, R.string.long_press_to_share, Snackbar.LENGTH_SHORT).setAction(R.string.ok, null).show();
    }

    @OnClick(R.id.shipp_holder_ll_user1)
    public void user1Click() {
        userClick(ship.getUser1().getId());
    }

    @OnClick(R.id.shipp_holder_ll_user2)
    public void user2Click() {
        userClick(ship.getUser2().getId());
    }

    @OnClick(R.id.shipp_holder_civ_orig)
    public void ownerPicClick() {
        ownerClick();
    }

    @OnClick(R.id.shipp_holder_ll_owner)
    public void ownerClick() {
        if(action.getType().equals(Action.Type.SHIPP)) userClick(ship.getOwner().getId());
        else userClick(action.getUser().getId());
    }

    public void userClick(String id) {
        ContextCompat.startActivity(context,
                new Intent(context, UserActivity.class).putExtra(UserActivity.USER_KEY, id), null);

    }

    @Override
    public int layoutId() {
        return R.layout.shipp_holder;
    }
}
