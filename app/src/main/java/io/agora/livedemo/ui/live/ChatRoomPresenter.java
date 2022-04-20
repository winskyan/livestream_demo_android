package io.agora.livedemo.ui.live;

import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import io.agora.ChatRoomChangeListener;
import io.agora.Error;
import io.agora.MessageListener;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.CmdMessageBody;
import io.agora.chat.Conversation;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.lives.EaseLiveMessageHelper;
import io.agora.chat.uikit.lives.OnLiveMessageCallBack;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoMsgHelper;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.ThreadManager;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.ui.base.BaseActivity;
import io.agora.util.EMLog;

public class ChatRoomPresenter implements ChatRoomChangeListener, MessageListener {
    private BaseActivity mContext;
    private String chatroomId;
    private String currentUser;
    private OnChatRoomListener onChatRoomListener;
    private Conversation conversation;
    private String ownerNickname;

    public ChatRoomPresenter(BaseActivity context, String chatroomId) {
        this.mContext = context;
        this.chatroomId = chatroomId;
        currentUser = ChatClient.getInstance().getCurrentUser();
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }

    //===========================================  EMChatRoomChangeListener start =================================
    @Override
    public void onChatRoomDestroyed(String roomId, String roomName) {
        if (roomId.equals(chatroomId)) {
            mContext.finish();
        }
    }

    @Override
    public void onMemberJoined(String roomId, String participant) {
        if (onChatRoomListener != null) {
            onChatRoomListener.onChatRoomMemberAdded(participant);
        }
    }

    @Override
    public void onMemberExited(String roomId, String roomName, String participant) {
        if (onChatRoomListener != null) {
            onChatRoomListener.onChatRoomMemberExited(participant);
        }
    }

    @Override
    public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
        if (roomId.equals(chatroomId)) {
            if (currentUser.equals(participant)) {
                ChatClient.getInstance().chatroomManager().leaveChatRoom(roomId);
                mContext.finish();
            } else {
                if (onChatRoomListener != null) {
                    onChatRoomListener.onChatRoomMemberExited(participant);
                }
            }
        }
    }

    @Override
    public void onMuteListAdded(String chatRoomId, List<String> mutes, long expireTime) {
        if (mutes.contains(ChatClient.getInstance().getCurrentUser())) {
            mContext.showToast(mContext.getString(R.string.live_in_mute_list));
        }
    }

    @Override
    public void onMuteListRemoved(String chatRoomId, List<String> mutes) {
        if (mutes.contains(ChatClient.getInstance().getCurrentUser())) {
            mContext.showToast(mContext.getString(R.string.live_out_mute_list));
        }
    }

    @Override
    public void onWhiteListAdded(String chatRoomId, List<String> whitelist) {
        if (whitelist.contains(ChatClient.getInstance().getCurrentUser())) {
            mContext.showToast(mContext.getString(R.string.live_anchor_add_white));
        }
    }

    @Override
    public void onWhiteListRemoved(String chatRoomId, List<String> whitelist) {
        if (whitelist.contains(ChatClient.getInstance().getCurrentUser())) {
            mContext.showToast(mContext.getString(R.string.live_anchor_remove_from_white));
        }
    }

    @Override
    public void onAllMemberMuteStateChanged(String chatRoomId, boolean isMuted) {
        if (isMuted) {
            LiveDataBus.get().with(DemoConstants.REFRESH_ATTENTION).postValue(mContext.getString(R.string.live_anchor_mute_all_attention_tip, ownerNickname));
        } else {
            LiveDataBus.get().with(DemoConstants.REFRESH_ATTENTION).postValue("");
        }
    }

    @Override
    public void onAdminAdded(String chatRoomId, String admin) {
        EMLog.i("lives", "onAdminAdded admin=" + admin);
        onAdminChange(admin);
    }

    @Override
    public void onAdminRemoved(String chatRoomId, String admin) {
        EMLog.i("lives", "onAdminRemoved admin=" + admin);
        onAdminChange(admin);
    }

    @Override
    public void onOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
        if (TextUtils.equals(chatroomId, chatRoomId)) {
            if (onChatRoomListener != null) {
                onChatRoomListener.onChatRoomOwnerChanged(chatRoomId, newOwner, oldOwner);
            }
        }
    }

    @Override
    public void onAnnouncementChanged(String chatRoomId, String announcement) {

    }

//===========================================  EMChatRoomChangeListener end =================================

