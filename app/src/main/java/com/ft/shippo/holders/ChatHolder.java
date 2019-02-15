package com.ft.shippo.holders;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.text.emoji.widget.EmojiAppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ft.shippo.MessageActivity;
import com.ft.shippo.R;
import com.ft.shippo.models.OfflineChat;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rafael on 06/02/18.
 */

public class ChatHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.chat_holder_civ_pic)
    CircleImageView civProfilePic;
    @BindView(R.id.chat_holder_tv_last_message)
    EmojiAppCompatTextView textViewLastMessage;
    @BindView(R.id.chat_holder_tv_username)
    EmojiAppCompatTextView textViewUsername;
    @BindView(R.id.chat_holder_tv_unread_count)
    TextView textViewUnreadCount;
    @BindView(R.id.chat_holder_tv_last_message_time)
    TextView textViewLastMessageTime;
    @BindView(R.id.chat_holder_iv_content_type)
    ImageView imageViewContentType;
    private Context context;
    private OfflineChat offlineChat;

    public ChatHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    public void feedWith(final Context context, OfflineChat model) {
        this.context = context;
        this.offlineChat = model;

        fillLayout();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MessageActivity.class).putExtra("chat", offlineChat.toJson()));
            }
        });
    }

    private void fillLayout() {
        OfflineMessage lastMessage = offlineChat.getLastMessage();
        long unreadCount = OfflineMessage.count(OfflineMessage.class, "chatid = ? and i_saw =? and userid <> ?", new String[]{offlineChat.getChatId(), "0", User.getCurrentUser().getId()});
        List<User> users = offlineChat.getUsers();
        User user;
        try {
            if (users.get(0).getId().equals(User.getCurrentUser().getId())) user = users.get(1);
            else user = users.get(0);

            if (unreadCount > 0) {
                textViewUnreadCount.setVisibility(View.VISIBLE);
                textViewUnreadCount.setText("" + unreadCount);
                Resources resources = context.getResources();
                Resources.Theme theme = context.getTheme();
                Drawable drawable = VectorDrawableCompat.create(resources, R.drawable.ic_new, theme);
                textViewUnreadCount.setBackground(drawable);
            } else {
                textViewUnreadCount.setVisibility(View.GONE);
            }
            textViewUsername.setText(user.getUsername());
            if (lastMessage != null) {
                textViewLastMessage.setVisibility(View.VISIBLE);
                textViewLastMessage.setText(lastMessage.isText() ? lastMessage.getBody() :
                        lastMessage.isAudio() ? context.getString(R.string.audio_simple, new SimpleDateFormat("mm:ss", Locale.getDefault()).format(lastMessage.getMetadata().getLong("duration")))
                                : context.getString(R.string.picture));
                if (lastMessage.isMine()) {
                    imageViewContentType.setImageResource(lastMessage.getIsSent() ? (lastMessage.otherSaw() ? R.drawable.ic_check_circle_green : R.drawable.ic_check_circle_dark) : R.drawable.ic_watch_later);
                } else {

                    if (lastMessage.isText()) imageViewContentType.setVisibility(View.GONE);
                    else {
                        imageViewContentType.setVisibility(View.VISIBLE);
                        imageViewContentType.setImageResource(lastMessage.isAudio() ? R.drawable.ic_headset_darker : R.drawable.ic_photo_dark);
                    }
                }
                LocalDate message_date = new DateTime(lastMessage.getCreatedAt()).toLocalDate();
                if (message_date.equals(LocalDate.now())) {
                    textViewLastMessageTime.setText(lastMessage.getLocalCreatedAt().toString("HH:mm"));
                } else if (message_date.equals(LocalDate.now().minusDays(1))) {
                    textViewLastMessageTime.setText(R.string.yesterday);
                } else if (message_date.isAfter(LocalDate.now().minusDays(7))) {
                    textViewLastMessageTime.setText(message_date.toString("EEE", Locale.getDefault()));
                } else {
                    textViewLastMessageTime.setText(message_date.toString("dd/MM", Locale.getDefault()));
                }
            } else {
                textViewLastMessage.setVisibility(View.GONE);
                LocalDate message_date = new DateTime(offlineChat.getCreatedAt()).toLocalDate();
                if (message_date.equals(LocalDate.now())) {
                    textViewLastMessageTime.setText(message_date.toString("HH:mm"));
                } else if (message_date.equals(LocalDate.now().minusDays(1))) {
                    textViewLastMessageTime.setText(R.string.yesterday);
                } else if (message_date.isAfter(LocalDate.now().minusDays(7))) {
                    textViewLastMessageTime.setText(message_date.toString("EEE", Locale.getDefault()));
                } else {
                    textViewLastMessageTime.setText(message_date.toString("dd/MM", Locale.getDefault()));
                }

            }
            GlideRequestManager.get()
                    .asBitmap().load(user.getPhotoUri())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(civProfilePic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
