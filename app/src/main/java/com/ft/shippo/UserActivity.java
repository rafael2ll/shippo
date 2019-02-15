package com.ft.shippo;

import android.support.text.emoji.widget.EmojiAppCompatEditText;
import android.support.text.emoji.widget.EmojiAppCompatTextView;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.error.VolleyError;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.ft.shippo.adapters.ActivityAdapter;
import com.ft.shippo.fragments.UserActiviyFragment;
import com.ft.shippo.fragments.UserShippsFragment;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 02/02/18.
 */

public class UserActivity extends AppCompatActivity {

    public static final String TAG = "UserActivity";
    public static final String USER_KEY = "user_key";
    ActivityAdapter activityAdapter;
    @BindView(R.id.fragment_user_tv_username)
    private EmojiAppCompatTextView textViewUsername;
    @BindView(R.id.fragment_user_tv_bio)
    private EmojiAppCompatEditText textViewBio;
    @BindView(R.id.fragment_user_tv_follow)
    private TextView textViewFollow;
    @BindView(R.id.fragment_user_tv_followers_count)
    private TextView textViewFollowerCount;
    @BindView(R.id.fragment_user_foloowing_count)
    private TextView textViewFollowingCount;
    @BindView(R.id.fragment_user_tv_location)
    private TextView textViewLocation;
    @BindView(R.id.fragment_user_civ_profile)
    private CircleImageView civProfilePic;
    //  @BindView(R.id.toolbar)
    //private Toolbar toolbar;
    @BindView(R.id.tab_layout)
    private TabLayout tabLayout;

    private User user;
    private boolean fromFacebook;
    private File pictureFile;

    UserShippsFragment userShippsFragment;
    UserActiviyFragment userActiviyFragment;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnifeLite.bind(this);

        String user_id = getIntent().getExtras().getString(USER_KEY, User.getCurrentUser().getId());
        if (Objects.equals(user_id, User.getCurrentUser().getId())) {
            user = User.getCurrentUser();
            findViewById(R.id.activity_user_iv_change_pic).setVisibility(View.VISIBLE);
            textViewFollow.setText(R.string.edit);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) startFragment(userActiviyFragment);
                else startFragment(userShippsFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (user != null) loadData();

        User.findOneByField(this, "_id", user_id,
                "username gender follower_count photoUri is_private following_count" +
                        " city_name country_name country birth bio", new ObjectListener<User>(User.class) {
                    @Override
                    public void onResult(User data, String error) {
                        user = data;
                        loadData();
                    }

                    @Override
                    public void onError(VolleyError error) {

                    }
                });
    }

