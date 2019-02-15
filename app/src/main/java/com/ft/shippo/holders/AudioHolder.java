package com.ft.shippo.holders;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.SendMessageService;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.utils.DownloadListener;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by rafael on 12/02/18.
 */

public class AudioHolder extends BaseHolder<OfflineMessage> implements Runnable {
    @BindView(R.id.holder_audio_pb_download)
    ProgressBar progressBarDownload;
    @BindView(R.id.holder_audio_seekbar_time)
    SeekBar seekBarTime;
    @BindView(R.id.holder_audio_iv_play)
    ImageView imageViewPlay;
    @BindView(R.id.holder_audio_tv_sent)
    TextView textViewSentDate;
    @BindView(R.id.holder_audio_tv_time)
    TextView textViewPlayedTime;
    @BindView(R.id.holder_audio_iv_send_state)
    ImageView sendState;
    @BindView(R.id.holder_audio_iv_selected)
    ImageView imageViewSelected;
    Handler handler = new Handler();
    private Context ctx;
    private OfflineMessage message;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private long playingAt;

    public AudioHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    @Override
    public void feedWith(Context ctx, OfflineMessage o, boolean isSelected) {
        this.ctx = ctx;
        this.message = o;
        textViewSentDate.setText(message.getLocalCreatedAt().toString("HH:mm"));
        handleDownloadState();
        ((LinearLayout) itemView).setGravity(message.isMine() ? Gravity.END : Gravity.START);
        if (message.isMine()) {
            sendState.setVisibility(View.VISIBLE);
            sendState.setImageResource(isSelected ? R.drawable.ic_cancel_accent : message.getIsSent() ? (message.otherSaw() ? R.drawable.ic_check_circle_green : R.drawable.ic_check_circle_dark) : R.drawable.ic_watch_later);
        } else {
            sendState.setVisibility(View.GONE);
            if (isSelected) imageViewSelected.setVisibility(View.VISIBLE);
            else imageViewSelected.setVisibility(View.GONE);
        }
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isPlaying) mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void handleDownloadState() {
        imageViewPlay.setImageResource((message.isMine()) ? (!message.getIsSent() ? R.drawable.ic_file_upload_white : R.drawable.ic_play_arrow_white) : message.isDownloaded() ? R.drawable.ic_play_arrow_white : R.drawable.ic_file_download_white);
        if (isPlaying) imageViewPlay.setImageResource(R.drawable.ic_pause_white);
        if (message.isSending()) {
            imageViewPlay.setVisibility(View.GONE);
            progressBarDownload.setVisibility(View.VISIBLE);
        } else {
            progressBarDownload.setVisibility(View.GONE);
            imageViewPlay.setVisibility(View.VISIBLE);
        }
        if (message.isDownloaded() && !isPlaying) {
            try {
                long duration = message.getMetadata().getLong("duration");
                DateFormat df = new SimpleDateFormat("mm:ss", Locale.getDefault());
                String formatted = df.format(duration);
                textViewPlayedTime.setText(formatted);
                seekBarTime.setMax((int) duration);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.holder_audio_iv_play)
    public void play() {
        if (isPlaying && mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                handleProgress(false);
                imageViewPlay.setImageResource(R.drawable.ic_play_arrow_white);
            } else {
                mediaPlayer.start();
                handleProgress(true);
            }
        } else if (message.isMine()) {

            if (message.getIsSent())
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(ctx, Uri.parse(message.getLocalPath()));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    isPlaying = true;
                    handleProgress(true);
                    handleDownloadState();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isPlaying = false;
                            playingAt = 0;
                            handleProgress(false);
                            mediaPlayer.release();
                            handleDownloadState();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ctx, R.string.audio_not_avaliable, Toast.LENGTH_SHORT).show();
                }
            else {
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent(SendMessageService.NEW_MESSAGE_TO_SEND));
            }
        } else {
            progressBarDownload.setVisibility(View.VISIBLE);
            imageViewPlay.setVisibility(View.INVISIBLE);
            progressBarDownload.setIndeterminate(true);
            SendMessageService.downloadAudio(ctx, message.getId(), new DownloadListener() {
                @Override
                public void onResponse(String response) {
                    message.setDownloaded(true);
                    message.setLocalPath(response);
                    message.save();
                    progressBarDownload.setVisibility(View.INVISIBLE);
                    imageViewPlay.setVisibility(View.VISIBLE);
                    handleDownloadState();
                }

                @Override
                public void onProgress(long transferredBytes, long totalSize) {
                    progressBarDownload.setIndeterminate(false);
                    progressBarDownload.setMax((int) totalSize);
                    progressBarDownload.setProgress((int) transferredBytes);
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBarDownload.setVisibility(View.INVISIBLE);
                    imageViewPlay.setVisibility(View.VISIBLE);
                    handleDownloadState();
                }
            });
        }
    }

    private void handleProgress(boolean b) {
        if (b) {
            if (mediaPlayer != null) seekBarTime.setMax(mediaPlayer.getDuration());
            handler.postDelayed(this, 100);
        } else {
            handler.removeCallbacks(this);
        }
    }

    @Override
    public void run() {
        DateFormat df = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String formatted = df.format(mediaPlayer.getCurrentPosition());
        textViewPlayedTime.setText(formatted);
        seekBarTime.setProgress(mediaPlayer.getCurrentPosition());
        handler.postDelayed(this, 100);
    }

    @Override
    public int layoutId() {
        return R.layout.holder_audio;
    }
}
