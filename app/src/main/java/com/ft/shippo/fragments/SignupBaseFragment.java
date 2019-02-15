package com.ft.shippo.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.text.emoji.widget.EmojiAppCompatEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.adapters.CountriesAdapter;
import com.ft.shippo.models.City;
import com.ft.shippo.models.Country;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.ObjectListener;
import com.google.gson.reflect.TypeToken;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.util.Calendar;
import java.util.List;

/**
 * Created by rafael on 31/01/18.
 */

public class SignupBaseFragment extends Fragment {
    private  OnFabClick fabClick;
    @BindView(R.id.signup_base_textview_title)
    TextView title;
    @BindView(R.id.singup_base_iv_gps)
    ImageView gpsView;
    @BindView(R.id.signup_base_textview_helper)
    TextView helper;
    @BindView(R.id.signup_base_gender_views)
    LinearLayout genderViews;
    @BindView(R.id.signup_base_text_views)
    LinearLayout textViews;
    @BindView(R.id.signup_base_iv_female)
    private ImageView femaleImage;
    @BindView(R.id.signup_base_iv_male)
    private ImageView maleImage;
    @BindView(R.id.signup_base_tv_female)
    private TextView femaleText;
    @BindView(R.id.signup_base_tv_male)
    private TextView maleText;
    @BindView(R.id.signup_base_autocomplete)
    private AutoCompleteTextView autoCompleteCities;
    @BindView(R.id.signup_base_edit_text)
    private EmojiAppCompatEditText editText;
    @BindView(R.id.signup_base_fab)
    private FloatingActionButton fab;
    @BindView(R.id.signup_base_icon)
    private ImageView iconView;
    @BindView(R.id.signup_base_spinner)
    private Spinner countrySpinner;
    //For date editText
    private String ddmmyyyy = "ddmmyyyy";
    private Calendar cal = Calendar.getInstance();
    private String current = "";

    //For location
    private CountriesAdapter countriesAdapter;
    private Country countrySelected;
    private ArrayAdapter<City> citiesAdapter;
    private City citySelected;
    private Location currentLocation = null;
    private String country = "";
    private boolean gender = false;

    public SignupBaseFragment(){}

