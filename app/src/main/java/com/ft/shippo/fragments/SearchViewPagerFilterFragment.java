package com.ft.shippo.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.adapters.CountriesAdapter;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Utils;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 30/01/18.
 */

public class SearchViewPagerFilterFragment extends Fragment {
    @BindView(R.id.fragment_search_filter_country_ll)
    View countryView;
    @BindView(R.id.fragment_search_filter_sp_filter)
    private Spinner spinnerFilters;
    @BindView(R.id.fragment_search_filter_sp_range)
    private Spinner spinnerRange;
    @BindView(R.id.fragment_search_filter_sp_countries)
    private Spinner spinnerCountry;
    private FilterAdapter filterAdapter;
    private FilterAdapter rangeAdapter;
    private CountriesAdapter countriesAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        countriesAdapter = new CountriesAdapter(getActivity());
        filterAdapter = new FilterAdapter();
        rangeAdapter = new FilterAdapter();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        filterAdapter.addItem(new Item(R.string.new_, R.drawable.ic_flash_darker))
                .addItem(new Item(R.string.recommended, R.drawable.ic_recommended_darker))
                .addItem(new Item(R.string.popular, R.drawable.ic_star_darker))
                .addItem(new Item(R.string.controversial, R.drawable.ic_trending_down_darker));


        rangeAdapter.addItem(new Item(R.string.world_wide, R.drawable.ic_public_grey_700_24dp))
                .addItem(new Item(R.string.my_city, R.drawable.ic_city_darker))
                .addItem(new Item(R.string.miles180, R.drawable.ic_location_mark_darker))
                .addItem(new Item(User.getCurrentUser().getCountryName(), null)
                        .setIconPath(Utils.getCountryImage(getActivity(), User.getCurrentUser().getCountryName())))
                .addItem(new Item(R.string.pick_country, R.drawable.ic_location_darker));

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_filter, container, false);
        ButterKnifeLite.bind(this, v);
        spinnerFilters.setAdapter(filterAdapter);
        spinnerRange.setAdapter(rangeAdapter);
        spinnerCountry.setAdapter(countriesAdapter);

        spinnerRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 4) countryView.setVisibility(View.VISIBLE);
                else countryView.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }

    public class FilterAdapter extends BaseAdapter {

        List<Item> itemList = new ArrayList<>();


        FilterAdapter addItem(Item item) {
            this.itemList.add(item);
            return this;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return itemList.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = (Item) getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.holder_filter_item, parent, false);


            TextView textView = convertView.findViewById(R.id.holder_filter_item_name);
            ImageView imageView = convertView.findViewById(R.id.holder_filter_item_icon);

            textView.setText(item.getName());
            imageView.setImageDrawable(item.getIcon());
            if (item.getIconPath() != -1)
                GlideRequestManager.get().asBitmap()
                        .load(item.getIconPath()).override(35, 30)
                        .into(imageView);

            return convertView;
        }
    }

    public class Item {
        private String name;
        private Drawable icon;
        private int iconPath = -1;

        public Item(String name, Drawable icon) {
            this.name = name;
            this.icon = icon;
        }

        public Item(int text, int image) {
            this.name = getResources().getString(text);
            this.icon = getResources().getDrawable(image);
        }

        public int getIconPath() {
            return iconPath;
        }

        public Item setIconPath(int i) {
            this.iconPath = i;
            return this;
        }

        public String getName() {
            return name;
        }

        public Drawable getIcon() {
            return icon;
        }
    }
}
