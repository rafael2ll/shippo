package com.ft.shippo.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ft.shippo.R;
import com.ft.shippo.adapters.ChatMediaAdapter;
import com.ft.shippo.models.OfflineChat;
import com.ft.shippo.models.OfflineMessage;
import com.ft.shippo.utils.Types;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import java.io.File;
import java.util.List;

/**
 * Created by rafael on 14/02/18.
 */

public class MediaDialogFragment extends DialogFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    ChatMediaAdapter chatMediaAdapter;

    OfflineChat chat;
    List<OfflineMessage> offlineMessageList;

    public MediaDialogFragment() {
    }

    public void setChat(OfflineChat chat) {
        this.chat = chat;
        this.offlineMessageList = OfflineMessage.find(OfflineMessage.class, "chatid = ? and contenttype <> ?", chat.getChatId(), Types.MESSSAGE_TEXT);
        for (OfflineMessage message : offlineMessageList)
            if (!new File(message.getLocalPath()).exists()) offlineMessageList.remove(message);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler, container, false);
        ButterKnifeLite.bind(this, v);
        v.findViewById(R.id.toolbar_includer).setVisibility(View.VISIBLE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        toolbar.setTitleTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Medium);
        toolbar.setTitle(Html.fromHtml(String.format(getText(R.string.media_shared_with).toString(), chat.getOtherUser().getUsername())));
        toolbar.setTitleTextColor(Color.WHITE);
        chatMediaAdapter = new ChatMediaAdapter(getActivity(), offlineMessageList);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        recyclerView.setAdapter(chatMediaAdapter);
        return v;
    }
}
