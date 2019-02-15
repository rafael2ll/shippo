package com.ft.shippo.holders;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 26/01/18.
 */

public class UserHolder extends BaseHolder<User> {
    @BindView(R.id.user_holder_civ)
    private CircleImageView mUserPic;
    @BindView(R.id.user_holder_follow)
    private TextView mFollow;
    @BindView(R.id.user_holder_location)
    private TextView mLocation;
    @BindView(R.id.user_holder_username)
    private TextView mUsername;

    private Context ctx;
    private User user;

    public UserHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    public void feedWith(Context ctx, User u, boolean ignored) {
        feedIt(ctx, u);
    }

    public void feedIt(Context ctx, User u) {
        this.user = u;
        this.ctx = ctx;
        GlideRequestManager.get()
                .load(user.getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .into(mUserPic);
        mUserPic.setBorderColor(ContextCompat.getColor(ctx, user.gender() ? R.color.blue : R.color.colorPrimary));
        mUsername.setText(user.getUsername());
        mLocation.setText(user.getCityName());

        loadIsFollowing();
    }

    private void loadIsFollowing() {

        if(user.isPending()){
            mFollow.setText(R.string.pending);
            mFollow.setBackgroundResource(R.drawable.rounder_border_colored);
            mFollow.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
            mFollow.setEnabled(false);
        }
        else if (user.isFollowingMe()) {
            mFollow.setText(R.string.following);
            mFollow.setBackgroundResource(R.drawable.round_square_filled_colored);
            mFollow.setTextColor(Color.WHITE);
            mFollow.setEnabled(true);
        } else {
            mFollow.setText(R.string.follow);
            mFollow.setBackgroundResource(R.drawable.rounder_border_colored);
            mFollow.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
            mFollow.setEnabled(true);
        }

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.isPrivate() && !user.isFollowingMe()){
                    user.setIsPending(true);
                    loadIsFollowing();
                    User.requestFollow(ctx, user.getId(),new BooleanListener(){
                        @Override
                        public void onResult(boolean data, String error){
                            if(!data) user.setIsPending(false);
                            loadIsFollowing();
                        }
                        @Override
                        public void onError(VolleyError error){
                            user.setIsPending(false);
                            loadIsFollowing();
                        }
                    });
                    return;
                }
                user.setIsFollowingMe(!user.isFollowingMe());
                loadIsFollowing();
                if (!user.isFollowingMe())
                    User.getCurrentUser().unfollow(ctx, user.getId(), new BooleanListener() {
                        @Override
                        public void onResult(boolean data, String error) {
                            if (!data) user.setIsFollowingMe(true);
                            loadIsFollowing();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            user.setIsFollowingMe(true);
                            loadIsFollowing();
                        }
                    });
                else User.getCurrentUser().follow(ctx, user.getId(), new BooleanListener() {
                    @Override
                    public void onResult(boolean data, String error) {
                        if (!data) user.setIsFollowingMe(false);
                        loadIsFollowing();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        user.setIsFollowingMe(false);
                        loadIsFollowing();
                    }
                });
            }
        });
    }

    @Override
    public int layoutId() {
        return R.layout.user_item_holder;
    }

    public void setOnClick(final OnItemClickListener listener) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(user);
            }
        });
    }

    public interface OnItemClickListener {
        public void onClick(User user);
    }
}
