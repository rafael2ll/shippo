package com.ft.shippo.fragments;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.facebook.AccessToken;
import com.ft.shippo.R;
import com.ft.shippo.utils.GlideRequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 23/02/18.
 */

public class SignupPictureFragment extends Fragment {
    @BindView(R.id.signup_civ)
    CircleImageView imageView;

    OnClickListener onClickListener;
    File pictureFile;
    boolean fromFacebook = false;
    public SignupPictureFragment addListener(OnClickListener e){
        this.onClickListener= e;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View  v= inflater.inflate(R.layout.fragment_signup_picture, container, false);
        ButterKnifeLite.bind(this, v);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            fromFacebook = true;
            loadPhoto();
        }
        return v;
    }

    @OnClick(R.id.signup_add_picture_later)
    public void addPicLater(){
        final boolean facebookEnabled = AccessToken.getCurrentAccessToken() != null;

        AlertDialog.Builder builder  =  new AlertDialog.Builder(getActivity())
                .setItems(facebookEnabled ? R.array.picture_fb_array : R.array.picture_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            if(facebookEnabled)fromFacebook = true;
                            else pickPicture();
                            loadPhoto();
                        }else if (which == 1){
                            if(facebookEnabled){
                                fromFacebook = false;
                                pictureFile = null;
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

    @OnClick(R.id.fab)
    public void next(){
        onClickListener.onNextClick(fromFacebook ? FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()
                : pictureFile == null ? null : pictureFile.getAbsolutePath());
    }

    @OnClick(R.id.signup_add_picture_later)
    public void skip(){
        onClickListener.onAddPicLaterClick();
    }
    private void pickPicture() {
        ImagePicker.create(getActivity())
                .returnMode(ReturnMode.ALL) // set whether pick and / or camera action should return immediate result or not.
                .folderMode(true) // folder mode (false by default)
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
            GlideRequestManager.get()
                    .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                    .into(imageView);
        }else if(pictureFile != null){
            GlideRequestManager.get().load(pictureFile)
                    .into(imageView);
        }else{
            imageView.setImageResource(R.drawable.ic_boy_15);
        }
    }


    public interface OnClickListener{
        public void onAddPicLaterClick();
        public void onNextClick(String path);
    }
}
