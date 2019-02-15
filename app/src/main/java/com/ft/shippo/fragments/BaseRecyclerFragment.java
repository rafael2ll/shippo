package com.ft.shippo.fragments;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ft.shippo.R;

/**
 * Created by rafael on 20/01/18.
 */

public class BaseRecyclerFragment extends Fragment {
    protected Toolbar toolbar;
    RecyclerView mRecycler;
    FloatingActionButton mFab;
    SwipeRefreshLayout refreshLayout;
    View appbar;
    View errorView;
    TextView errorTextView;
    LinearLayout cardButton;
    View.OnClickListener onClickListener;
    ImageView errorButtonImage;
    TextView errorButtonTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler, container, false);
        mRecycler = v.findViewById(R.id.recycler_view);
        mFab = v.findViewById(R.id.fab);
        refreshLayout = v.findViewById(R.id.refresh_layout);
        appbar = v.findViewById(R.id.toolbar_includer);
        toolbar = v.findViewById(R.id.toolbar);
        cardButton = v.findViewById(R.id.ll);
        errorTextView = v.findViewById(R.id.textView);
        errorView = v.findViewById(R.id.error_view_include);
        errorButtonTextView = v.findViewById(R.id.text_view_button_error);
        errorButtonImage = v.findViewById(R.id.image_view_icon);
        cardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(v);
            }
        });
        return v;
    }

    public void enableToolbar(@StringRes int title) {
        appbar.setVisibility(View.VISIBLE);
        toolbar.setTitle(title);

    }

    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        refreshLayout.setOnRefreshListener(listener);
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecycler.setAdapter(adapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRecycler.setLayoutManager(layoutManager);
    }

    public void apply(RecyclerView.LayoutManager layoutManager, RecyclerView.Adapter adapter) {
        setLayoutManager(layoutManager);
        setAdapter(adapter);
    }

    public void setFabClick(final View.OnClickListener listener) {
        mFab.setVisibility(View.VISIBLE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
            }
        });
    }

     void showErrorView(@StringRes int res) {
        errorTextView.setText(res);
        errorButtonTextView.setText(R.string.try_again);
        errorButtonImage.setImageResource(R.drawable.ic_refresh_primary);
        errorView.setVisibility(View.VISIBLE);
    }

     void showFeedEmpty() {
        errorTextView.setText(R.string.feed_is_empty);
        errorButtonImage.setImageResource(R.drawable.ic_follow_primary);
        errorButtonTextView.setText(R.string.follow_people);
        errorView.setVisibility(View.VISIBLE);
    }

    boolean isFeedEmpty(){
        return errorButtonTextView.getText().equals(getString(R.string.follow_people));
    }
     void hideErrorView() {
        errorView.setVisibility(View.GONE);
    }

    public void setErrorClick(View.OnClickListener listener){
        this.onClickListener = listener;
    }
    public void setabIcon(@DrawableRes int res) {
        mFab.setImageResource(res);
    }
}
