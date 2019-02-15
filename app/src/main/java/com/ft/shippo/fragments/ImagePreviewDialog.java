package com.ft.shippo.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.esafirm.imagepicker.model.Image;
import com.ft.shippo.R;
import com.ft.shippo.utils.GlideRequestManager;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 14/02/18.
 */

public class ImagePreviewDialog extends DialogFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    List<Image> imageList = new ArrayList<>();
    ImagePreviewAdapter adapter;
    OnSendListener onSendListener;

    public ImagePreviewDialog() {
        super();
    }

    public void setImageList(List<Image> imageList, OnSendListener listener) {
        this.onSendListener = listener;
        this.imageList = imageList;


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.preview_image_dialog, container, false);
        ButterKnifeLite.bind(this, v);
        adapter = new ImagePreviewAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        toolbar.inflateMenu(R.menu.save);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onSendListener.send(imageList);
                dismiss();
                return true;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }

    public interface OnSendListener {
        public void send(List<Image> imageList);
    }

    public static class PreviewImageFragment extends Fragment {
        @BindView(R.id.image_view)
        ImageView imageView;
        Image e;
        onRemove onRemove;

        @OnClick(R.id.holder_image_iv_remove)
        public void remove() {
            onRemove.onRemove();
        }

        public PreviewImageFragment addImage(Image e, onRemove onRemove) {
            this.e = e;
            this.onRemove = onRemove;
            if (imageView != null)
                GlideRequestManager.get().asBitmap().
                        load(e.getPath()).centerCrop()
                        .into(imageView);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.holder_image_preview, container, false);
            ButterKnifeLite.bind(this, v);
            if (e != null)
                GlideRequestManager.get().asBitmap().
                        load(e.getPath()).centerCrop()
                        .into(imageView);
            return v;
        }

        public interface onRemove {
            public void onRemove();
        }
    }

    public class ImagePreviewAdapter extends FragmentStatePagerAdapter {

        public ImagePreviewAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new PreviewImageFragment().addImage(imageList.get(position), new PreviewImageFragment.onRemove() {
                @Override
                public void onRemove() {
                    imageList.remove(viewPager.getCurrentItem());
                    adapter.notifyDataSetChanged();
                    if (adapter.getCount() == 0) dismiss();
                }
            });
        }

        @Override
        public int getCount() {
            return imageList.size();
        }
    }
}
