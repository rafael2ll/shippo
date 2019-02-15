package com.ft.shippo.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ft.shippo.MyFirebaseMessagingService;
import com.ft.shippo.R;
import com.ft.shippo.holders.AudioHolder;
import com.ft.shippo.holders.BaseHolder;
import com.ft.shippo.holders.DateHolder;
import com.ft.shippo.holders.MessageHolder;
import com.ft.shippo.holders.PictureHolder;
import com.ft.shippo.models.OfflineMessage;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by rafael on 12/02/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<BaseHolder> {
    private final int layouts[] = new int[]{R.layout.holder_message, R.layout.holder_audio, R.layout.holder_message_image, R.layout.holder_date, 0};
    private List<MessageItem> messageItemList = new ArrayList<>();
    private String chat_id;
    private LinearLayoutManager layoutManager;
    private Context ctx;
    private OnMessageSelectListener messageSelectListener;

    public MessageAdapter(Context ctx, String chat_id, LinearLayoutManager linearLayoutManager) {
        this.chat_id = chat_id;
        this.ctx = ctx;
        this.layoutManager = linearLayoutManager;
        messageSelectListener = new OnMessageSelectListener() {
            @Override
            public void onMessageSelect(MessageItem message) {
                message.select(true);
                notifyItemChanged(messageItemList.size() - 1 - messageItemList.indexOf(message));
            }

            @Override
            public void onMessageUnselect(MessageItem message) {
                message.select(false);
                notifyItemChanged(messageItemList.size() - 1 - messageItemList.indexOf(message));
            }
        };
        List<OfflineMessage> offlineMessageList = OfflineMessage.find(OfflineMessage.class, "chatid = ?", new String[]{chat_id}, null, "createdat desc", "50");

        for (OfflineMessage message : offlineMessageList) {
            if (!message.isMine() && message.isNotSeen()) {
                message.setRead(ctx);
            }
        }
        if (offlineMessageList.size() > 0) {
            addToStart(offlineMessageList.get(0), true);
            if(offlineMessageList.size()>1)addToEnd(offlineMessageList.subList(1, offlineMessageList.size()), false);
            MyFirebaseMessagingService.updateNotifcation(ctx);
        }
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        int view = layouts[viewType];
        switch (viewType) {
            case 0:
                return new MessageHolder(inflater.inflate(view, parent, false));
            case 1:
                return new AudioHolder(inflater.inflate(view, parent, false));
            case 2:
                return new PictureHolder(inflater.inflate(view, parent, false));
            case 3:
                return new DateHolder(inflater.inflate(view, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        final MessageItem item = messageItemList.get(messageItemList.size() - 1 - position);
        if(item.getMessage() != null){
            if(item.getMessage().isNotSeen() && !item.getMessage().isMine()){
                item.getMessage().setRead(ctx);
            }
        }
        holder.feedWith(ctx, holder instanceof AudioHolder ? item.getMessage() :
                holder instanceof MessageHolder ? item.getMessage() : holder instanceof PictureHolder ? item.getMessage() : item.getDate(), item.isSelected());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (item.isSelected()) messageSelectListener.onMessageUnselect(item);
                else messageSelectListener.onMessageSelect(item);
                return true;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return messageItemList.get(messageItemList.size() - position - 1).type.ordinal();
    }

    @Override
    public int getItemCount() {
        return messageItemList.size();
    }

    public void newMessage() {
        OfflineMessage message = OfflineMessage.find(OfflineMessage.class, "chatid = ?", new String[]{chat_id}, "", "id DESC", "1").get(0);
        MyFirebaseMessagingService.updateNotifcation(ctx);
        addToStart(message, true);
    }

    public void update() {
        int i = 0;
        for (MessageItem message : messageItemList) {
            if (message.getMessage() != null) {
                if (message.getMessage().isMine()) {
                    if (!message.getMessage().otherSaw() || !message.getMessage().getIsSent()) {
                        OfflineMessage offlineMessage = OfflineMessage.findById(OfflineMessage.class, message.getMessage().getId());
                        message.message = offlineMessage;
                        notifyItemChanged(messageItemList.size()- 1 - messageItemList.indexOf(message));
                    }
                }
                i++;
            }
        }
    }

    public void addToStart(OfflineMessage offlineMessage, boolean scroll) {
        if(contains(offlineMessage))return;
        boolean isNewMessageToday = !isPreviousSameDate(0, new DateTime(offlineMessage.getCreatedAt()).toLocalDate());
        if (isNewMessageToday) {
            messageItemList.add(0, new MessageItem(null, MessageItem.Type.DATE, offlineMessage.getCreatedAt()));
        }
        messageItemList.add(0, new MessageItem(offlineMessage,
                MessageItem.Type.valueOf(offlineMessage.getContentType()), offlineMessage.getCreatedAt()));
        notifyItemRangeInserted(messageItemList.size() - 1, isNewMessageToday ? 2 : 1);
        if (layoutManager != null && scroll) {
            layoutManager.scrollToPosition(messageItemList.size() - 1);
        }
    }

    private boolean contains(OfflineMessage offlineMessage) {
        for(MessageItem item : messageItemList){
            if(item.getMessage() != null)
                if(item.getMessage().getId().equals(offlineMessage.getId())) return true;
        }
        return false;
    }

    private boolean isPreviousSameDate(int position, LocalDate dateToCompare) {
        if (messageItemList.size() <= position) return false;
        LocalDate date = new DateTime(messageItemList.get(position).getDate()).toLocalDate();
        return date.isEqual(dateToCompare);
    }

    public void addToEnd(List<OfflineMessage> offlineMessages, boolean reverse) {
        if (reverse) Collections.reverse(offlineMessages);

        if (!messageItemList.isEmpty()) {
            int last = messageItemList.size() - 1;
            if (isPreviousSameDate(last, new DateTime(offlineMessages.get(0).getCreatedAt()).toLocalDate())) {
                if (messageItemList.get(last).isDate()) messageItemList.remove(last);
            }
        }

        int oldSize = messageItemList.size();
        generateDateHeaders(offlineMessages);
        notifyItemRangeInserted(oldSize, messageItemList.size() - oldSize);
    }

    private void generateDateHeaders(List<OfflineMessage> offlineMessages) {
        for (int i = 0; i < offlineMessages.size(); i++) {
            OfflineMessage message = offlineMessages.get(i);
            this.messageItemList.add(new MessageItem(message, MessageItem.Type.valueOf(message.getContentType()), message.getCreatedAt()));
            if (offlineMessages.size() > i + 1) {
                OfflineMessage nextMessage = offlineMessages.get(i + 1);
                LocalDate localDate = new LocalDate(message.getCreatedAt());
                LocalDate localDate1;
                try {
                    localDate1 = new LocalDate(nextMessage.getCreatedAt().toString());
                }catch (Exception ignored){
                    localDate1 = localDate;
                }
                if (!localDate.equals(localDate1)) {
                    this.messageItemList.add(MessageItem.newDate(message.getCreatedAt()));
                }
            } else {
                this.messageItemList.add(MessageItem.newDate(message.getCreatedAt()));
            }
        }
    }

    interface OnMessageSelectListener {
        public void onMessageSelect(MessageItem message);

        public void onMessageUnselect(MessageItem message);
    }

    public static class MessageItem {

        OfflineMessage message = null;
        Type type;
        boolean isSelected;
        Date date;
        public MessageItem(OfflineMessage message, Type type, Date date) {
            this.message = message;
            this.type = type;
            this.date = date;
        }

        public static MessageItem newDate(Date createdAt) {
            return new MessageItem(null, Type.DATE, createdAt);
        }

        public OfflineMessage getMessage() {
            return message;
        }

        public Type getType() {
            return type;
        }

        public Date getDate() {
            return date;
        }

        public void select(boolean selected) {
            isSelected = selected;
        }

        public boolean isDate() {
            return type.equals(Type.DATE);
        }

        public boolean isSelected() {
            return isSelected;
        }

        public enum Type {TEXT, AUDIO, PICTURE, DATE, LOCAL}

    }
}
