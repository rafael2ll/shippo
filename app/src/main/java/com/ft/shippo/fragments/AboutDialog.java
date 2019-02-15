package com.ft.shippo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.ft.shippo.R;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

/**
 * Created by rafael on 08/03/18.
 */

public class AboutDialog extends DialogFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.web_view)
    WebView web_view;

    int type = 0; //0 : privacy_policy // 1 : terms of use // 2: third libs

    public AboutDialog setType(int type){
        this.type = type;
        return  this;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.about_dialog, container, false);
        ButterKnifeLite.bind(this, v);
        toolbar.setTitle(type == 0 ? R.string.privacy_policy : type == 1 ? R.string.terms_of_use : R.string.about);
        web_view.loadUrl(String.format("file:///android_asset/%s.html", type == 0 ? "privacy_policy" : type == 1 ? "terms_and_conditions_and_privacy" : "third_party_libs"));
        return v;
    }
}
