package com.ft.shippo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.adapters.EasyRecyclerAdapter;
import com.ft.shippo.adapters.InfiniteScrollListener;
import com.ft.shippo.holders.CommentHolder;
import com.ft.shippo.models.Comment;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.Shipp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 29/01/18.
 */

public class CommentFragment extends DialogFragment {

    public static final String SHIPP_KEY = "SHIPP_KEY";
    @BindView(R.id.fragment_comment_tv_respond_name)
    TextView textViewRespondName;
    @BindView(R.id.fragment_comment_respond)
    View respondView;
    @BindView(R.id.recycler_view)
    private RecyclerView mRecycler;
    @BindView(R.id.fragement_comment_tiet)
    private TextInputEditText mCommentEditText;
    @BindView(R.id.fragement_comment_iv_send)
    private ImageView mSend;
    @BindView(R.id.loading)
    private ProgressBar mSending;
    private EasyRecyclerAdapter<Comment, CommentHolder> mAdapter;
    private LinearLayoutManager layoutManager;
    private InfiniteScrollListener infiniteScrollListener;
    private int page = 0;
    private Shipp shipp;
    private List<Comment> comments = new ArrayList<>();
    private CommentHolder.OnRespondClickListener respondClickListener;
    private Comment replyingTo = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    private synchronized void loadComments(int i) {
        infiniteScrollListener.showLoadMore(true);
        Comment.loadComment(getActivity(), shipp.getId(), i, new ListObjectListener<Comment>(new TypeToken<List<Comment>>() {
        }.getType()) {
            @Override
            public void onResult(List<Comment> data, String error) {
                if (data.size() < 20) infiniteScrollListener.shouldLoadMore(false);
                infiniteScrollListener.showLoadMore(false);
                page++;
                comments.addAll(data);
                mAdapter.notifyItemRangeInserted(comments.size() - data.size(), data.size());
            }

            @Override
            public void onError(VolleyError error) {
                infiniteScrollListener.showLoadMore(false);
                Snackbar.make(mCommentEditText, R.string.error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnifeLite.bind(this, v);
        try {
            shipp = new Gson().fromJson(getArguments().getString(CommentFragment.SHIPP_KEY), Shipp.class);
        } catch (NullPointerException e) {
            dismiss();
        }
        mSending.setVisibility(View.GONE);
        layoutManager = new LinearLayoutManager(getActivity());
        respondClickListener = new CommentHolder.OnRespondClickListener() {
            @Override
            public void onClick(Comment comment) {
                replyingTo = comment;
                respondView.setVisibility(View.VISIBLE);
                textViewRespondName.setText(Html.fromHtml(String.format(getActivity().getText(R.string.reply_to).toString(), comment.getUser().getUsername())));
            }
        };
        infiniteScrollListener = new InfiniteScrollListener(20, layoutManager) {
            @Override
            public void onScrolledToEnd(int firstVisibleItemPosition) {
                loadComments(page + 1);
            }
        };

        mAdapter = new EasyRecyclerAdapter<Comment, CommentHolder>(comments, Comment.class, R.layout.main_comment_holder, CommentHolder.class) {

            @Override
            protected void populateViewHolder(CommentHolder viewHolder, Comment model, int position) {
                viewHolder.feedIt(getActivity(), model, respondClickListener);
            }
        };
        loadComments(page);

        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.addOnScrollListener(infiniteScrollListener);
        return v;
    }

    @OnClick(R.id.fragment_comment_respond_iv_cancel)
    public void cancelReply() {
        respondView.setVisibility(View.GONE);
        replyingTo = null;
    }

    @OnClick(R.id.fragement_comment_iv_send)
    public void send() {
        String text = mCommentEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(text) && shipp != null) {
            mSending.setVisibility(View.VISIBLE);
            mCommentEditText.setText("");
            mSend.setEnabled(false);
            Comment.comment(getActivity(), shipp.getId(), text, replyingTo != null, replyingTo == null ? null : replyingTo.getId(), new ObjectListener<Comment>(Comment.class) {
                @Override
                public void onResult(Comment data, String error) {
                    mSending.setVisibility(View.GONE);
                    mSend.setEnabled(true);
                    if (respondView.getVisibility() == View.VISIBLE) {
                        respondView.setVisibility(View.GONE);
                        replyingTo = null;
                    }
                    mAdapter.cleanup();
                    mAdapter.notifyDataSetChanged();
                    mCommentEditText.clearComposingText();
                    loadComments(0);
                }

                @Override
                public void onError(VolleyError error) {
                    mSending.setVisibility(View.GONE);
                    mSend.setEnabled(true);
                    Snackbar.make(mSend, R.string.error_try_again, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }
}
