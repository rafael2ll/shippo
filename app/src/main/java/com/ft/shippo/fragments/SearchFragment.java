package com.ft.shippo.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.adapters.SimpleViewPagerAdapter;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

/**
 * Created by rafael on 30/01/18.
 */

public class SearchFragment extends Fragment {
    public static final String TAG = "SearchFragment";

    @BindView(R.id.view_pager)
    private ViewPager viewPager;
    @BindView(R.id.tab_layout)
    private TabLayout tabLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.fragment_search_tiet_search)
    private TextInputEditText inputEditText;
    private SearchUserViewPagerFragment userViewPagerFragment;
    private SearchShippViewPagerFragment shippViewPagerFragment;
    private SimpleViewPagerAdapter viewPagerAdapter;

    private String search = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewPagerFragment = new SearchUserViewPagerFragment();
        shippViewPagerFragment = new SearchShippViewPagerFragment();
        viewPagerAdapter = new SimpleViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.add(shippViewPagerFragment, getResources().getString(R.string.shipps));
        viewPagerAdapter.add(userViewPagerFragment, getResources().getString(R.string.people));
       // viewPagerAdapter.add(new Fragment(), getResources().getString(R.string.tags));
       // viewPagerAdapter.add(new SearchViewPagerFilterFragment(), "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnifeLite.bind(this, v);
        tabLayout.setupWithViewPager(viewPager, false);
        viewPager.setAdapter(viewPagerAdapter);
       // tabLayout.getTabAt(3).setIcon(R.drawable.ic_filter_list_grey);
        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateFrags(v.getText().toString().trim());
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    try {
                        imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
                    } catch (NullPointerException ignored) {
                    }
                    return true;
                }
                return false;
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 3)
                    tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                if (tab.getPosition() == 3)
                    tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_500), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return v;
    }

    @OnClick(R.id.fragment_search_iv_search)
    public void search() {
        updateFrags(inputEditText.getText().toString());
    }

    private void updateFrags(String s) {
        search = s;
        shippViewPagerFragment.onTextUpdate(search);
        userViewPagerFragment.onTextUpdate(search);
        viewPagerAdapter.notifyDataSetChanged();
    }
}
