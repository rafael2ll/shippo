package com.ft.shippo.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ft.shippo.R;
import com.ft.shippo.fragments.FullImageDialog;
import com.ft.shippo.holders.BaseHolder;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Types;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by rafael on 14/02/18.
 */

public class ChatMediaAdapter extends RecyclerView.Adapter {

    List<OfflineMessage> offlineMessages = new ArrayList<>();
    List<OfflineMessage> imageMessages = new ArrayList<>();
    private Context context;
    private View.OnClickListener imagelistener;
    private OfflineMessage clicked;

    public ChatMediaAdapter(Context ctx, List<OfflineMessage> list) {
        this.context = ctx;
        this.offlineMessages = list;
        for (OfflineMessage message : list) if (message.isPicture()) imageMessages.add(message);

        imagelistener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = 0;
                for (OfflineMessage m : imageMessages) {
                    if (Objects.equals(m.getId(), clicked.getId())) break;
                    position++;
                }
                FullImageDialog fullImageDialog = new FullImageDialog();
                fullImageDialog.setImages(imageMessages, position);
                fullImageDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "");

            }
        };
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.holder_media_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).feedWith(context, offlineMessages.get(position), false);
    }

    @Override
    public int getItemViewType(int position) {
        String type = offlineMessages.get(position).getContentType();
        return type.equals(Types.MESSSAGE_PICTURE) ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return offlineMessages.size();
    }

    public class Holder extends BaseHolder<OfflineMessage> {
        OfflineMessage message;
        @BindView(R.id.image_view)
        ImageView imageView;
        @BindView(R.id.text)
        TextView textView;
        MediaPlayer player;

        public Holder(final View itemView) {
            super(itemView);
            ButterKnifeLite.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (message.isAudio()) {
                        if (player.isPlaying()) player.pause();
                        else player.start();
                        handlePlayerState();
                    } else {
                        clicked = message;
                        imagelistener.onClick(itemView);
                    }
                }
            });
        }

        private void handlePlayerState() {
            imageView.setImageResource(player.isPlaying() ? R.drawable.ic_pause_white : R.drawable.ic_play_arrow_white);
        }

        @Override
        public void feedWith(Context ctx, OfflineMessage offlineMessage, boolean isSelected) {
            this.message = offlineMessage;
            if (offlineMessage.isPicture()) {
                textView.setVisibility(View.GONE);
                GlideRequestManager.get()
                        .load(offlineMessage.getLocalPath())
                        .placeholder(R.color.grey_300)
                        .centerCrop()
                        .into(imageView);
            } else {
                try {
                    textView.setVisibility(View.VISIBLE);
                    long duration = message.getMetadata().getLong("duration");
                    DateFormat df = new SimpleDateFormat("mm:ss", Locale.getDefault());
                    String formatted = df.format(duration);
                    textView.setText(formatted);
                    player = new MediaPlayer();
                    player.setDataSource(message.getLocalPath());
                    player.prepare();
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            imageView.setImageResource(R.drawable.ic_mic_white_96dp);
                        }
                    });
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public int layoutId() {
            return 0;
        }
    }

}
