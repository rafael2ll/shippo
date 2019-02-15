package com.ft.shippo.holders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.adapters.EasyRecyclerAdapter;
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
 * Created by rafael on 29/01/18.
 */

public class CommentHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.main_comment_holder_recycler)
    RecyclerView recyclerSubComments;
    @BindView(R.id.comment_holder_civ)
    private CircleImageView mProfilePic;
    @BindView(R.id.comment_holder_tv_name)
    private TextView mUsername;
    @BindView(R.id.comment_holder_tv_comment)
    private TextView mComment;
    @BindView(R.id.comment_holder_tv_respond)
    private TextView mRespond;
    @BindView(R.id.comment_holder_tv_like_count)
    private TextView mLikeCount;
    @BindView(R.id.comment_holder_tv_unlike_count)
    private TextView mUnlikeCount;
    @BindView(R.id.comment_holder_iv_like)
    private ImageView mLike;
    @BindView(R.id.comment_holder_iv_unlike)
    private ImageView mUnlike;
    private Comment comment;
    private EasyRecyclerAdapter<Comment, SubCommentHolder> subCommentAdapter;
    private Context ctx;
    private OnRespondClickListener onRespondClickListener;
    @BindView(R.id.comment_holder_tv_date)
    private TextView mCommentDate;

    public CommentHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    public void feedIt(Context ctx, Comment comment, OnRespondClickListener listener) {
        this.ctx = ctx;
        this.comment = comment;
        this.onRespondClickListener = listener;
        fillComment();
        //if (comment.getSubcomments().size() > 0) fillSubcomments();else
        // recyclerSubComments.setVisibility(View.GONE);
    }

    private void fillSubcomments() {
        recyclerSubComments.setLayoutManager(new LinearLayoutManager(ctx));
        recyclerSubComments.setVisibility(View.VISIBLE);
        subCommentAdapter = new EasyRecyclerAdapter<Comment, SubCommentHolder>(comment.getSubcomments(), Comment.class, R.layout.sub_comment_holder, SubCommentHolder.class) {
            @Override
            protected void populateViewHolder(SubCommentHolder viewHolder, Comment model, int position) {
                viewHolder.feedIt(ctx, model);
            }
        };

        recyclerSubComments.setAdapter(subCommentAdapter);
        recyclerSubComments.setNestedScrollingEnabled(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, comment.getSubcomments().size() * 120);
        recyclerSubComments.setLayoutParams(params);
    }

    private void fillComment() {
        mLike.setActivated(comment.iLiked());
        mUnlike.setActivated(comment.iDisliked());
        mComment.setText(comment.getText());
        mUsername.setText(String.format("%s", comment.getUser().getUsername()));
        mCommentDate.setText(comment.getPostedFromNow(ctx));
        mLikeCount.setText(String.format("%s", comment.getLikeCountFormatted()));
        mUnlikeCount.setText(String.format("%s", comment.getUnlikeCountFormatted()));
        GlideRequestManager.get()
                .load(comment.getUser().getPhotoUri())
                .into(mProfilePic);
    }

    @OnClick(R.id.comment_holder_iv_like)
    public void like() {
        final boolean action = !mLike.isActivated();
        mLike.setActivated(action);
        if (mUnlike.isActivated()) {
            mUnlike.setActivated(false);
            handleThumbState(false, true);

        }
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

    @OnClick(R.id.comment_holder_iv_unlike)
    public void unlike() {
        final boolean action = !mUnlike.isActivated();
        mUnlike.setActivated(action);
        if (mLike.isActivated()) {
            mLike.setActivated(false);
            handleThumbState(true, true);
        }
        handleThumbState(false, true);
        Like.dislikeComment(ctx, !action, comment.getId(), User.getCurrentUser().getId(), new BooleanListener() {
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
                handleThumbState(false, false);
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

    @OnClick(R.id.comment_holder_tv_respond)
    public void onRespondClick() {
        this.onRespondClickListener.onClick(comment);
    }

    public interface OnRespondClickListener {
        public void onClick(Comment comment);
    }
}
