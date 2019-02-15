package com.ft.shippo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.ft.shippo.R;
import com.ft.shippo.adapters.CountriesAdapter;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rafael on 04/03/18.
 */

public class TrendingFilterDialog extends BottomSheetDialogFragment {
    @BindView(R.id.other_country_layout)
    View otherCountryView;
    @BindView(R.id.checkbox_only_friends)
    CheckBox checkBoxOnlyFriends;
    @BindView(R.id.spinner)
    Spinner spinner;

    List<Integer> filters = Arrays.asList(R.id.radio_button_world_wide, R.id.radio_button_my_country, R.id.radio_button_180miles, R.id.radio_button_my_city, R.id.radio_button_other);
    int filter_item= 0;
    boolean only_friends = false;
    String other_country;
    private Callback callback;
    private CountriesAdapter countriesAdapter;

    public TrendingFilterDialog(){}

    public TrendingFilterDialog setParams(int filter_item, boolean only_friends, String other_country, Callback callback){
        this.filter_item = filter_item;
        this.only_friends = only_friends;
        this.other_country = other_country;
        this.callback = callback;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_trending_filters, container, false);
        ButterKnifeLite.bind(this, v);
        countriesAdapter = new CountriesAdapter(getActivity());
        spinner.setAdapter(countriesAdapter);
        if(other_country != null){
            int index =countriesAdapter.findPostion(other_country);
            spinner.setSelection(index);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                other_country = countriesAdapter.getItem(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(only_friends) checkBoxOnlyFriends.setChecked(true);
        checkBoxOnlyFriends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                only_friends = isChecked;
            }
        });

        ((RadioGroup)v.findViewById(R.id.radio_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                filter_item = filters.indexOf(checkedId);
                if(filter_item == filters.indexOf(R.id.radio_button_other)) otherCountryView.setVisibility(View.VISIBLE);
                else otherCountryView.setVisibility(View.GONE);
            }
        });
        return v;
    }

    @OnClick(R.id.button)
    public void apply(){
        callback.onApplied(filter_item, only_friends, other_country);
        dismiss();
    }
    public  interface Callback{
        public void onApplied(int filter, boolean only_friends, String country_code);
    }
}
