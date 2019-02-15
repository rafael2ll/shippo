package com.ft.shippo.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.models.OfflineChat;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.utils.GlideRequestManager;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 14/02/18.
 */

public class FullImageDialog extends DialogFragment {
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    FragmentPagerAdapter fragmentPagerAdapter;
    int position;
    List<OfflineMessage> messages = new ArrayList<>();
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            toolbar.setVisibility(View.GONE);
        }
    };

    public FullImageDialog() {
    }

    public void setImages(List<OfflineMessage> messages, int position) {
        this.messages = messages;
        this.position = position;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.darkTheme);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_chat_opened_image, container, false);
        ButterKnifeLite.bind(this, v);
        toolbar.inflateMenu(R.menu.delete);
        OfflineChat chat = OfflineChat.find(OfflineChat.class, "chatid = ?", messages.get(0).getChatId()).get(0);

        toolbar.setTitle(chat.getOtherUser().getUsername());
        toolbar.setTitleTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Medium);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Small);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                deleteImage();
                return true;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setVisibility(toolbar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                if (toolbar.getVisibility() == View.VISIBLE) handler.postDelayed(runnable, 3000);
                else handler.removeCallbacks(runnable);
            }
        };
        fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new ImageFragment().setImage(messages.get(position), onClickListener);
            }

            @Override
            public int getCount() {
                return messages.size();
            }
        };
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setSubtitle(messages.get(position).getLocalCreatedAt().toString(getActivity().getString(R.string.date_with_day)));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setCurrentItem(position);
        return v;
    }

    private void deleteImage() {
        OfflineMessage message = messages.get(viewPager.getCurrentItem());
        File image = new File(message.getLocalPath());
        if (image.exists()) image.delete();
        messages.remove(message);
        message.delete();
        fragmentPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1 >= messages.size() ? 0 : viewPager.getCurrentItem() + 1);
    }

    public static class ImageFragment extends Fragment {

        OfflineMessage message;
        ImageView view;
        View.OnClickListener listener;
        TextView textUnavaliable;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.holder_full_image, container, false);
            ButterKnifeLite.bind(this, v);
            view = v.findViewById(R.id.image_view);
            textUnavaliable = v.findViewById(R.id.text);
            if (message != null) {
                view.setVisibility(View.VISIBLE);
                textUnavaliable.setVisibility(View.GONE);
                File f = new File(message.getLocalPath());
                if (f.exists()) GlideRequestManager.get()
                        .load(f).into(view);
                else {
                    view.setVisibility(View.GONE);
                    textUnavaliable.setVisibility(View.VISIBLE);
                }
            }
            return v;
        }

        public ImageFragment setImage(OfflineMessage message, View.OnClickListener listener) {
            this.listener = listener;
            this.message = message;
            if (view != null) {
                view.setVisibility(View.VISIBLE);
                textUnavaliable.setVisibility(View.GONE);
                File f = new File(message.getLocalPath());
                if (f.exists()) GlideRequestManager.get()
                        .load(f).into(view);
                else {
                    view.setVisibility(View.GONE);
                    textUnavaliable.setVisibility(View.VISIBLE);
                }
            }
            return this;
        }

        @OnClick(R.id.image_view)
        public void onClick() {
            listener.onClick(view);
        }
    }
}