    public SignupBaseFragment setListener(OnFabClick click) {
        this.fabClick = click;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_base_fragment, container, false);
        ButterKnifeLite.bind(this, v);
        fabClick.onStartFragment(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabClick.onFabClick();
            }
        });
        return v;
    }

    public SignupBaseFragment setEditTextInputType(int type) {
        editText.setInputType(type);
        return this;
    }

    public SignupBaseFragment setEditTextSingleLine() {
        editText.setMaxLines(1);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return this;
    }

    public SignupBaseFragment displaySpinnerCountries() {
        countriesAdapter = new CountriesAdapter(getActivity());
        countrySpinner.setVisibility(View.VISIBLE);
        countrySpinner.setAdapter(countriesAdapter);
        countrySpinner.setSelection(countriesAdapter.getMyCountryIndex());

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countrySelected = countriesAdapter.getItem(position);
                enableCityAutoComplete(countrySelected.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (!countriesAdapter.isEmpty()) countrySelected = countriesAdapter.getItem(0);
            }
        });
        return this;
    }

    public SignupBaseFragment setHelper(@StringRes int text) {
        helper.setText(text);
        return this;
    }

    public SignupBaseFragment setTitle(@StringRes int text) {
        title.setText(text);
        return this;
    }

    public SignupBaseFragment setIconView(@DrawableRes int icon) {
        iconView.setImageResource(icon);
        return this;
    }

    public SignupBaseFragment showEditText(@StringRes int hint) {
        editText.setVisibility(View.VISIBLE);
        editText.setHint(hint);
        return this;
    }

    public void showGenderOption() {
        genderViews.setVisibility(View.VISIBLE);
        textViews.setVisibility(View.GONE);
        iconView.setVisibility(View.GONE);
    }

    public SignupBaseFragment enableDateMask() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(current)) {
                    return;
                }

                String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                int cl = clean.length();
                int sel = cl;
                for (int i = 2; i <= cl && i < 6; i += 2) {
                    sel++;
                }
                //Fix for pressing delete next to a forward slash
                if (clean.equals(cleanC)) sel--;

                if (clean.length() < 8) {
                    clean = clean + ddmmyyyy.substring(clean.length());
                } else {
                    //This part makes sure that when we finish entering numbers
                    //the date is correct, fixing it otherwise
                    int day = Integer.parseInt(clean.substring(0, 2));
                    int mon = Integer.parseInt(clean.substring(2, 4));
                    int year = Integer.parseInt(clean.substring(4, 8));

                    mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                    cal.set(Calendar.MONTH, mon - 1);
                    year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
                    cal.set(Calendar.YEAR, year);
                    // ^ first set year for the line below to work correctly
                    //with leap years - otherwise, date e.g. 29/02/2012
                    //would be automatically corrected to 28/02/2012

                    day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal.getActualMaximum(Calendar.DATE) : day;
                    clean = String.format("%02d%02d%02d", day, mon, year);
                }

                clean = String.format("%s/%s/%s", clean.substring(0, 2),
                        clean.substring(2, 4),
                        clean.substring(4, 8));

                sel = sel < 0 ? 0 : sel;
                current = clean;
                editText.setText(current);
                editText.setSelection(sel < current.length() ? sel : current.length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return this;
    }

    public String getETText() {
        return editText.getText().toString();
    }

    public City getCitySelected() {
        return citySelected;
    }

    public SignupBaseFragment enableCityAutoComplete(String country) {
        this.country = country;
        gpsView.setVisibility(View.VISIBLE);
        autoCompleteCities.setVisibility(View.VISIBLE);
        autoCompleteCities.setMaxLines(1);
        autoCompleteCities.setThreshold(3);
        citiesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        autoCompleteCities.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 4) findCities(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        autoCompleteCities.setAdapter(citiesAdapter);
        autoCompleteCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                citySelected = citiesAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (citiesAdapter.getCount() > 0) citySelected = citiesAdapter.getItem(0);
            }
        });
        gpsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, false);
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    currentLocation = locationManager.getLastKnownLocation(provider);
                    City.findCity(getActivity(), currentLocation, new ObjectListener<City>(City.class) {
                        @Override
                        public void onResult(City data, String error) {
                            citySelected = data;
                            autoCompleteCities.setText(data.getName());
                            if(citySelected != null)countrySpinner.setSelection(countriesAdapter.getMyCountryIndex());
                        }

                        @Override
                        public void onError(VolleyError error) {

                        }
                    });
                } else {
                    Snackbar.make(fab, R.string.location_error, Snackbar.LENGTH_LONG).show();
                }
            }
        });
        return this;
    }

    private synchronized void findCities(String s) {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currentLocation = locationManager.getLastKnownLocation(provider);
        }
        City.findCities(getActivity(), s, country, currentLocation, new ListObjectListener<City>(new TypeToken<List<City>>() {
        }.getType()) {
            @Override
            public void onResult(List<City> data, String error) {
                if (citiesAdapter != null) {
                    citiesAdapter.clear();
                    citiesAdapter.addAll(data);
                    citiesAdapter.notifyDataSetChanged();
                    if (data.size() > 0) citySelected = data.get(0);
                }
            }

            @Override
            public void onError(VolleyError error) {
                citiesAdapter.clear();
                citiesAdapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.signup_base_card_female)
    public void femaleClick() {
        gender = false;
        genderClick();
    }

    @OnClick(R.id.signup_base_card_male)
    public void maleClick() {
        gender = true;
        genderClick();
    }

    private void genderClick() {
        Drawable female = VectorDrawableCompat.create(getResources(), R.drawable.human_female, null);
        Drawable male = VectorDrawableCompat.create(getResources(), R.drawable.human_male, null);
        DrawableCompat.setTint(female, ContextCompat.getColor(getActivity(), gender ? R.color.grey_200 : R.color.pink_A200));
        DrawableCompat.setTint(male, ContextCompat.getColor(getActivity(), gender ? R.color.blue_A200 : R.color.grey_200));
        femaleImage.setImageDrawable(female);
        maleImage.setImageDrawable(male);

        femaleText.setTextColor(ContextCompat.getColor(getActivity(), gender ? R.color.grey_200 : R.color.pink_A200));
        maleText.setTextColor(ContextCompat.getColor(getActivity(), gender ? R.color.blue_A200 : R.color.grey_200));
    }

    public SignupBaseFragment currentLocation(Location lastLocation) {
        currentLocation = lastLocation;
        return this;
    }

    public SignupBaseFragment isLast() {
        fab.setImageResource(R.drawable.ic_done_white);
        return this;
    }

    public void setEditTextText(String editTextText) {
        this.editText.setText(editTextText);
    }

    public boolean getgender() {
        return gender;
    }

    public interface OnFabClick {
        void onFabClick();

        void onStartFragment(SignupBaseFragment fragment);
    }
}
