package com.ft.shippo.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.Comment;
import com.ft.shippo.models.Like;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 06/02/18.
 */

public class SubCommentHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.sub_comment_holder_civ)
    private CircleImageView mProfilePic;
    @BindView(R.id.sub_comment_holder_tv_name)
    private TextView mUsername;
    @BindView(R.id.sub_comment_holder_tv_comment)
    private TextView mComment;

    @BindView(R.id.sub_comment_holder_tv_like_count)
    private TextView mLikeCount;
    @BindView(R.id.sub_comment_holder_tv_unlike_count)
    private TextView mUnlikeCount;
    @BindView(R.id.sub_comment_holder_iv_like)
    private ImageView mLike;
    @BindView(R.id.sub_comment_holder_iv_unlike)
    private ImageView mUnlike;

    private Comment comment;
    private Context ctx;

    public SubCommentHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    public void feedIt(Context ctx, Comment comment) {
        this.ctx = ctx;
        this.comment = comment;
        fillComment();
    }

    private void fillComment() {
        mComment.setText(comment.getText());
        mUsername.setText(String.format("%s • %s", comment.getUser().getUsername(), comment.getPostedFromNow(ctx)));
        mLikeCount.setText(String.format("%.2s", comment.getLikeCountFormatted()));
        mUnlikeCount.setText(String.format("%.2s", comment.getUnlikeCountFormatted()));
        GlideRequestManager.get()
                .load(comment.getUser().getPhotoUri())
                .into(mProfilePic);
    }

    @OnClick(R.id.sub_comment_holder_iv_like)
    public void like() {
        final boolean action = !mLike.isActivated();
        mLike.setActivated(action);
        Like.likeComment(ctx, !action, comment.getId(), User.getCurrentUser().getId(), new BooleanListener() {
            @Override
            public void onResult(boolean data, String error) {
                if (data) {
                    mLike.setActivated(action);
                } else mLike.setActivated(!action);
            }

            @Override
            public void onError(VolleyError error) {
                mLike.setActivated(!action);
            }
        });
    }

    @OnClick(R.id.sub_comment_holder_iv_unlike)
    public void unlike() {
        final boolean action = !mUnlike.isActivated();
        mUnlike.setActivated(action);
        Like.dislikeComment(ctx, !action, comment.getId(), User.getCurrentUser().getId(), new BooleanListener() {
            @Override
            public void onResult(boolean data, String error) {
                if (data) {
                    mUnlike.setActivated(action);
                } else mUnlike.setActivated(!action);
            }

            @Override
            public void onError(VolleyError error) {
                mUnlike.setActivated(!action);
            }
        });
    }

}
