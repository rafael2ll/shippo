package com.ft.shippo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.error.VolleyError;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.ft.shippo.adapters.MessageAdapter;
import com.ft.shippo.fragments.ImagePreviewDialog;
import com.ft.shippo.fragments.MediaDialogFragment;
import com.ft.shippo.models.BooleanListener;
import com.ft.shippo.models.Chat;
import com.ft.shippo.models.Message;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.OfflineChat;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.models.Silence;
import com.ft.shippo.models.User;
import com.ft.shippo.utils.GlideRequestManager;
import com.ft.shippo.utils.Types;
import com.ft.shippo.utils.Utils;
import com.google.gson.Gson;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;
import com.mindorks.butterknifelite.annotations.OnClick;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    public static final String NEW_MESSAGE_ACTION = "com.ft.shippo.MessageActivity.NEW_MESSAGE";
    public static final String UPDATE_MESSAGES = "com.ft.shippo.MessageActivity.UPDATE_MESSAGES";
    @BindView(R.id.activity_message_tv_last_on)
    TextView textViewLastOn;
    @BindView(R.id.activity_message_tv_username)
    TextView textViewUsername;
    @BindView(R.id.activity_message_et_message)
    EditText editTextMessage;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fab_send)
    FloatingActionButton fabSend;

    @BindView(R.id.activity_message_text_view)
    View textView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    List<Image> imageList = new ArrayList<>();
    Handler handler = new Handler();
    TextView time;
    LinearLayoutManager layoutManager;
    @BindView(R.id.activity_message_civ)
    private CircleImageView civProfile;
    private OfflineChat offlineChat;
    private User otherUser;
    private Gson gson;
    private MediaRecorder mRecorder;
    private MessageAdapter messagesAdapter;
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (recyclerView != null) {
                if (intent.getAction().equals(NEW_MESSAGE_ACTION)) {
                    messagesAdapter.newMessage();
                    recyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
                }else if(intent.getAction().equals(MyFirebaseMessagingService.USER_ISONLINE)){
                    if(otherUser.getId().equals(intent.getExtras().getString("user_id")))
                        otherUser.setIsOnline(true);
                    if(textViewLastOn != null)textViewLastOn.setText(R.string.online);
                }else if(intent.getAction().equals(MyFirebaseMessagingService.USER_BECOME_OFFLINE)){
                    if(otherUser.getId().equals(intent.getExtras().getString("user_id")))
                        otherUser.setIsOnline(true);
                    if(textViewLastOn != null)textViewLastOn.setText(getString(R.string.last_seen, new DateTime(Utils.toLocalDate(otherUser.getUpdatedAt())).toString("EEE H:mm")));
                }
                else {
                    messagesAdapter.update();
                }
            }
        }
    };;
    private int silence_option;
    private Runnable chatRunnable = new Runnable() {
        @Override
        public void run() {
            Chat.updateChat(MessageActivity.this, offlineChat.getChatId(), new ObjectListener<Chat>(Chat.class){

                @Override
                public void onResult(Chat data, String error) {
                    offlineChat.update(data).save();
                    otherUser = offlineChat.getOtherUser();
                    if(textViewUsername == null)return;
                    textViewUsername.setText(otherUser.getUsername());
                    GlideRequestManager.get().asBitmap()
                            .load(otherUser.getPhotoUri())
                            .error(R.drawable.ic_default_avatar)
                            .override(100).into(civProfile);

                    if(otherUser.isOnline())textViewUsername.setText(R.string.online);
                    try {
                        textViewLastOn.setText(getString(R.string.last_seen, new DateTime(new SimpleDateFormat().parse(Utils.toLocalDate(otherUser.getUpdatedAt()))).toString("EEE H:mm")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onError(VolleyError error) {
                    if(textViewUsername == null)return;
                    textViewUsername.setText(otherUser.getUsername());
                    GlideRequestManager.get().asBitmap()
                            .load(otherUser.getPhotoUri())
                            .error(R.drawable.ic_default_avatar)
                            .override(100).into(civProfile);

                    if(otherUser.isOnline())textViewUsername.setText(R.string.online);
                    try {
                        textViewLastOn.setText(getString(R.string.last_seen, new DateTime(new SimpleDateFormat().parse(Utils.toLocalDate(otherUser.getUpdatedAt()))).toString("EEE H:mm")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            });
            handler.postDelayed(this, 1000*60*3);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnifeLite.bind(this);
        setSupportActionBar(toolbar);
        gson = new Gson();
        if (getIntent().hasExtra("chat_id"))
            offlineChat = OfflineChat.find(OfflineChat.class, "chatid = ?", getIntent().getExtras().getString("chat_id")).get(0);
        if (getIntent().hasExtra("chat")) {
            offlineChat = gson.fromJson(getIntent().getExtras().getString("chat", ""), OfflineChat.class);
        }
        Chat.getMessages(this);
        otherUser = offlineChat.getOtherUser();

        textViewUsername.setText(otherUser.getUsername());
        GlideRequestManager.get().asBitmap()
                .load(otherUser.getPhotoUri())
                .error(R.drawable.ic_default_avatar)
                .override(100).into(civProfile);

        try {
            textViewLastOn.setText(getString(R.string.last_seen, new DateTime(new SimpleDateFormat().parse(Utils.toLocalDate(otherUser.getUpdatedAt()))).toString("EEE H:mm")));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        messagesAdapter = new MessageAdapter(this, offlineChat.getChatId(), layoutManager);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messagesAdapter);
        recyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fabSend.setImageResource(s.toString().length() > 0 ? R.drawable.ic_send_white : R.drawable.ef_ic_camera_white);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_user_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        OfflineChat.setOpened(offlineChat.getChatId());
        IntentFilter intentFilter = new IntentFilter(NEW_MESSAGE_ACTION);
        intentFilter.addAction(UPDATE_MESSAGES);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);
        handler.postDelayed(chatRunnable, 1000*60*3);
    }

    @Override
    protected void onResume() {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (recyclerView != null) {
                    if (intent.getAction().equals(NEW_MESSAGE_ACTION)) {
                        messagesAdapter.newMessage();
                        recyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
                    } else if (intent.getAction().equals(MyFirebaseMessagingService.USER_ISONLINE)) {
                        if (otherUser.getId().equals(intent.getExtras().getString("user_id")))
                            otherUser.setIsOnline(true);
                        if (textViewLastOn != null) textViewLastOn.setText(R.string.online);
                    } else if (intent.getAction().equals(MyFirebaseMessagingService.USER_BECOME_OFFLINE)) {
                        if (otherUser.getId().equals(intent.getExtras().getString("user_id")))
                            otherUser.setIsOnline(true);
                        if (textViewLastOn != null)
                            textViewLastOn.setText(getString(R.string.m_ago, 2));
                    } else {
                        messagesAdapter.update();
                    }
                }
            }
        };
        OfflineChat.setOpened(offlineChat.getChatId());
        IntentFilter intentFilter = new IntentFilter(NEW_MESSAGE_ACTION);
        intentFilter.addAction(UPDATE_MESSAGES);
        handler.postDelayed(chatRunnable, 1000*60*3);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(chatRunnable);
        OfflineChat.removeOpened();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        OfflineChat.removeOpened();
        handler.removeCallbacks(chatRunnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_show_perfil:
                startActivity(new Intent(this, UserActivity.class).putExtra("user_key", otherUser.getId()));
                break;
            case R.id.menu_item_show_media:
                MediaDialogFragment mediaDialogFragment = new MediaDialogFragment();
                mediaDialogFragment.setChat(offlineChat);
                mediaDialogFragment.show(getSupportFragmentManager(), "");
                break;
            case R.id.menu_item_silence:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                List<Silence> silenceList = Silence.find(Silence.class, "user_id = ?", otherUser.getId());

                builder.setSingleChoiceItems(R.array.silence_array, silenceList.size() > 0 ? silenceList.get(0).getOption() : 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        silence_option = which;

                    }
                }).setTitle(R.string.silence_for)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.silence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                        if(silence_option == 0)Silence.deleteAll(Silence.class, "user_id = ?", otherUser.getId());
                                else new Silence(otherUser.getId(), DateTime.now().toString(), DateTime.now().plusMinutes(silence_option == 1? 30 : silence_option == 2? 2*60
                                                : silence_option == 3? 6*60 : silence_option == 4 ? 12*60 : silence_option == 5 ? 24*60 : silence_option == 6 ? 7*24*60
                                                : silence_option == 7? 365*24*60 : 10*365*24*60).toString(), silence_option).save();
                                User.silence(MessageActivity.this, offlineChat.getOtherUser().getId(), silence_option, new ObjectListener<User>(User.class) {
                                    @Override
                                    public void onResult(User data, String error) {
                                        Toast.makeText(MessageActivity.this, R.string.user_muted_successfully, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(VolleyError error) {

                                    }
                                });
                            }
                        }).show();
                break;
            case R.id.menu_item_delete:
                new AlertDialog.Builder(this).setMessage(R.string.delete_chat).
                        setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                OfflineMessage.deleteAll(OfflineMessage.class, "chatid = ?", offlineChat.getChatId());
                                Chat.delete(MessageActivity.this, offlineChat.getChatId(), new BooleanListener() {
                                    @Override
                                    public void onResult(boolean data, String error) {
                                        offlineChat.delete();
                                        finish();
                                    }

                                    @Override
                                    public void onError(VolleyError error) {

                                    }
                                });
                            }
                        }).show();

        }
        return true;
    }

    @OnClick(R.id.activity_message_civ)
    public void userClick() {
        ContextCompat.startActivity(this,
                new Intent(this, UserActivity.class).putExtra(UserActivity.USER_KEY, otherUser.getId()), null);

    }

    @OnClick(R.id.activity_message_iv_back)
    public void onBack() {
        finish();
    }

    @OnClick(R.id.fab_send)
    public void send() {
        Chat.updateChat(this, offlineChat.getChatId(), new ObjectListener<Chat>(Chat.class){

            @Override
            public void onResult(Chat data, String error) {
                offlineChat.update(data).save();
                otherUser = offlineChat.getOtherUser();
                if(textViewUsername == null)return;
                textViewUsername.setText(otherUser.getUsername());
                GlideRequestManager.get().asBitmap()
                        .load(otherUser.getPhotoUri())
                        .error(R.drawable.ic_default_avatar)
                        .override(100).into(civProfile);

                textViewLastOn.setText(getString(R.string.last_seen, new DateTime(Utils.toLocalDate(otherUser.getUpdatedAt())).toString("EEE H:mm")));

            }

            @Override
            public void onError(VolleyError error) {

            }
        });
        String text = editTextMessage.getText().toString();
       if (!text.isEmpty()) {
            editTextMessage.setText("");
            final OfflineMessage offlineMessage = new OfflineMessage(text, User.getCurrentUser().getId(), offlineChat.getChatId(), Types.MESSSAGE_TEXT, null, DateTime.now());
            offlineMessage.isSent(false);
            offlineMessage.save();
            newMessage();
        }else galleryClick();
    }

    private void newMessage() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(this, SendMessageService.class).setAction(SendMessageService.NEW_MESSAGE_TO_SEND));
        if (recyclerView != null) {
            messagesAdapter.newMessage();
            recyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
        }
    }

    public void galleryClick() {
        ImagePicker.create(this)
                .returnMode(ReturnMode.CAMERA_ONLY) // set whether pick and / or camera action should return immediate result or not.
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle("Folder") // folder selection title
                .toolbarImageTitle("Tap to select") // image selection title
                .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color
                .limit(5) // max images can be selected (99 by default
                .showCamera(true) // show camera or not (true by default)
                .theme(R.style.AppTheme) // must inherit ef_BaseTheme. please refer to sample
                .enableLog(true) // disabling log
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            this.imageList = images;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (imageList.size() > 0) {
            ImagePreviewDialog dialog = new ImagePreviewDialog();
            dialog.setImageList(imageList, new ImagePreviewDialog.OnSendListener() {
                @Override
                public void send(List<Image> imageList) {
                    for (Image image : imageList) {
                        File cacheFile = new File(getExternalCacheDir(), "PICTURE_"+System.currentTimeMillis()+".png");
                        try {
                            BitmapFactory.decodeFile(image.getPath()).compress(Bitmap.CompressFormat.PNG, 90, new FileOutputStream(cacheFile));
                            final OfflineMessage offlineMessage = new OfflineMessage(cacheFile.getPath(), User.getCurrentUser().getId(), offlineChat.getChatId(), Types.MESSSAGE_PICTURE, null, DateTime.now());
                            offlineMessage.loadImageMetadata(MessageActivity.this, new File(image.getPath()));
                            offlineMessage.isSent(false);
                            offlineMessage.save();
                            newMessage();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    MessageActivity.this.imageList.clear();

                }
            });

            dialog.show(getSupportFragmentManager(), "");
        }
    }

    private void reveal(final View contentShow, final View contentHide) {
        AlphaAnimation alphaAnimationShow = new AlphaAnimation(0, 1);
        AlphaAnimation alphaAnimationHide = new AlphaAnimation(1, 0);

        alphaAnimationHide.setDuration(250);
        alphaAnimationShow.setDuration(250);
        alphaAnimationHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                contentHide.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        alphaAnimationShow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                contentShow.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        contentHide.startAnimation(alphaAnimationHide);
        contentShow.startAnimation(alphaAnimationShow);
    }
}
