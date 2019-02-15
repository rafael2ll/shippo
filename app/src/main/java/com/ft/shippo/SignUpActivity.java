package com.ft.shippo;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;

import com.android.volley.error.VolleyError;
import com.facebook.AccessToken;
import com.ft.shippo.adapters.SimpleViewPagerAdapter;
import com.ft.shippo.fragments.SignupBaseFragment;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.CircleViewPagerIndicator;
import com.ft.shippo.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 16/01/18.
 */

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.circle_page_indicator)
    CircleViewPagerIndicator circleViewPagerIndicator;
    SimpleViewPagerAdapter simpleViewPagerAdapter;
    List<SignupBaseFragment> fragmentList = new ArrayList<>();
    Location lastLocation;
    private FirebaseUser mFireUser;
    private User user;
    private int lastPage = 0;
    private AccessToken fb_token;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnifeLite.bind(this);
        user = new User();
        mFireUser = FirebaseAuth.getInstance().getCurrentUser();
        simpleViewPagerAdapter = new SimpleViewPagerAdapter(getSupportFragmentManager());
        requestPermisssions();
        createFragments();
        if (getIntent().hasExtra("fb_token")) {
            fb_token = AccessToken.getCurrentAccessToken();
            if (fb_token != null) user.setFBToken(fb_token);
            user.setFbId(fb_token.getUserId());
        }
        if (mFireUser != null) user.setPhotoUri(mFireUser.getPhotoUrl().toString());
    }

    private void createFragments() {
        fragmentList.add(new SignupBaseFragment().setListener(new SignupBaseFragment.OnFabClick() {
            @Override
            public void onFabClick() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }

            @Override
            public void onStartFragment(SignupBaseFragment fragment) {
                fragment.setHelper(R.string.username_holder)
                        .showEditText(R.string.placeholder_username)
                        .setEditTextSingleLine()
                        .setEditTextInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                        .setIconView(R.drawable.ic_account_box_red)
                        .setTitle(R.string.username);
                if (mFireUser != null) fragment.setEditTextText(mFireUser.getDisplayName());
                simpleViewPagerAdapter.notifyDataSetChanged();
            }
        }));
        fragmentList.add(new SignupBaseFragment().setListener(new SignupBaseFragment.OnFabClick() {
            @Override
            public void onFabClick() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }

            @Override
            public void onStartFragment(SignupBaseFragment fragment) {
                fragment.setHelper(R.string.email_holder)
                        .setEditTextSingleLine()
                        .setEditTextInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .showEditText(R.string.placeholder_email)
                        .setIconView(R.drawable.email)
                        .setTitle(R.string.email);
                if (mFireUser != null) fragment.setEditTextText(mFireUser.getEmail());
                simpleViewPagerAdapter.notifyDataSetChanged();
            }
        }));
        fragmentList.add(new SignupBaseFragment().setListener(new SignupBaseFragment.OnFabClick() {
            @Override
            public void onFabClick() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }

            @Override
            public void onStartFragment(SignupBaseFragment fragment) {
                fragment.setHelper(R.string.password_holder)
                        .setTitle(R.string.gender)
                        .showGenderOption();
                simpleViewPagerAdapter.notifyDataSetChanged();
            }
        }));
        fragmentList.add(new SignupBaseFragment().setListener(new SignupBaseFragment.OnFabClick() {
            @Override
            public void onFabClick() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }

            @Override
            public void onStartFragment(SignupBaseFragment fragment) {
                fragment.setHelper(R.string.password_holder)
                        .showEditText(R.string.placeholder_password)
                        .setIconView(R.drawable.ic_password_text)
                        .setTitle(R.string.password)
                        .setEditTextSingleLine()
                        .setEditTextInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                simpleViewPagerAdapter.notifyDataSetChanged();
            }
        }));

        fragmentList.add(new SignupBaseFragment().setListener(new SignupBaseFragment.OnFabClick() {
            @Override
            public void onFabClick() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }

            @Override
            public void onStartFragment(SignupBaseFragment fragment) {
                fragment.setHelper(R.string.birth_holder)
                        .showEditText(R.string.placeholder_birth)
                        .setEditTextSingleLine()
                        .setEditTextInputType(InputType.TYPE_CLASS_DATETIME)
                        .setIconView(R.drawable.calendar_today)
                        .enableDateMask()
                        .setTitle(R.string.birth);
                simpleViewPagerAdapter.notifyDataSetChanged();
            }
        }));

        fragmentList.add(new SignupBaseFragment().setListener(new SignupBaseFragment.OnFabClick() {
            @Override
            public void onFabClick() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }

            @Override
            public void onStartFragment(SignupBaseFragment fragment) {
                fragment.setHelper(R.string.location_explain)
                        .displaySpinnerCountries()
                        .currentLocation(lastLocation)
                        .setIconView(R.drawable.map_marker_radius)
                        .setTitle(R.string.location);
                simpleViewPagerAdapter.notifyDataSetChanged();
            }
        }));

        fragmentList.add(new SignupBaseFragment().setListener(new SignupBaseFragment.OnFabClick() {
            @Override
            public void onFabClick() {
                user.setBio(fragmentList.get(viewPager.getCurrentItem()).getETText());
                handleCreateAccount();
            }

            @Override
            public void onStartFragment(SignupBaseFragment fragment) {
                fragment.setHelper(R.string.bio_holder)
                        .showEditText(R.string.bio)
                        .setIconView(R.drawable.airballoon)
                        .setTitle(R.string.bio)
                        .isLast();
                simpleViewPagerAdapter.notifyDataSetChanged();
            }
        }));
        simpleViewPagerAdapter.addAll(fragmentList);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                handleFragment((SignupBaseFragment) simpleViewPagerAdapter.getItem(lastPage), lastPage);
                lastPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {

            public void transformPage(@NonNull View view, float position) {
                View title = view.findViewById(R.id.signup_base_textview_title);
                View editText = view.findViewById(R.id.signup_base_edit_text);
                View autoCompleteCities = view.findViewById(R.id.signup_base_autocomplete);
                View countrySpinner = view.findViewById(R.id.signup_base_spinner);
                View helper = view.findViewById(R.id.signup_base_textview_helper);
                View iconView = view.findViewById(R.id.signup_base_icon);
                View gpsView = view.findViewById(R.id.singup_base_iv_gps);
                View fab = view.findViewById(R.id.signup_base_fab);
                if (title != null) {
                    if (position > -1 && position < 1) {
                        float pageWidth = view.getWidth();
                        iconView.setTranslationX(position * pageWidth * 0.5f);
                        title.setTranslationX((position * pageWidth) * 0.11f);
                        helper.setTranslationX(position * pageWidth * 0.2f);
                        if (editText.getVisibility() != View.GONE)
                            editText.setTranslationX(position * pageWidth * 0.8f);
                        if (autoCompleteCities.getVisibility() != View.GONE)
                            autoCompleteCities.setTranslationX(position * pageWidth * 0.8f);
                        if (countrySpinner.getVisibility() != View.GONE)
                            countrySpinner.setTranslationX(position * pageWidth * 0.6f);
                        if (gpsView.getVisibility() != View.GONE)
                            gpsView.setTranslationX(position * pageWidth * 0.9f);
                        fab.setTranslationX(position * pageWidth * 1.2f);

                        if (position == 0) {
                            view.setScaleX(1);
                            view.setScaleY(1);
                        }
                    }
                }
            }
        });
        viewPager.setAdapter(simpleViewPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        circleViewPagerIndicator.setViewPager(viewPager);
    }

    private void handleCreateAccount() {
        if (Utils.isAnyNull(user.getUsername()) || user.getUsername().length() < 3) {
            Snackbar.make(circleViewPagerIndicator, R.string.username_too_short, Snackbar.LENGTH_LONG).show();
            viewPager.setCurrentItem(0, true);
            return;
        }
        if (!Utils.isEmailValid(user.getEmail())) {
            Snackbar.make(circleViewPagerIndicator, R.string.invalid_email, Snackbar.LENGTH_LONG).show();
            viewPager.setCurrentItem(1, true);
            return;
        }
        if (!Utils.isPasswordValid(user.getPassword(), user.getUsername())) {
            Snackbar.make(circleViewPagerIndicator, R.string.password_too_weak, Snackbar.LENGTH_LONG).show();
            viewPager.setCurrentItem(3, true);
            return;
        }
        try {
            if (user.getAge() < 5 || user.getAge() > 100) {
                Snackbar.make(circleViewPagerIndicator, R.string.invalid_date, Snackbar.LENGTH_LONG).show();
                viewPager.setCurrentItem(4, true);
                return;
            }
        } catch (Exception e) {
            Snackbar.make(circleViewPagerIndicator, R.string.invalid_date, Snackbar.LENGTH_LONG).show();
            viewPager.setCurrentItem(4, true);
            return;
        }
        if (user.getCity() == null || user.getCountry() == null) {
            Snackbar.make(circleViewPagerIndicator, R.string.city_or_country_missing, Snackbar.LENGTH_LONG).show();
            viewPager.setCurrentItem(5, true);
            return;
        }
        showContentOrLoadingIndicator(true);
        if (mFireUser != null) {
            AuthCredential emailAuthCredential = EmailAuthProvider.getCredential(user.getEmail(), user.getPassword());
            mFireUser.linkWithCredential(emailAuthCredential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    user.save(SignUpActivity.this, new ObjectListener<User>(User.class) {
                        @Override
                        public void onResult(User data, String error) {
                            data.saveLocally();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            error.printStackTrace();
                            showContentOrLoadingIndicator(false);
                            FirebaseAuth.getInstance().signOut();
                            Snackbar.make(circleViewPagerIndicator, R.string.error_try_again, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    showContentOrLoadingIndicator(false);
                    Snackbar.make(circleViewPagerIndicator, R.string.error_try_again, Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    user.save(SignUpActivity.this, new ObjectListener<User>(User.class) {
                        @Override
                        public void onResult(User data, String error) {
                            data.saveLocally();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            error.printStackTrace();
                            showContentOrLoadingIndicator(false);
                            Snackbar.make(circleViewPagerIndicator, R.string.error_try_again, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    showContentOrLoadingIndicator(false);
                    if( e instanceof FirebaseAuthUserCollisionException) {
                        viewPager.setCurrentItem(1);
                        Snackbar.make(circleViewPagerIndicator, "This email is already in use", Snackbar.LENGTH_LONG).show();
                    }else if(e instanceof FirebaseAuthWeakPasswordException){
                        viewPager.setCurrentItem(3);
                        Snackbar.make(circleViewPagerIndicator, R.string.password_too_weak, Snackbar.LENGTH_LONG).show();
                    }
                    else Snackbar.make(circleViewPagerIndicator, R.string.error_try_again, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleFragment(SignupBaseFragment fragment, int currentItem) {
        switch (currentItem) {
            case 0:
                user.setUsername(fragment.getETText());
                break;
            case 1:
                user.setEmail(fragment.getETText());
                break;

            case 2:
                user.setMan(fragment.getgender());
                break;
            case 3:
                user.setPassword(fragment.getETText());
                break;
            case 4:
                user.setBirth(fragment.getETText());
                break;

            case 5:
                if(fragment.getCitySelected() != null) {
                    user.setCity(fragment.getCitySelected());
                    user.setCountry(fragment.getCitySelected().getCountry());
                    user.setLocation(fragment.getCitySelected().getLocation());
                }
                break;
            case 6:
                user.setBio(fragment.getETText());
        }
    }

    public void requestPermisssions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        200);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }

    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermisssions();
            return;
        }
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        lastLocation = locationManager.getLastKnownLocation(provider);
    }

    private void showContentOrLoadingIndicator(boolean isLoading) {
        // Decide which view to hide and which to show.
        final View mMainLayout = findViewById(R.id.acitvity_signup_views);
        final View mLoadingView = findViewById(R.id.activity_signup_loading);
        final View hideView = isLoading ? mMainLayout : mLoadingView;
        final View showView = isLoading ? mLoadingView : mLoadingView;

        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);
        showView.setTranslationY(0);
        showView.animate()
                .alpha(1f)
                .translationY(showView.getHeight())
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                .setListener(null).start();
        hideView.animate()
                .alpha(0f)
                .translationY(0)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                }).start();
    }
}
