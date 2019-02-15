package com.ft.shippo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.ft.shippo.fragments.ResetDialogFragment;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.util.Arrays;

/**
 * Created by rafael on 15/01/18.
 */

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.textInputLayoutEmail)
    TextInputLayout mEmailTIL;
    @BindView(R.id.textInputLayoutSenha)
    TextInputLayout mPassTIL;

    @BindView(R.id.imageView)
    ImageView mLogo;
    @BindView(R.id.login_main_relative_layout)
    RelativeLayout mMainLayout;

    @BindView(R.id.loading_linear_layout)
    LinearLayout mLoadingView;
    @BindView(R.id.loading_text)
    TextView mLoadingText;
    @BindView(R.id.textView)
    TextView textViewLogo;
    CallbackManager mCallbackManager;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_main_login);
        ButterKnifeLite.bind(this);
        AlphaAnimation m = new AlphaAnimation(0.2f, 1.0f);
        m.setRepeatMode(AlphaAnimation.REVERSE);
        m.setRepeatCount(4);
        mLogo.setAnimation(m);
        textViewLogo.setTypeface(Utils.getShippoTypeface(this));
        m.setDuration(1000);
        m.start();
        mCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                showContentOrLoadingIndicator(false);
            }

            @Override
            public void onError(FacebookException error) {
                showContentOrLoadingIndicator(false);
                Snackbar.make(mLoadingView, R.string.fb_login_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @OnClick(R.id.buttonLoginWithFB)
    public void doFBLogin() {
        if(AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        showContentOrLoadingIndicator(true);
        mLoadingText.setText(R.string.loging_fb);
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mLoadingText.setText(getString(R.string.fetching_data));
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            createAppUser(token);
                        } else {
                            // If sign in fails, display a message to the user.
                            showContentOrLoadingIndicator(false);
                            if(task.getException() instanceof FirebaseAuthUserCollisionException)
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setMessage(R.string.collide_account_error)
                                        .setPositiveButton(R.string.got_it, null)
                                        .setNegativeButton(R.string.forgot_my_pass, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new ResetDialogFragment().show(getSupportFragmentManager(), "");
                                            }
                                        })
                                .show();
                            else Snackbar.make(mLoadingView, R.string.fb_login_error, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @OnClick(R.id.login_create_account)
    public void onCreateClick() {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    @OnClick(R.id.activity_login_forgot_password)
    public void forgotClick(){
        new ResetDialogFragment().show(getSupportFragmentManager(), "");
    }

    private void createAppUser(final AccessToken accessToken) {
        User.findOneByField(this, "fb_id", accessToken.getUserId(), null, new ObjectListener<User>(User.class) {
            @Override
            public void onResult(User data, String error) {
                if (data != null) {
                    data.saveLocally();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class).putExtra("fb_token", accessToken.toString()));
                    finish();

                }
            }

            @Override
            public void onError(VolleyError error) {
                if (error.getMessage().contains("user does not exist")) {
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class).putExtra("fb_token", accessToken.toString()));
                    finish();
                } else {
                    showContentOrLoadingIndicator(false);
                    Snackbar.make(mLoadingView, R.string.wire_strange, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void login(final View view) {
        showContentOrLoadingIndicator(true);
        String email = mEmailTIL.getEditText().getText().toString();
        String password = mPassTIL.getEditText().getText().toString();
        if (!Utils.isAnyNull(email, password)) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password.trim())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            loadUser(authResult.getUser());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    showContentOrLoadingIndicator(false);
                    Snackbar.make(view, R.string.email_pass_error, Snackbar.LENGTH_LONG).setAction("OK", null)
                            .show();
                }
            });
        }
    }

    private void loadUser(FirebaseUser user) {
        User.login(this, user.getEmail(), mPassTIL.getEditText().getText().toString().trim(), new ObjectListener<User>(User.class) {
            @Override
            public void onResult(User data, String error) {
                if (data != null) {
                    data.saveLocally();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onError(VolleyError error) {
                showContentOrLoadingIndicator(false);
                error.printStackTrace();
                if (mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                }
                Snackbar.make(mLoadingView, R.string.email_pass_error, Snackbar.LENGTH_LONG).setAction("OK", null)
                        .show();
            }
        });
    }

    private void showContentOrLoadingIndicator(boolean isLoading) {
        // Decide which view to hide and which to show.
        final View hideView = isLoading ? mMainLayout : mLoadingView;
        final View showView = isLoading ? mLoadingView : mMainLayout;

        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);
        showView.animate()
                .alpha(1f)
                .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                .setListener(null);
        hideView.animate()
                .alpha(0f)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });
    }
}
