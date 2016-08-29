package com.quickblox.sample.core.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.core.R;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QBMessagesAdapter extends RecyclerView.Adapter<QBMessagesAdapter.QBMessageViewHolder> implements QBBaseAdapter<QBChatMessage> {
    private static final String TAG = QBMessagesAdapter.class.getSimpleName();
    private List<Integer> customViewList;
    private String textOwn;
    private String timeOwn;
    private String textOpp;
    private String timeOpp;

    protected enum ViewTypes {TYPE_OWN_MESSAGE, TYPE_OPPONENT_MESSAGE, TYPE_ATTACHMENT_MESSAGE_OWN, TYPE_ATTACHMENT_MESSAGE_OPPONENT, TYPE_ATTACHMENT_CUSTOM}

    private QBMessageViewHolder qbViewHolder;

    private int preferredImageSizePreview = (int) (80 * Resources.getSystem().getDisplayMetrics().density);

    protected LayoutInflater inflater;
    protected Context context;
    protected List<QBChatMessage> chatMessages;
    private RequestListener glideRequestListener;
    private
    @LayoutRes
    int widgetLayoutResId;

    private boolean useEmbeddedImageLoader = true;

    private int typeOwnAttachmentMessageLayoutResource = R.layout.item_attachment_message_own;
    private int typeOpponentAttachmentMessageLayoutResource = R.layout.item_attachment_message_opponent;
    private int typeOwnMessageLayoutResource = R.layout.item_text_message_own;
    private int typeOpponentMessageLayoutResource = R.layout.item_text_message_opponent;
    private int typeAttachmentLayoutResource;

    protected int[] viewItemIDs = {R.id.message_textview, R.id.time_text_message_textview, R.id.attach_imageview, R.id.centered_progressbar};
    protected int[] viewItemIDsCustom;
    protected Map<String, Integer> viewItemIDsList;

    public QBMessagesAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public QBMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewTypes valueType = ViewTypes.values()[viewType];
        switch (valueType) {
            case TYPE_OWN_MESSAGE:
                qbViewHolder = new MessageOwnHolder(inflater.inflate(typeOwnMessageLayoutResource, parent, false), R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case TYPE_OPPONENT_MESSAGE:
                qbViewHolder = new MessageOpponentHolder(inflater.inflate(typeOpponentMessageLayoutResource, parent, false), R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                qbViewHolder = new AttachOwnHolder(inflater.inflate(typeOwnAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);
                return qbViewHolder;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                qbViewHolder = new AttachOpponentHolder(inflater.inflate(typeOpponentAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);

            case TYPE_ATTACHMENT_CUSTOM:
                Log.d("QBMessagesAdapter", "onCreateViewHolder case TYPE_ATTACHMENT_CUSTOM");
//               resource must be set manually
                return new AttachOpponentHolder(inflater.inflate(typeOpponentAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);
            default:
                return null;
        }
    }

    /**
     * Add id of the ViewHolderItem.
     *
     * @param ids array of IDs in order messageTextView, timeTextMessageTextViewID, attach_imageViewID, attachmentProgressBarID.
     */
//    protected void setCustomViewHolderItemIDs(@IdRes int... ids) {
//        for (int x = 0; x < ids.length; x++) {
//            if (ids[x] != 0) {
//                Log.d(TAG, "ids[x]= " + ids[x]);
//                viewItemIDs[x] = ids[x];
//            }
//        }
//    }

    /**
     * array of IDs in order messageTextView, timeTextMessageTextViewID, attach_imageViewID, attachmentProgressBarID
     */
    protected int[] setCustomViewItemIDs() {
        return viewItemIDs;
    }

    protected void setOwnMessageLayoutResource(@LayoutRes int typeOwnMessageLayoutResource) {
        this.typeOwnMessageLayoutResource = typeOwnMessageLayoutResource;
    }

    protected void setOpponentMessageLayoutResource(@LayoutRes int typeOpponentMessageLayoutResource) {
        this.typeOpponentMessageLayoutResource = typeOpponentMessageLayoutResource;
    }

    protected void setOwnAttachmentMessageLayoutResource(@LayoutRes int typeOwnAttachmentMessageLayoutResource) {
        this.typeOwnAttachmentMessageLayoutResource = typeOwnAttachmentMessageLayoutResource;
    }

    protected void setOpponentAttachmentMessageLayoutResource(@LayoutRes int typeOpponentAttachmentMessageLayoutResource) {
        this.typeOpponentAttachmentMessageLayoutResource = typeOpponentAttachmentMessageLayoutResource;
    }

    protected void setMessageLayoutResourceByType(ViewTypes typeLayout, @LayoutRes int messageLayoutResource) {
        switch (typeLayout) {
            case TYPE_OWN_MESSAGE:
                typeOwnMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_OPPONENT_MESSAGE:
                typeOpponentMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                typeOwnAttachmentMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                typeOpponentMessageLayoutResource = messageLayoutResource;
                break;
            default:
                break;
        }
    }

    public void addCustomView(@IdRes int... ids) {
        customViewList = new ArrayList<>();
        for (int ID : ids) {
            customViewList.add(ID);
        }
    }

    public void setWidgetLayoutResource(@LayoutRes int widgetLayoutResId) {
        if (widgetLayoutResId != this.widgetLayoutResId) {
            // Layout changed
//            canRecycleLayout = false;
        }
        this.widgetLayoutResId = widgetLayoutResId;
    }

    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        switch (valueType) {
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                if (useEmbeddedImageLoader) {
                    Log.wtf(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
                    onBindViewAttachOwnHolder((AttachOwnHolder) holder, position);
                }
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                Log.wtf(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
//                initRequestListener(holder);
//                showAttachment(holder, chatMessage);
                break;
            case TYPE_OWN_MESSAGE:
                onBindViewMsgOwnHolder((MessageOwnHolder) holder, position);
                break;
            case TYPE_OPPONENT_MESSAGE:
                onBindViewMsgOpponentHolder((MessageOpponentHolder) holder, position);
                break;
            default:
                break;
        }

    }

    protected void onBindViewAttachOwnHolder(AttachOwnHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        initRequestListener(holder, ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN);
        showAttachment(holder, chatMessage, ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN);
    }

    protected void onBindViewMsgOwnHolder(MessageOwnHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);

        holder.messageTextView.setText((textOwn == null) ? chatMessage.getBody() : textOwn);
        holder.timeTextMessageTextView.setText((timeOwn == null) ? new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(new Date(chatMessage.getDateSent() * 1000)) : timeOwn);
    }

    protected void setTextOwnText(String str) {
        textOwn = str;
    }

    protected void setTimeOwnText(String str) {
        timeOwn = str;
    }

    protected void onBindViewMsgOpponentHolder(MessageOpponentHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);

        holder.messageTextView.setText((textOpp == null) ? chatMessage.getBody() : textOpp);
        holder.timeTextMessageTextView.setText((timeOpp == null) ? new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(new Date(chatMessage.getDateSent() * 1000)) : timeOpp);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public QBChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        QBChatMessage chatMessage = getItem(position);

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = chatMessage.getAttachments().iterator().next();
            Log.d("QBMessagesAdapter", "attachment.getType= " + attachment.getType());
            if (attachment.getType() != null && attachment.getType().equals(QBAttachment.PHOTO_TYPE)) {
                if (isIncoming(chatMessage)) {
                    return ViewTypes.TYPE_ATTACHMENT_MESSAGE_OPPONENT.ordinal();
                } else {
                    return ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN.ordinal();
                }
            } else {
                return ViewTypes.TYPE_ATTACHMENT_CUSTOM.ordinal();
            }
        } else {
            if (isIncoming(chatMessage)) {
                return ViewTypes.TYPE_OPPONENT_MESSAGE.ordinal();
            } else {
                return ViewTypes.TYPE_OWN_MESSAGE.ordinal();
            }
        }
    }

    @Override
    public void add(QBChatMessage item) {
        chatMessages.add(item);
        notifyDataSetChanged();
    }

    @Override
    public List<QBChatMessage> getList() {
        return chatMessages;
    }

    @Override
    public void addList(List<QBChatMessage> items) {
        chatMessages.addAll(0, items);
        notifyDataSetChanged();
    }

    @Override
    public QBChatMessage getItem(RecyclerView.ViewHolder viewHolder) {
        return null;
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    private void showAttachment(final QBMessageViewHolder holder, QBChatMessage chatMessage, ViewTypes type) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        QBAttachment attachment = attachments.iterator().next();
        Glide.with(context)
                .load(attachment.getUrl())
                .listener(glideRequestListener)
                .override(preferredImageSizePreview, preferredImageSizePreview)
                .dontTransform()
                .error(R.drawable.ic_error)
                .into((type == ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN) ? ((AttachOwnHolder)holder).attach_imageView : ((AttachOpponentHolder)holder).attach_imageView);
    }

    public void setUseGlideImageLoader(boolean flag) {
        useEmbeddedImageLoader = flag;
    }

    private void initRequestListener(final QBMessageViewHolder holder, final ViewTypes type) {
        glideRequestListener = new RequestListener() {
            QBMessageViewHolder viewHolder = (type == ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN) ? (AttachOwnHolder)holder : (AttachOpponentHolder)holder;
            @Override
            public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                e.printStackTrace();
                viewHolder.attach_imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                viewHolder.attachmentProgressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                viewHolder.attach_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.attachmentProgressBar.setVisibility(View.GONE);
                return false;
            }
        };
    }


    public static class MessageOwnHolder extends QBMessageViewHolder {
        public TextView messageTextView;
        public TextView timeTextMessageTextView;

        public MessageOwnHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
        }
    }

    public static class MessageOpponentHolder extends QBMessageViewHolder {
        TextView messageTextView;
        TextView timeTextMessageTextView;

        public MessageOpponentHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
        }
    }

    public static class AttachOwnHolder extends QBMessageViewHolder {
//        ImageView attach_imageView;
//        ProgressBar attachmentProgressBar;

        public AttachOwnHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attach_imageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
        }
    }

    public static class AttachOpponentHolder extends QBMessageViewHolder {
//        ImageView attach_imageView;
//        ProgressBar attachmentProgressBar;

        public AttachOpponentHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attach_imageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
        }
    }

    public abstract static class QBMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView attach_imageView;
        ProgressBar attachmentProgressBar;

        public QBMessageViewHolder(View itemView) {
            super(itemView);
        }
    }


    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public View customLayout;
        TextView messageTextView;
        TextView timeTextMessageTextView;
        ImageView attach_imageView;
        ProgressBar attachmentProgressBar;
        public List<View> listViewBind;

        public ViewHolder(View itemView, LayoutInflater inflater, ViewTypes viewType, List<Integer> listView, @LayoutRes int widgetLayoutResId, int... itemIDs) {
            super(itemView);
            setViewFromCustomLayout(listView);
            setCustomWidget(itemView, inflater, widgetLayoutResId);
            switch (viewType) {
                case TYPE_ATTACHMENT_MESSAGE_OWN:
                case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                    attach_imageView = (ImageView) itemView.findViewById(itemIDs[2]);
                    attachmentProgressBar = (ProgressBar) itemView.findViewById(itemIDs[3]);
                    break;
                case TYPE_OWN_MESSAGE:
                case TYPE_OPPONENT_MESSAGE:
                    Log.d(TAG, "messageTextView itemIDs[0]= " + itemIDs[0]);
                    messageTextView = (TextView) itemView.findViewById(itemIDs[0]);
                    Log.d(TAG, "messageTextView getId= " + messageTextView.getId());
                    break;
                default:
                    break;
            }
            timeTextMessageTextView = (TextView) itemView.findViewById(itemIDs[1]);
        }

        public ViewHolder(View itemView) {
            super(itemView);
        }

        private void setViewFromCustomLayout(List<Integer> listView) {
            if (listView != null) {
                listViewBind = new ArrayList<>();
                for (Integer ID : listView) {
                    Log.d(TAG, "ID= " + ID);
                    View view = itemView.findViewById(ID);
                    listViewBind.add(view);
                }
            }
        }

        private void setCustomWidget(View itemView, LayoutInflater inflater, @LayoutRes int widgetLayoutResId) {
            final ViewGroup widgetFrame = (ViewGroup) itemView
                    .findViewById(R.id.widget_frame);
            if (widgetFrame != null) {
                if (widgetLayoutResId != 0) {
                    customLayout = inflater.inflate(widgetLayoutResId, widgetFrame);
                } else {
                    widgetFrame.setVisibility(View.GONE);
                }
            }
        }
    }
}