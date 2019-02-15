package com.ft.shippo.utils;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by rafael on 11/06/17.
 */

@GlideModule()
public class MyGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Apply options to the builder here.
        builder.setDefaultRequestOptions(new RequestOptions().encodeQuality(100));
    }

    public boolean genderifestParsingEnabled() {
        return false;
    }
}
