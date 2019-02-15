package com.ft.shippo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.error.VolleyError;
import com.ft.shippo.fragments.ChatFragment;
import com.ft.shippo.fragments.FeedFragment;
import com.ft.shippo.fragments.NotificationFragment;
import com.ft.shippo.fragments.SearchFragment;
import com.ft.shippo.fragments.TrendingFragment;
import com.ft.shippo.models.Chat;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.Notification;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.OfflineChat;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.Types;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import java.util.HashMap;
import java.util.List;

import static com.ft.shippo.MyFirebaseMessagingService.updateNotifcation;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.bottom_view)
    BottomNavigationView mBottomNavView;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    HashMap<String, Fragment> mFraglist = new HashMap<>();
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnifeLite.bind(this);
        if (mAuth == null) mAuth = FirebaseAuth.getInstance();
        if (mAuthListener == null)
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() != null) {
                        if (!firebaseAuth.getCurrentUser().getProviders().contains(EmailAuthProvider.PROVIDER_ID) || User.getCurrentUser() == null) {
                            firebaseAuth.signOut();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        } else loadData();
                    } else login();
                }
            };
        mAuth.addAuthStateListener(mAuthListener);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    public void loadData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        try {
                            User.getCurrentUser().updateLocation(MainActivity.this, task.getResult());
                        } catch (Exception ignored) {
                        }
                    } else {
                        task.getException().printStackTrace();
                    }
                }
            });
        else requestPermisssions();
        startService(new Intent(this, SendMessageService.class));
        startService(new Intent(this, OnlineObserverService.class));
        updateNotifcation(this);
        Chat.updateMyConversations(this, new ListObjectListener<Chat>(Types.LIST_CHAT) {
            @Override
            public void onResult(List<Chat> data, String error) {
                for (Chat chat : data) {
                    List<OfflineChat> oldChatList = OfflineChat.find(OfflineChat.class, "chatid = ?", chat.getId());
                    if (oldChatList.size() > 0) {
                        oldChatList.get(0).update(chat).save();
                    } else {
                        OfflineChat offlineChat = new OfflineChat(chat);
                        offlineChat.save();
                    }
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
        Chat.getMessages(this);
        if (FirebaseInstanceId.getInstance().getToken() != null) {
            if(!FirebaseInstanceId.getInstance().getToken().equals(User.getCurrentUser().getFCMToken()))User.getCurrentUser().setFCMToken(this,
                    FirebaseInstanceId.getInstance().getToken(), new ObjectListener<User>(User.class) {
                @Override
                public void onResult(User data, String error) {
                    data.saveLocally();
                }

                @Override
                public void onError(VolleyError error) {

                }
            });
            else Log.d(TAG, User.getCurrentUser().getFCMToken());
        }
        Notification.updateNotfications(this);
        mBottomNavView.setSelectedItemId(R.id.feed_menuitem);
        startFragment(FeedFragment.TAG, false);
        mBottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.feed_menuitem:
                        startFragment(FeedFragment.TAG, true);
                        return true;
                    case R.id.trending_menuitem:
                        startFragment(TrendingFragment.TAG, true);
                        return true;
                    case R.id.messages_menuitem:
                        startFragment(ChatFragment.TAG, true);
                        return true;
                    case R.id.profile_menuitem:
                        startActivity(new Intent(MainActivity.this, UserActivity.class));
                        return false;
                    case R.id.settings_menuitem: startActivity(new Intent(MainActivity.this, SettingActivity.class));
                }
                return false;
            }
        });
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
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        512);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 512: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
            }
        }
    }

    public void startFragment(String tag, boolean can_return) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null)
            switch (tag) {
                case FeedFragment.TAG:
                    fragment = new FeedFragment();
                    ((FeedFragment)fragment).attachListener(new FeedFragment.OnToolbarFeedItemClick() {
                        @Override
                        public void onClick(int view) {
                            if(view == NOTFICATION)startFragment(NotificationFragment.TAG, true);
                            else startFragment(SearchFragment.TAG, true);
                        }
                    });
                    break;
                case SearchFragment.TAG:
                    fragment = new SearchFragment();
                    break;

                case NotificationFragment.TAG :
                    fragment = new NotificationFragment();

                    break;
                case ChatFragment.TAG:
                    fragment = new ChatFragment();
                    break;
                case TrendingFragment.TAG:
                    fragment = new TrendingFragment();
                    ((TrendingFragment)fragment).attachListener(new TrendingFragment.OnToolbarFeedItemClick() {
                        @Override
                        public void onClick(int view) {
                            if(view == NOTFICATION)startFragment(NotificationFragment.TAG, true);
                            else startFragment(SearchFragment.TAG, true);
                        }
                    });
            }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.main_frame, fragment, tag);
        if (can_return) fragmentTransaction.addToBackStack("");
        fragmentTransaction.commit();
    }

    public void login() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}