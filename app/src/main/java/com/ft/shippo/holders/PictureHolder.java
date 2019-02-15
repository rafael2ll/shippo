package com.ft.shippo.holders;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.error.VolleyError;
import com.ft.shippo.R;
import com.ft.shippo.SendMessageService;
import com.ft.shippo.fragments.FullImageDialog;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.utils.DownloadListener;
import com.ft.shippo.utils.GlideRequestManager;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;

import static android.text.format.Formatter.formatFileSize;

/**
 * Created by rafael on 14/02/18.
 */

public class PictureHolder extends BaseHolder<OfflineMessage> {
    @BindView(R.id.holder_message_image_iv_image)
    ImageView imageViewImage;
    @BindView(R.id.holder_message_image_iv_send_state)
    ImageView imageViewSendState;
    @BindView(R.id.holder_message_image_tv_sent)
    TextView textViewSentDate;
    @BindView(R.id.holder_message_image_upload_content)
    View uploadView;
    @BindView(R.id.holder_message_image_upload_iv)
    ImageView uploadImage;
    @BindView(R.id.holder_message_image_upload_size)
    TextView textViewSize;
    @BindView(R.id.holder_message_image_progress)
    ProgressBar progressBar;
    @BindView(R.id.holder_message_image_selected)
    ImageView imageViewSelected;

    private OfflineMessage offlineMessage;
    private Context ctx;

    public PictureHolder(View itemView) {
        super(itemView);
        ButterKnifeLite.bind(this, itemView);
    }

    @Override
    public void feedWith(Context ctx, OfflineMessage message, boolean isSelected) {
        this.ctx = ctx;
        this.offlineMessage = message;
        handleUploadDownloadViews(false);
        ((LinearLayout) itemView).setGravity(message.isMine() ? Gravity.END : Gravity.START);
        if (message.isMine()) {
            imageViewSendState.setVisibility(View.VISIBLE);
            imageViewSendState.setImageResource(isSelected ? R.drawable.ic_cancel_accent : message.getIsSent() ? (message.otherSaw() ? R.drawable.ic_check_circle_green : R.drawable.ic_check_circle_dark) : R.drawable.ic_watch_later);
        } else {
            imageViewSendState.setVisibility(View.GONE);
            if (isSelected) imageViewSelected.setVisibility(View.VISIBLE);
            else imageViewSelected.setVisibility(View.GONE);
        }

        imageViewImage.setImageResource(R.color.grey_500);
        GlideRequestManager.get()
                .load(offlineMessage.getLocalPath())
                .placeholder(R.color.grey_500)
                .centerCrop()
                .into(imageViewImage);
    }

    private void handleUploadDownloadViews(boolean showDownloadUpload) {
        if (offlineMessage.isMine()) {
            uploadImage.setImageResource(R.drawable.ic_file_upload_white);
            if (showDownloadUpload || offlineMessage.isSending()) {
                uploadView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);

                textViewSize.setVisibility(View.GONE);
                uploadImage.setVisibility(View.GONE);

            } else if (offlineMessage.getIsSent()) {
                uploadView.setVisibility(View.GONE);
            } else {
                uploadView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                imageViewImage.setVisibility(View.VISIBLE);
                textViewSize.setVisibility(View.VISIBLE);
                try {
                    textViewSize.setText(formatFileSize(ctx, offlineMessage.getMetadata().getLong("size")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            uploadImage.setImageResource(R.drawable.ic_file_download_white);
            if (showDownloadUpload) {
                uploadView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                progressBar.setIndeterminate(true);
                textViewSize.setVisibility(View.GONE);
                uploadImage.setVisibility(View.GONE);
            } else if (offlineMessage.isDownloaded()) {
                uploadView.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                imageViewImage.setVisibility(View.VISIBLE);
                textViewSize.setVisibility(View.VISIBLE);
                try {
                    textViewSize.setText(formatFileSize(ctx, offlineMessage.getMetadata().getLong("size")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnClick(R.id.holder_message_image_upload_iv)
    public void downloadOrUpload() {
        if (offlineMessage.isMine()) {
            if (!offlineMessage.isSending())
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent(SendMessageService.NEW_MESSAGE_TO_SEND));
        } else {
            if(ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            SendMessageService.downloadImage(ctx, offlineMessage.getId(), new DownloadListener() {
                @Override
                public void onResponse(String response) {
                    offlineMessage.setDownloaded(true);
                    offlineMessage.setLocalPath(response);
                    offlineMessage.save();
                    GlideRequestManager.get()
                            .load(offlineMessage.getLocalPath())
                            .placeholder(R.color.grey_500)
                            .centerCrop()
                            .into(imageViewImage);
                    handleUploadDownloadViews(false);
                }

                @Override
                public void onProgress(long transferredBytes, long totalSize) {
                    progressBar.setIndeterminate(false);
                    progressBar.setMax((int) totalSize);
                    progressBar.setProgress((int) transferredBytes);
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.INVISIBLE);
                    handleUploadDownloadViews(false);
                }
            });
            else ActivityCompat.requestPermissions((FragmentActivity)ctx, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        handleUploadDownloadViews(true);
    }

    @OnClick(R.id.holder_message_image_iv_image)
    public void onImageClick() {
        int position = 0;
        if(!offlineMessage.isDownloaded()){
            downloadOrUpload();
            return;
        }
        List<OfflineMessage> messageList = OfflineMessage.find(OfflineMessage.class, "contenttype = ? and chatid = ?", "PICTURE", offlineMessage.getChatId());
        for (OfflineMessage m : messageList) {
            if (Objects.equals(m.getId(), offlineMessage.getId())) break;
            position++;
        }
        FullImageDialog fullImageDialog = new FullImageDialog();
        fullImageDialog.setImages(messageList, position);
        fullImageDialog.show(((FragmentActivity) ctx).getSupportFragmentManager(), "");

    }

    @Override
    public int layoutId() {
        return R.layout.holder_message_image;
    }
}
