package com.ft.shippo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.models.Country;
import com.google.gson.Gson;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by rafael on 31/01/18.
 */

public class CountriesAdapter extends BaseAdapter {

    @BindView(R.id.holder_country_icon)
    ImageView imageIcon;
    @BindView(R.id.holder_country_name)
    TextView textName;

    private int my_country_index = 0;
    private List<Country> countryList = new ArrayList<>();
    private Context ctx;

    public CountriesAdapter(@NonNull Context context) {
        String json = null;
        this.ctx = context;
        try {
            InputStream inputStream = context.getAssets().open("countries_.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
            String my_country = context.getResources().getConfiguration().locale.getCountry();
            Log.d("country", my_country);
            JSONArray jsonArray = new JSONArray(json);
            for(int i =0; i<jsonArray.length(); i++){
                JSONObject object = jsonArray.getJSONObject(i);
                String name = object.getString("name");
                String code = object.getString("code");
                String _id = object.getJSONObject("_id").getString("$oid");
                Country c = new Country(_id, name, code);
                if(my_country.toLowerCase().equals(code.toLowerCase())) my_country_index =i;
                if (Objects.equals(code, "DO")) code = "doo";
                c.setIcon(context.getResources().getIdentifier(code.toLowerCase().replace("-", "_"), "raw", context.getPackageName()));
                countryList.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(ctx).inflate(R.layout.holder_country, parent, false);
        ButterKnifeLite.bind(this, convertView);
        Country country = getItem(position);
        if (country.getIcon() != 0) imageIcon.setImageResource(country.getIcon());
        textName.setText(country.getName());
        return convertView;
    }


    public int getMyCountryIndex() {
        return my_country_index;
    }

    @Nullable
    @Override
    public Country getItem(int position) {
        return countryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return countryList.size();
    }

    public int findPostion(String other_country) {
        int i =0;
        for(Country c : countryList){
            if(c.getId().equals(other_country)) return  i;
            else i++;
        }
        return -1;
    }
}
