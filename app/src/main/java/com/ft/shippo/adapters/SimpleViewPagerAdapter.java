package com.ft.shippo.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 30/01/18.
 */

public class SimpleViewPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> fragmentsNames = new ArrayList<>();

    public SimpleViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void add(Fragment fragment, String name) {
        fragmentList.add(fragment);
        fragmentsNames.add(name);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentsNames.get(position);
    }

    public void addAll(List<? extends Fragment> fList) {
        fragmentList.addAll(fList);
    }
}
