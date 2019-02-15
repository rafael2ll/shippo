package com.ft.shippo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.error.VolleyError;
import com.ft.shippo.models.Message;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.utils.Requester;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.DownloadListener;
import com.ft.shippo.utils.Types;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rafael on 09/02/18.
 */

public class SendMessageService extends Service {

    public static final String NEW_MESSAGE_TO_SEND = "com.ft.shippo.SendMessageService.NEW_MESSAGE_TO_SEND";
    boolean isChecked = false;
    private List<OfflineMessage> offlineMessages = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NEW_MESSAGE_TO_SEND:
                    updateToSend();
                    break;
            }
        }
    };

    public static void downloadAudio(Context ctx, long id, DownloadListener listener) {
        OfflineMessage message = OfflineMessage.findById(OfflineMessage.class, id);
        String path = ctx.getExternalCacheDir().getAbsolutePath() + "/AUDIO_" + System.currentTimeMillis() + ".3gp";
        Requester.downloader(ctx, message.getBody(), path, listener);
    }

    public static void downloadImage(Context ctx, long id, DownloadListener listener) {
        OfflineMessage message = OfflineMessage.findById(OfflineMessage.class, id);
        String path = ctx.getExternalCacheDir().getAbsolutePath() + "/IMAGE_" + System.currentTimeMillis() + ".jpg";
        Requester.downloader(ctx, message.getBody(), path, listener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NEW_MESSAGE_TO_SEND);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
        trySendAll();
        isChecked = true;
        return START_NOT_STICKY;
    }

    private synchronized void trySendAll() {
        try {
            final List<OfflineMessage> off = OfflineMessage.find(OfflineMessage.class, "userid = ? and is_sent = ?", User.getCurrentUser().getId(), "0");
            for (OfflineMessage e : off) {
                if (!e.isSending() && !offlineMessages.contains(e)) offlineMessages.add(e);
            }
            Log.d(getClass().getName(), offlineMessages.size() + "");
            for (final OfflineMessage message : offlineMessages) {
                if (message.isSending()) return;
                ObjectListener<Message> listener = new ObjectListener<Message>(Message.class) {
                    @Override
                    public void onResult(Message data, String error) {
                        Log.d("imagemessage", data.toString());
                        offlineMessages.remove(message);
                        message.update(data);
                        message.setSending(false);
                        message.isSent(true);
                        message.save();
                        LocalBroadcastManager.getInstance(SendMessageService.this).sendBroadcast(new Intent(MessageActivity.UPDATE_MESSAGES));
                    }

                    @Override
                    public void onError(VolleyError error) {
                        error.printStackTrace();
                        message.setSending(false);
                        message.isSent(false);
                        message.save();
                        LocalBroadcastManager.getInstance(SendMessageService.this).sendBroadcast(new Intent(MessageActivity.UPDATE_MESSAGES));
                    }
                };
                message.setSending(true);
                message.save();
                LocalBroadcastManager.getInstance(SendMessageService.this).sendBroadcast(new Intent(MessageActivity.UPDATE_MESSAGES));
                switch (message.getContentType()) {
                    case Types.MESSSAGE_TEXT:
                        Message.sendMessage(this, message.getBody(), message.getContentType(), message.getChatId(), listener);
                        break;
                    case Types.MESSSAGE_AUDIO:
                        Message e = new Message(message.getBody(), User.getCurrentUser(), message.getChatId(), Types.MESSSAGE_AUDIO);
                        e.sendAsAudio(this, new File(message.getLocalPath()), listener);
                        break;
                    case Types.MESSSAGE_PICTURE:
                        Message m = new Message(message.getBody(), User.getCurrentUser(), message.getChatId(), Types.MESSSAGE_AUDIO);
                        m.sendAsImage(this, new File(message.getLocalPath()), listener);
                        break;
                }
            }
        }catch (SQLiteException e){e.printStackTrace();}
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void updateToSend() {
        trySendAll();
    }

}
