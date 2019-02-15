package com.ft.shippo;

import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;

import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.OfflineData;
import com.orm.SugarApp;

/**
 * Created by rafael on 16/01/18.
 */

public class MyApplication extends SugarApp {

    @Override
    public void onCreate() {
        super.onCreate();
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);
        GlideRequestManager.init(this);
        OfflineData.init(this);
    }
}
