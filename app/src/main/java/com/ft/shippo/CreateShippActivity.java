package com.ft.shippo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.text.emoji.widget.EmojiAppCompatEditText;
import android.support.text.emoji.widget.EmojiAppCompatTextView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.error.VolleyError;
import com.ft.shippo.fragments.SearchUserDialog;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.Shipp;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Types;
import com.ft.shippo.utils.Utils;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 26/01/18.
 */

public class CreateShippActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.new_shipp_tiet_message)
    EmojiAppCompatEditText mText;
    @BindView(R.id.new_shipp_mactv_tags)
    MultiAutoCompleteTextView mTags;
    @BindView(R.id.new_shipp_loading_view)
    View loadingView;
    @BindView(R.id.shipp_success_view)
    View successView;
    @BindView(R.id.new_shipp_creating_subtitle)
    EmojiAppCompatTextView subtitleCreating;
    @BindView(R.id.header_view)
    View headerViews;
    @BindView(R.id.shipp_success_view_buttons)
    View successButtons;

    @BindView(R.id.im_luck_image)
    ImageView imageViewImLucky;
    @BindView(R.id.im_luck_loading)
    ProgressBar progressBarImLucky;
    @BindView(R.id.im_luck_text)
    TextView textViewImLucky;
    @BindView(R.id.im_lucky_view)
    View viewImLucky;

    @BindView(R.id.new_shipp_tv_p1_name)
    private EmojiAppCompatTextView mUser1name;
    @BindView(R.id.new_shipp_tv_p1_age)
    private TextView mUser1age;
    @BindView(R.id.new_shipp_civ_p1)
    private CircleImageView mUser1pic;
    @BindView(R.id.new_shipp_tv_p1_country)
    private TextView mUser1Country;
    @BindView(R.id.new_shipp_ll_user1)
    LinearLayout leftCard;
    @BindView(R.id.new_shipp_add_user1)
    Button addUser1;

    @BindView(R.id.new_shipp_tv_p2_name)
    private EmojiAppCompatTextView mUser2name;
    @BindView(R.id.new_shipp_tv_p2_age)
    private TextView mUser2age;
    @BindView(R.id.new_shipp_civ_p2)
    private CircleImageView mUser2pic;
    @BindView(R.id.new_shipp_tv_p2_country)
    private TextView mUser2Country;
    @BindView(R.id.new_shipp_ll_user2)
    LinearLayout rightCard;
    private Shipp shipp;
    @BindView(R.id.new_shipp_add_user2)
    Button addUser2;

    @BindView(R.id.bubble)
    private ImageView bubble1;
    @BindView(R.id.bubble2)
    private ImageView bubble2;
    @BindView(R.id.shipp_bubble1_helper)
    ImageView bubbleHelper1;
    @BindView(R.id.shipp_bubble2_helper)
    ImageView bubbleHelper2;

    private static final int bubbleThemes[] = new int[]{R.style.BlueBubble, R.style.RedBubble};//, R.style.GreenBubble, R.style.PurpleBubble,R.style.PinkBubble, R.style.CianBubble, R.style.BlueBubble,R.style.BrownBubble, R.style.BlackBubble, R.style.OrangeBubble};
    final Random random = new Random();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_shipp);
        ButterKnifeLite.bind(this);
        setSupportActionBar(mToolbar);
        shipp = new Shipp();
        shipp.setOwner(User.getCurrentUser());
        mUser1pic.setImageResource(R.drawable.ic_circled_plus_blue);
        mUser2pic.setImageResource(R.drawable.ic_circled_plus_red);
        mText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                shipp.setLabel(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (shipp.getOwner() != null && shipp.getUser2() != null && shipp.getUser2() != null) {
            showLoadView();
            shipp.save(this, new BooleanListener() {

                @Override
                public void onResult(boolean data, String error) {
                    if(data) {
                        hideLoadView();
                        showSuccessView();
                    }else {
                        hideLoadView();
                        showHeaderViews();
                        Toast.makeText(CreateShippActivity.this, R.string.error_try_again, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    hideLoadView();
                    showHeaderViews();
                    Toast.makeText(CreateShippActivity.this, R.string.error_try_again, Toast.LENGTH_LONG).show();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHeaderViews() {
        headerViews.setVisibility(View.VISIBLE);
        headerViews.animate()
                .setDuration(200)
                .setListener(null)
                .translationY(headerViews.getHeight()).start();
        subtitleCreating.setVisibility(View.GONE);
        imageViewImLucky.setVisibility(View.VISIBLE);
    }

    private void showSuccessView() {
        successView.setVisibility(View.VISIBLE);
        successView.setAlpha(0.0f);
        successView.animate()
                .translationY(successView.getHeight())
                .setDuration(250)
                .alpha(1.0f).start();

        successButtons.setVisibility(View.VISIBLE);
        successButtons.setAlpha(0);
        successButtons.animate()
                .translationY(successButtons.getHeight())
                .setListener(null)
                .setDuration(250)
                .alpha(1.0f).start();
    }

    private void hideLoadView() {
        int shortAnim = 200;
        loadingView.animate()
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loadingView.setVisibility(View.GONE);
                    }
                }).setDuration(shortAnim).start();

    }

    private void showLoadView() {
        viewImLucky.setVisibility(View.GONE);
        headerViews.animate()
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        headerViews.setVisibility(View.GONE);
                    }
                }).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime)).start();

        loadingView.setVisibility(View.VISIBLE);
        loadingView.setAlpha(0.0f);
        loadingView.animate()
                .translationY(loadingView.getHeight())
                .setDuration(200)
                .alpha(1.0f)
                .setListener(null).start();
        subtitleCreating.setText(shipp.getLabel());
        subtitleCreating.setVisibility(View.VISIBLE);

        subtitleCreating.setAlpha(0);
        subtitleCreating.animate()
                .translationY(subtitleCreating.getHeight())
                .setDuration(200)
                .alpha(1.0f).start();
    }


    @OnClick(R.id.im_luck_button)
    public void imLuckyClick(){
        findViewById(R.id.im_luck_button).setClickable(false);
        progressBarImLucky.setVisibility(View.VISIBLE);
        imageViewImLucky.setVisibility(View.GONE);
        User.imLucky(this, new ListObjectListener<User>(Types.LIST_USER){
            @Override
            public void onResult(List<User> data, String error) {
                if(data.size() > 0){
                    shipp.setUser1(data.get(0));
                    addUser1.setVisibility(View.GONE);
                    loadUser1();
                    if(data.size()> 1){
                        shipp.setUser2(data.get(1));
                        addUser2.setVisibility(View.GONE);
                        loadUser2();
                    }
                }
                stopImLucky();
            }

            @Override
            public void onError(VolleyError error) {
                stopImLucky();
            }
        });
    }

    private void stopImLucky() {
        progressBarImLucky.setVisibility(View.GONE);
        imageViewImLucky.setVisibility(View.VISIBLE);
        findViewById(R.id.im_luck_button).setClickable(true);
    }

    @OnClick(R.id.new_shipp_add_user1)
    public void onButton1Click() {
        onUser1Click();
    }

    @OnClick(R.id.new_shipp_ll_user1)
    public void onUser1Click() {
        mUser2pic.requestFocus();
        new SearchUserDialog().addListener(new SearchUserDialog.OnUserSelectedListener() {
            @Override
            public void onUserSelected(User user) {
                shipp.setUser1(user);
                if (addUser1.getVisibility() != View.GONE)
                    addUser1.setVisibility(View.GONE);
                loadUser1();
            }

            @Override
            public void onCancel() {

            }
        }).show(getSupportFragmentManager(), "");
    }

    @OnClick(R.id.new_shipp_add_user2)
    public void onButton2Click() {
        onUser2Click();
    }

    @OnClick(R.id.new_shipp_ll_user2)
    public void onUser2Click() {
        mUser2pic.requestFocus();
        new SearchUserDialog().addListener(new SearchUserDialog.OnUserSelectedListener() {
            @Override
            public void onUserSelected(User user) {
                shipp.setUser2(user);
                if (addUser2.getVisibility() != View.GONE)
                    addUser2.setVisibility(View.GONE);

                loadUser2();
            }

            @Override
            public void onCancel() {

            }
        }).show(getSupportFragmentManager(), "");
    }

    @OnClick(R.id.new_shipp_return_button)
    public void returnButton(){
        finish();
    }
    @OnClick(R.id.new_shipp_new_shipp_button)
    public void newShippButton(){
        startActivity(new Intent(this, CreateShippActivity.class));
        finish();
    }
    private void loadUser1() {
        int user1age = shipp.getUser1().getAge();

        mUser1name.setText(shipp.getUser1().getUsername());
        mUser1age.setText(getString(R.string.years, user1age));
        mUser1Country.setText(shipp.getUser1().getCityName());
        GlideRequestManager.get()
                .load(shipp.getUser1().getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .into(mUser1pic);
        final ContextThemeWrapper wrapper1 = new ContextThemeWrapper(this, bubbleThemes[shipp.getUser1().isMan() ? 0 : 1]);
        final Drawable bubble1Drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_bubble, wrapper1.getTheme());


        bubble1.setImageDrawable(bubble1Drawable);
        int color1 = Utils.getAttributeColor(this, wrapper1.getTheme(), R.attr.bubble_color);

        leftCard.setBackground(Utils.leftShippDrawable(color1));
        bubbleHelper1.setImageDrawable(new ColorDrawable(color1));
    }

    public void loadUser2() {
        int user2age = shipp.getUser2().getAge();

        mUser2name.setText(shipp.getUser2().getUsername());
        mUser2age.setText(getString(R.string.years, user2age));
        mUser2Country.setText(shipp.getUser2().getCityName());
        GlideRequestManager.get()
                .load(shipp.getUser2().getPhotoUri())
                .placeholder(R.drawable.ic_default_avatar)
                .into(mUser2pic);

        final ContextThemeWrapper wrapper2 = new ContextThemeWrapper(this, bubbleThemes[shipp.getUser2().isMan() ? 0 : 1]);
        final Drawable bubble2Drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_bubble, wrapper2.getTheme());
        bubble2.setImageDrawable(bubble2Drawable);
        int color2 = Utils.getAttributeColor(this, wrapper2.getTheme(), R.attr.bubble_color);
        bubbleHelper2.setImageDrawable(new ColorDrawable(color2));
        rightCard.setBackground(Utils.rightShippDrawable(color2));
    }
}