    private void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.replacer, fragment)
                .commit();
    }

    private void loadData() {
        loadSummaries();
        if(!User.isMe(user)) loadIsFollowing();
        userActiviyFragment = new UserActiviyFragment().setUser(user);
        userShippsFragment = new UserShippsFragment().setUser(user);
        startFragment(userActiviyFragment);
    }

    @OnClick(R.id.fragment_user_foloowing_count)
    public void seeFollowing() {
        Intent intent = new Intent(this, UserFollowDetailsActivity.class);
        intent.setAction(UserFollowDetailsActivity.Type.FOLLOWING.toString());
        intent.putExtra(USER_KEY, user.getId());

        startActivity(intent);
    }

    @OnClick(R.id.fragment_user_tv_followers_count)
    public void seeFollowers() {
        Intent intent = new Intent(this, UserFollowDetailsActivity.class);
        intent.setAction(UserFollowDetailsActivity.Type.FOLLOWERS.toString());
        intent.putExtra(USER_KEY, user.getId());

        startActivity(intent);
    }

    @OnClick(R.id.fragment_user_tv_follow)
    public void follow(){
        if(User.isMe(user) && !textViewBio.isEnabled()){
            textViewBio.setEnabled(true);
            textViewBio.setFocusable(true);
            textViewBio.requestFocus();
            textViewFollow.setText(R.string.save);
        }else if(User.isMe(user)){
            textViewBio.setEnabled(false);
            textViewBio.setFocusable(false);
            textViewFollow.setText(R.string.edit);
            user.setBio(textViewBio.getText().toString());
            user.saveLocally();
            User.updateBio(this, user.getBio(), new BooleanListener(){
                @Override
                public void onResult(boolean data, String error) {
                    Toast.makeText(GlideRequestManager.mContext, R.string.bio_updated, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(GlideRequestManager.mContext, R.string.error_try_again, Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            if(user.isPending() ) return;
            if(user.isPrivate() && !user.isFollowingMe()){
                user.setIsPending(true);
                loadIsFollowing();
                User.requestFollow(this, user.getId(),new BooleanListener(){
                    @Override
                    public void onResult(boolean data, String error){
                        if(!data) user.setIsPending(false);
                        loadIsFollowing();
                    }
                    @Override
                    public void onError(VolleyError error){
                        user.setIsPending(false);
                        loadIsFollowing();
                    }
                });

            }
            else if(user.isFollowingMe()){
                user.setIsFollowingMe(false);
                loadIsFollowing();
                User.getCurrentUser().unfollow(this, user.getId(), new BooleanListener() {
                    @Override
                    public void onResult(boolean data, String error) {
                        user.setIsFollowingMe(!data);
                        loadIsFollowing();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        user.setIsFollowingMe(true);
                        loadIsFollowing();
                    }
                });
            }else{
                user.setIsFollowingMe(true);
                loadIsFollowing();
                User.getCurrentUser().follow(this, user.getId(), new BooleanListener() {
                    @Override
                    public void onResult(boolean data, String error) {
                        user.setIsFollowingMe(data);
                        loadIsFollowing();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        user.setIsFollowingMe(false);
                        loadIsFollowing();
                    }
                });
            }
        }
    }

    private void loadIsFollowing() {
        if(user.isPending()){
            textViewFollow.setText(R.string.pending);
            textViewFollow.setBackgroundResource(R.drawable.rounder_border_colored);
            textViewFollow.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textViewFollow.setEnabled(false);
        }
        else if (user.isFollowingMe()) {
            textViewFollow.setText(R.string.following);
            textViewFollow.setBackgroundResource(R.drawable.round_square_filled_colored);
            textViewFollow.setTextColor(Color.WHITE);
            textViewFollow.setEnabled(true);
        } else {
            textViewFollow.setText(R.string.follow);
            textViewFollow.setBackgroundResource(R.drawable.rounder_border_colored);
            textViewFollow.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            textViewFollow.setEnabled(true);
        }
    }
    private void loadSummaries() {
        textViewBio.setText(user.getBio());
        textViewUsername.setText(String.format(Locale.getDefault(), "%s, %d", user.getUsername(), user.getAge()));
        textViewLocation.setText(String.format("%s, %s", user.getCityName(), user.getCountryName()));
        textViewFollowerCount.setText(getString(R.string.follower_count, user.getFollowerCountFormatted()));
        textViewFollowingCount.setText(getString(R.string.following_count, user.getFollowingCountFormatted()));

        GlideRequestManager.get()
                .load(user.getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .override(250).into(civProfilePic);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    private void pickPicture() {
        ImagePicker.create(this)
                .returnMode(ReturnMode.ALL) // set whether pick and / or camera action should return immediate result or not.
                .single()
                .folderMode(true)
                .toolbarFolderTitle(getString(R.string.pick_a_picture)) // folder selection title
                .toolbarImageTitle(getString(R.string.pick_a_picture)) // image selection title
                .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color
                .showCamera(true) // show camera or not (true by default)
                .theme(R.style.AppTheme) // must inherit ef_BaseTheme. please refer to sample
                .enableLog(true) // disabling log
                .start();
    }

    private void loadPhoto() {
        if(fromFacebook){
            String photoUrl = "https://graph.facebook.com/" + user.getFbId() + "/picture?height=500";
            GlideRequestManager.get()
                    .load(photoUrl)
                    .into(civProfilePic);
        }else if(pictureFile != null){
            GlideRequestManager.get().load(pictureFile)
                    .into(civProfilePic);
        }else{
            civProfilePic.setImageResource(R.color.blue);
        }
    }
    @OnClick(R.id.activity_user_iv_change_pic)
    public void addPicLater(){
        final boolean facebookEnabled = FirebaseAuth.getInstance().getCurrentUser().getProviders().contains(FacebookAuthProvider.PROVIDER_ID);

        AlertDialog.Builder builder  =  new AlertDialog.Builder(this)
                .setItems(facebookEnabled ? R.array.picture_fb_array : R.array.picture_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            if(facebookEnabled){
                                fromFacebook = true;
                                User.setFBasPhoto(UserActivity.this,new BooleanListener(){

                                    @Override
                                    public void onResult(boolean data, String error) {
                                        Toast.makeText(GlideRequestManager.mContext, R.string.photo_updated, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(VolleyError error) {
                                        Toast.makeText(GlideRequestManager.mContext, R.string.update_picture_fail_error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else pickPicture();
                            loadPhoto();
                        }else if (which == 1){
                            if(!facebookEnabled){
                                fromFacebook = false;
                                pictureFile = null;
                                User.setNoPhoto(UserActivity.this,new BooleanListener(){

                                    @Override
                                    public void onResult(boolean data, String error) {
                                        Toast.makeText(GlideRequestManager.mContext, R.string.photo_updated, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(VolleyError error) {
                                        Toast.makeText(GlideRequestManager.mContext, R.string.update_picture_fail_error, Toast.LENGTH_LONG).show();
                                    }
                                });
                                loadPhoto();
                            }else pickPicture();
                        }else{
                            fromFacebook = false;
                            pictureFile = null;
                            loadPhoto();
                        }
                    }
                });

        builder.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                startActivity(new Intent(this, SettingActivity.class));
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            this.pictureFile = new File(image.getPath());
            User.updateProfilePhoto(this, pictureFile, new BooleanListener(){

                @Override
                public void onResult(boolean data, String error) {
                   Toast.makeText(GlideRequestManager.mContext, R.string.photo_updated, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(GlideRequestManager.mContext, R.string.update_picture_fail_error, Toast.LENGTH_LONG).show();
                }
            });
        }else{
            pictureFile = null;
        }
        loadPhoto();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
