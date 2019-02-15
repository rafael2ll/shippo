package com.ft.shippo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.ft.shippo.fragments.AboutDialog;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_settings_iv_private)
    ImageView imageViewPrivateAccount;
    @BindView(R.id.activity_settings_sw_private)
    Switch switchPrivate;

    @BindView(R.id.acitivity_settings_fb_ll)
    View connectedFBView;
    @BindView(R.id.acitivity_settings_login_fb_ll)
    View toConnectFBView;
    @BindView(R.id.acitivity_settings_fb_civ)
    CircleImageView civFbPic;
    @BindView(R.id.acitivity_settings_fb_name)
    TextView fbTextViewName;
    @BindView(R.id.activity_settings_fb_connect_tv)
    TextView fbConnectTextView;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnifeLite.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switchPrivate.setChecked(User.getCurrentUser().isPrivate());
        imageViewPrivateAccount.setImageResource(User.getCurrentUser().isPrivate() ?
                R.drawable.ic_lock_darker : R.drawable.ic_lock_open_darker);

        switchPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                User.getCurrentUser().updatePrivateAccount(SettingActivity.this, isChecked);
                imageViewPrivateAccount.setImageResource(isChecked ? R.drawable.ic_lock_darker : R.drawable.ic_lock_open_darker);
            }
        });
        mCallbackManager = CallbackManager.Factory.create();
        handleFBLogin();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel(){
                handleFBLogin();
            }

            @Override
            public void onError(FacebookException error) {
                if(AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();
               handleFBLogin();
                Snackbar.make(connectedFBView, R.string.fb_login_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void handleFBLogin() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getProviders().contains(FacebookAuthProvider.PROVIDER_ID)) {
            connectedFBView.setVisibility(View.VISIBLE);
            toConnectFBView.setVisibility(View.GONE);
            String photoUrl = "https://graph.facebook.com/" + User.getCurrentUser().getFbId() + "/picture?height=500";
            GlideRequestManager.get().load(photoUrl).placeholder(R.drawable.ic_default_avatar).into(civFbPic);
            fbTextViewName.setText(getString(R.string.connected_as, firebaseUser.getDisplayName()));
        } else {
            connectedFBView.setVisibility(View.GONE);
            fbConnectTextView.setText(R.string.connect_fb);
            toConnectFBView.setVisibility(View.VISIBLE);
        }

    }


    @OnClick(R.id.activity_settings_fb_connect_tv)
    public void connectFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        fbConnectTextView.setText(R.string.loging_fb);
    }
    @OnClick(R.id.acitivity_settings_fb_disconnect)
    public void  disconnectFacebook(){
        User.unlinkWithFacebook(this, new BooleanListener(){
            @Override
            public void onResult(boolean data, String error) {
                if(AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().getCurrentUser().unlink(FacebookAuthProvider.PROVIDER_ID);
                handleFBLogin();
            }

            @Override
            public void onError(VolleyError error) {
                    Snackbar.make(connectedFBView, R.string.error_try_again, Snackbar.LENGTH_LONG).show();
            }
        });
    }
    public void handleAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        fbConnectTextView.setText(getString(R.string.fetching_data));
        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(Profile.getCurrentProfile().getName()).setPhotoUri(Uri.parse( "https://graph.facebook.com/" + token.getUserId() + "/picture?height=500")).build();
                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates);
                        handleFBLogin();
                        if (task.isSuccessful()) {
                            handleFBLogin();
                            User.linkWithFacebook(SettingActivity.this, AccessToken.getCurrentAccessToken().getUserId(), AccessToken.getCurrentAccessToken(), new BooleanListener(){
                                @Override
                                public void onResult(boolean data, String error) {
                                }

                                @Override
                                public void onError(VolleyError error) {
                                    Snackbar.make(toConnectFBView, R.string.fb_login_error, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Snackbar.make(toConnectFBView, R.string.fb_login_error, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @OnClick(R.id.activity_settings_view_about)
    public void aboutClick() {
        new AboutDialog().setType(2).show(getSupportFragmentManager(), "");
    }

    @OnClick(R.id.activity_settings_view_logoff)
    public void logOffClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.log_off)
                .setMessage(R.string.sure_logoff)
                .setPositiveButton(R.string.log_off, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                User.logOff();
                                Utils.clearApplicationData(SettingActivity.this);
                                Utils.triggerRebirth(SettingActivity.this);
                            }
                        }
                ).setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @OnClick(R.id.activity_settings_view_support)
    public void support() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"shippo_support@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick(R.id.activity_settings_view_delete_acccount)
    public void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete)
                .setPositiveButton(R.string.log_off, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                User.deleteAccount(SettingActivity.this, "", new BooleanListener() {
                                    @Override
                                    public void onResult(boolean data, String error) {
                                        User.logOff();
                                        Utils.clearApplicationData(SettingActivity.this);
                                        Utils.triggerRebirth(SettingActivity.this);
                                    }

                                    @Override
                                    public void onError(VolleyError error) {

                                    }
                                });
                            }
                        }
                ).setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @OnClick(R.id.activity_settings_view_tos)
    public void tosClick() {
        new AboutDialog().setType(1).show(getSupportFragmentManager(), "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
