package com.ft.shippo.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.adapters.UserSearchAdapter;
import com.ft.shippo.holders.UserHolder;
import com.ft.shippo.models.User;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

/**
 * Created by rafael on 26/01/18.
 */

public class SearchUserDialog extends DialogFragment {
    @BindView(R.id.progress)
    ProgressBar mLoading;
    @BindView(R.id.shipp_user_search_tiet_search)
    private TextInputEditText mText;
    @BindView(R.id.shipp_user_search_iv)
    private ImageView mSearch;
    @BindView(R.id.recycler_view)
    private RecyclerView mRecycler;
    @BindView(R.id.shipp_user_search_container)
    private LinearLayout mContainer;
    private OnUserSelectedListener onUserSelectedListener;
    private String search = "";
    private LinearLayoutManager linearLayoutManager;
    private UserSearchAdapter mAdapter;
    private UserHolder.OnItemClickListener userClick;

    public SearchUserDialog() {
    }


    public SearchUserDialog addListener(OnUserSelectedListener listener) {
        this.onUserSelectedListener = listener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.shipp_user_search, container, false);
        ButterKnifeLite.bind(this, v);

        userClick = new UserHolder.OnItemClickListener() {
            @Override
            public void onClick(User user) {
                if (user.isPrivate() && !user.isFollowingMe()) {
                    Snackbar.make(v, R.string.must_follow_to_pick, Snackbar.LENGTH_LONG).show();
                    return;
                }
                onUserSelectedListener.onUserSelected(user);
                dismiss();
            }
        };
        mText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    search = v.getText().toString();
                    mAdapter.loadMore(search, 0, new UserSearchAdapter.OnLoadMoreListener() {
                        @Override
                        public void onLoaded(int count) {
                            toggleLoading(false);
                        }

                        @Override
                        public void onFail() {
                            toggleLoading(false);
                        }
                    });
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mText.getWindowToken(), 0);
                    toggleLoading(true);
                }
                return true;
            }
        });
        mAdapter = new UserSearchAdapter(getActivity(), userClick);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(linearLayoutManager);
        mRecycler.setAdapter(mAdapter);
        toggleLoading(true);
        mAdapter.loadMore(search, 0, new UserSearchAdapter.OnLoadMoreListener() {
            @Override
            public void onLoaded(int count) {
                toggleLoading(false);
            }

            @Override
            public void onFail() {
                toggleLoading(false);
            }
        });
        return v;
    }


    @OnClick(R.id.shipp_user_search_iv)
    public void search() {
        search = mText.getText().toString().trim();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.hideSoftInputFromWindow(mText.getWindowToken(), 0);
        } catch (NullPointerException ignored) {
        }
        toggleLoading(true);
        mAdapter.loadMore(search, 0, new UserSearchAdapter.OnLoadMoreListener() {
            @Override
            public void onLoaded(int count) {
                toggleLoading(false);
            }

            @Override
            public void onFail() {
                toggleLoading(false);
            }
        });
    }

    private void toggleLoading(boolean b) {
        mLoading.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public interface OnUserSelectedListener {
        public void onUserSelected(User user);

        public void onCancel();
    }
}