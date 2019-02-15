package com.ft.shippo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.error.VolleyError;
import com.ft.shippo.models.Chat;
import com.ft.shippo.models.ListObjectListener;
import com.ft.shippo.models.Message;
import com.ft.shippo.models.OfflineChat;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.OfflineNotification;
import com.ft.shippo.models.Silence;
import com.ft.shippo.models.User;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by rafael on 07/02/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String UPDATE_NOTFICATION = "com.ft.shippo.MyFirebaseMessagingService.UPDATE_NOTIFICATION"; //chat
    public static final String USER_ISONLINE = "com.ft.shippo.MyFirebaseMessagingService.USER_BECOME_ONLINE"; //user is online
    public static final String USER_BECOME_OFFLINE = "com.ft.shippo.MyFirebaseMessagingService.USER_BECOME_OFFLINE"; //user is online
    public static final String MESSAGE_CHANGED = "com.ft.shippo.MyFirebaseMessagingService.MESSAGE_CHANGED"; //chat
    public static final String NEW_NOTIFICATION = "com.ft.shippo.MyFirebaseMessagingService.NEW_NOTIFICATION"; //user general notifications
    static String[] types = new String[]{"NEW_MESSAGE", "MESSAGE_READ", "NEW_CHAT"};
    private static final String TAG = "MessageService";
    Types type;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UPDATE_NOTFICATION)) updateNotifcation(context);
        }
    };

    public static void updateNotifcation(final Context context) {

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "shippo_messages";// The id of the channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        try {
            List<OfflineMessage> messagesToRead = OfflineMessage.find(OfflineMessage.class, "userid <> ? and i_saw = ?", User.getCurrentUser().getId(), "0");
            if (messagesToRead.size() > 0) {
                List<String> chatIds = new ArrayList<>();
                Log.d("notification", messagesToRead.toString());
                OfflineChat chat = null;
                for (OfflineMessage message : messagesToRead) {
                    if (!chatIds.contains(message.getChatId())) {
                        chatIds.add(message.getChatId());
                    }
                }

                Intent intent = new Intent(context, chatIds.size() == 1 ? MessageActivity.class : MainActivity.class);
                intent.putExtra("chat_id", chatIds.get(0));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);

                NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle("Messages");
                int i = 0;
                for (OfflineMessage message : messagesToRead) {
                    List<Silence> silenceList = Silence.find(Silence.class, "userid = ?", message.getUser());
                    if (silenceList.size() > 0) {
                        Silence silence = silenceList.get(0);
                        if (DateTime.now().isBefore(new DateTime(silence.getEnd()))) continue;
                        else silence.delete();
                    }
                    List<OfflineChat> chatList = OfflineChat.find(OfflineChat.class, "chatid = ?", message.getChatId());
                    if (chatList.size() == 0) {
                        Chat.getMyConversations(context, new ListObjectListener<Chat>(com.ft.shippo.utils.Types.LIST_CHAT) {
                            @Override
                            public void onResult(List<Chat> data, String error) {
                                for (Chat chat : data) {
                                    List<OfflineChat> oldChatList = OfflineChat.find(OfflineChat.class, "chatid = ?", chat.getId());
                                    if (oldChatList.size() > 0) {
                                        oldChatList.get(0).update(chat).save();
                                    } else {
                                        OfflineChat offlineChat = new OfflineChat(chat);
                                        offlineChat.save();
                                    }
                                }
                                updateNotifcation(context);
                            }

                            @Override
                            public void onError(VolleyError error) {

                            }
                        });
                    } else {
                        chat = chatList.get(0);
                        //   if(i ==0)GlideRequestManager.get().load(chat.getOtherUser().getPhotoUri()).into(target);
                        if (chat.isSilenced() || chat.isBlocked() || OfflineChat.isOpened(chat.getChatId()))
                            continue;
                        if (message.isText())
                            style.addMessage(message.getBody(), new DateTime(message.getCreatedAt()).getMillis(), chat.getOtherUser().getUsername());
                        else if (message.isAudio()) {
                            try {
                                long duration = message.getMetadata().getLong("duration");
                                DateFormat df = new SimpleDateFormat("mm:ss", Locale.getDefault());
                                String formatted = df.format(duration);
                                style.addMessage(context.getString(R.string.audio, formatted), new DateTime(message.getCreatedAt()).getMillis(), chat.getOtherUser().getUsername());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (message.isPicture()) {
                            style.addMessage(context.getString(R.string.image), new DateTime(message.getCreatedAt()).getMillis(), chat.getOtherUser().getUsername());
                        }
                        i++;
                    }

                    notificationBuilder
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round)).setStyle(style);
                    notificationBuilder.setSmallIcon(R.drawable.ic_comment_white)
                            .setContentTitle(context.getResources().getString(R.string.app_name))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentIntent(pendingIntent);
                    if (notificationManager != null) {
                        notificationManager.notify(245, notificationBuilder.build());
                    }
                }
            } else {
                notificationManager.cancel(245);
            }
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_NOTFICATION);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            Map<String, String> data = remoteMessage.getData();
            type = Types.valueOf(data.get("type"));
            switch (type) {
                case USER_ONLINE:
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(USER_ISONLINE).putExtra("user_id", data.get("user_id")));
                    break;
                case USER_OFFLINE:
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(USER_BECOME_OFFLINE).putExtra("user_id", data.get("user_id")));
                    break;
                case NEW_CHAT:
                    handleNewChat(data);
                    break;
                case NEW_MESSAGE:
                    handleNewMessage(data);
                    break;
                case MESSAGE_READ:
                    handleMessageRead(data);
                    break;
                case NOTIFICATION:
                    handleNotification(data);
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MESSAGE_CHANGED));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNotification(Map<String, String> data) {
        com.ft.shippo.models.Notification notification = new Gson().fromJson(data.get("notification"), com.ft.shippo.models.Notification.class);
        OfflineNotification offlineNotification = new OfflineNotification(notification);
        List<OfflineNotification> offlineNotifications = OfflineNotification.findByID(notification.getId());
        if (offlineNotifications.size() > 0) {
            offlineNotification = offlineNotifications.get(0);
            offlineNotification.update(notification);
            offlineNotification.save();
        } else {
            offlineNotification.save();
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(NEW_NOTIFICATION));
    }

    private void handleNewChat(Map<String, String> data) {
    }

    private void handleNewMessage(Map<String, String> data) {
        Message e = new Gson().fromJson(data.get("message"), Message.class);
        try {
            OfflineMessage offlineMessage = new OfflineMessage(e);
            offlineMessage.save();
            updateNotifcation(this);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MessageActivity.NEW_MESSAGE_ACTION));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private void handleMessageRead(Map<String, String> data) {
        String message_id = data.get("message_id");
        String user_id = data.get("read_by");
        Log.d(TAG, "message read: " + user_id);
        List<OfflineMessage> offlineMessages = OfflineMessage.find(OfflineMessage.class, "_id=?", message_id);
        if (offlineMessages.size() > 0) {
            OfflineMessage offlineMessage = offlineMessages.get(0);
            offlineMessage.addSeenBy(user_id);
            offlineMessage.save();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MessageActivity.UPDATE_MESSAGES));

        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    public enum Types {
        NEW_MESSAGE {
            public String getMessageKey() {
                return "message";
            }

            @Override
            public String toString() {
                return types[0];
            }
        }, MESSAGE_READ {
            public String getUserReadKey() {
                return "read_by";
            }

            @Override
            public String toString() {
                return types[1];
            }
        }, NEW_CHAT {
            public String getChatKey() {
                return "chat";
            }

            @Override
            public String toString() {
                return types[2];
            }
        },
        NOTIFICATION {
            @Override
            public String toString() {
                return types[3];
            }
        },  USER_ONLINE, USER_OFFLINE,
    }
}