//===========================================  ChatMessageListener start =================================

    @Override
    public void onMessageReceived(List<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            String username = null;
            if (message.getChatType() == ChatMessage.ChatType.GroupChat
                    || message.getChatType() == ChatMessage.ChatType.ChatRoom) {
                username = message.getTo();
            } else {
                username = message.getFrom();
            }
            if (username.equals(chatroomId)) {
                if (onChatRoomListener != null) {
                    onChatRoomListener.onMessageReceived();
                }
            }
        }
    }

    @Override
    public void onCmdMessageReceived(List<ChatMessage> messages) {
        ChatMessage message = messages.get(messages.size() - 1);
        if (DemoConstants.CMD_GIFT.equals(((CmdMessageBody) message.getBody()).action())) {
            //showLeftGiftView(message.getFrom());
        } else if (DemoConstants.CMD_PRAISE.equals(((CmdMessageBody) message.getBody()).action())) {
            if (onChatRoomListener != null) {
                // onChatRoomListener.onReceivePraiseMsg(message.getIntAttribute(DemoConstants.EXTRA_PRAISE_COUNT, 1));
            }
        }
    }

    @Override
    public void onMessageRead(List<ChatMessage> messages) {

    }

    @Override
    public void onMessageDelivered(List<ChatMessage> messages) {

    }

    @Override
    public void onMessageRecalled(List<ChatMessage> messages) {

    }

    @Override
    public void onMessageChanged(ChatMessage message, Object change) {
        if (onChatRoomListener != null) {
            onChatRoomListener.onMessageChanged();
        }
    }

//===========================================  ChatMessageListener end =================================

    public void showMemberChangeEvent(String username, String event) {
        ChatMessage message = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
        message.setTo(chatroomId);
        message.setFrom(username);
        TextMessageBody textMessageBody = new TextMessageBody(event);
        message.addBody(textMessageBody);
        message.setChatType(ChatMessage.ChatType.ChatRoom);
        message.setAttribute("member_add", true);
        ChatClient.getInstance().chatManager().saveMessage(message);
        if (onChatRoomListener != null) {
            onChatRoomListener.onMessageSelectLast();
        }
    }

    private void onAdminChange(String admin) {
        if (onChatRoomListener != null) {
            onChatRoomListener.onAdminChanged();
        }
    }


    public void sendPraiseMessage(int praiseCount) {
        DemoMsgHelper.getInstance().sendLikeMsg(praiseCount, new OnLiveMessageCallBack() {
            @Override
            public void onSuccess(ChatMessage message) {
                Log.e("TAG", "send praise message success");
                ThreadManager.getInstance().runOnMainThread(() -> {
                    if (onChatRoomListener != null) {
                        onChatRoomListener.onMessageSelectLast();
                    }
                });
            }

            @Override
            public void onError(String messageId, int code, String error) {
                deleteMuteMsg(messageId, code);
                mContext.showToast("errorCode = " + code + "; errorMsg = " + error);
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    public void setOnChatRoomListener(OnChatRoomListener listener) {
        this.onChatRoomListener = listener;
    }

    public void sendGiftMsg(GiftBean bean, OnLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendGiftMsg(bean.getId(), bean.getNum(), new OnLiveMessageCallBack() {
            @Override
            public void onSuccess(ChatMessage message) {
                if (callBack != null) {
                    callBack.onSuccess();
                    callBack.onSuccess(message);
                }
                ThreadManager.getInstance().runOnMainThread(() -> {
                    if (onChatRoomListener != null) {
                        onChatRoomListener.onMessageSelectLast();
                    }
                });
            }

            @Override
            public void onError(String messageId, int code, String error) {
                if (callBack != null) {
                    callBack.onError(code, error);
                    callBack.onError(messageId, code, error);
                }
                deleteMuteMsg(messageId, code);
            }

            @Override
            public void onProgress(int progress, String status) {
                if (callBack != null) {
                    callBack.onProgress(progress, status);
                }
            }
        });
    }

    public void sendTxtMsg(String content, boolean isBarrageMsg, OnLiveMessageCallBack callBack) {
        DemoMsgHelper.getInstance().sendMsg(content, isBarrageMsg, new OnLiveMessageCallBack() {
            @Override
            public void onSuccess(ChatMessage message) {
                if (callBack != null) {
                    callBack.onSuccess(message);
                }
            }

            @Override
            public void onError(String messageId, int code, String error) {
                if (callBack != null) {
                    callBack.onError(messageId, code, error);
                }
                deleteMuteMsg(messageId, code);
            }

            @Override
            public void onProgress(int i, String s) {
                if (callBack != null) {
                    callBack.onProgress(i, s);
                }
            }
        });
    }

    private void deleteMuteMsg(String messageId, int code) {
        if (code == Error.USER_MUTED || code == Error.MESSAGE_ILLEGAL_WHITELIST) {
            if (conversation == null) {
                conversation = ChatClient.getInstance().chatManager().getConversation(chatroomId, Conversation.ConversationType.ChatRoom, true);
            }
            conversation.removeMessage(messageId);
        }
    }

    public interface OnChatRoomListener {
        void onChatRoomOwnerChanged(String chatRoomId, String newOwner, String oldOwner);

        void onChatRoomMemberAdded(String participant);

        void onChatRoomMemberExited(String participant);

        void onMessageReceived();

        void onMessageSelectLast();

        void onMessageChanged();

        void onAdminChanged();

    }
}
