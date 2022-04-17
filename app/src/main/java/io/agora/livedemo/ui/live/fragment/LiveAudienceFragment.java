package io.agora.livedemo.ui.live.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.agora.Error;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.uikit.lives.OnLiveMessageCallBack;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.OnConfirmClickListener;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.common.ThreadManager;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.util.EMLog;

public class LiveAudienceFragment extends LiveBaseFragment {
    @BindView(R.id.loading_layout)
    RelativeLayout loadingLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.loading_text)
    TextView loadingText;
    @BindView(R.id.cover_image)
    ImageView coverView;
    @BindView(R.id.group_ui)
    ConstraintLayout groupUi;
    @BindView(R.id.live_stream_end_tip)
    TextView liveStreamEndTip;

    private Unbinder unbinder;
    private OnLiveListener liveListener;
    int praiseCount;
    final int praiseSendDelay = 4 * 1000;
    private Thread sendPraiseThread;
    /**
     * Whether it is an operation of switching the owner, if it is an operation of switching the owner, the logic of exiting the chat room will not be called. Prevent the new page from joining the live broadcast room, and the page being destroyed calls the operation of exiting the live broadcast room, resulting in an exception in the chat room.
     */
    private boolean isSwitchOwner;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live_audience;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        switchCameraView.setVisibility(View.GONE);
        closeIv.setVisibility(View.VISIBLE);
        liveReceiveGift.setVisibility(View.VISIBLE);
        Glide.with(mContext).load(liveRoom.getCover()).placeholder(R.color.placeholder).into(coverView);
    }

    @Override
    protected void updateAvatar() {
        super.updateAvatar();
        Glide.with(this).load(mAvatarUrl).apply(RequestOptions.placeholderOf(R.drawable.avatar_default)).into(ivIcon);
    }

    @Override
    protected void initListener() {
        super.initListener();
        tvAttention.setOnClickListener(this);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveRoom();
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        EaseUserUtils.showUserAvatar(mContext, liveRoom.getOwner(), ivIcon);
        LiveDataBus.get().with(DemoConstants.REFRESH_ATTENTION, String.class)
                .observe(getViewLifecycleOwner(), response -> {
                    if (TextUtils.isEmpty(response)) {
                        layoutAttention.setVisibility(View.GONE);
                    } else {
                        layoutAttention.setVisibility(View.VISIBLE);
                        tvAttention.setText(response);
                    }
                });

        LiveDataBus.get().with(DemoConstants.EVENT_ANCHOR_FINISH_LIVE, Boolean.class).observe(mContext, event -> {
            if (liveRoom != null
                    && !TextUtils.isEmpty(liveRoom.getVideo_type())
                    && !DemoHelper.isVod(liveRoom.getVideo_type())) {
                liveStreamEndTip.setVisibility(View.VISIBLE);
                liveReceiveGift.setEnabled(false);
                liveReceiveGift.setImageResource(R.drawable.live_gift_disable);
                commentIv.setEnabled(false);
            }
        });

        getLiveRoomDetail();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_attention:

                break;
        }
    }

    @OnClick(R.id.img_bt_close)
    void close() {
        mContext.finish();
    }

    @OnClick(R.id.like_image)
    void Praise() {
        periscopeLayout.addHeart();
        synchronized (this) {
            ++praiseCount;
        }
        if (sendPraiseThread == null) {
            sendPraiseThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mContext != null && !mContext.isFinishing()) {
                        int count = 0;
                        synchronized (LiveAudienceFragment.this) {
                            count = praiseCount;
                            praiseCount = 0;
                        }
                        if (count > 0) {
                            presenter.sendPraiseMessage(count);
                        }
                        try {
                            Thread.sleep(praiseSendDelay + new Random().nextInt(2000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            });
            sendPraiseThread.setDaemon(true);
            sendPraiseThread.start();
        }
    }

    @Override
    protected void skipToListDialog() {
        super.skipToListDialog();
        if (chatroom.getAdminList().contains(ChatClient.getInstance().getCurrentUser())) {
            try {
                showUserList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LiveMemberListDialog dialog = (LiveMemberListDialog) getChildFragmentManager().findFragmentByTag("liveMember");
            if (dialog == null) {
                dialog = LiveMemberListDialog.getNewInstance(chatroomId);
            }
            if (dialog.isAdded()) {
                return;
            }
            dialog.show(getChildFragmentManager(), "liveMember");
        }
    }

    @Override
    protected void anchorClick() {
        super.anchorClick();
        //showUserDetailsDialog(chatroom.getOwner());
    }

    @Override
    protected void onGiftClick() {
        super.onGiftClick();
        showGiftDialog();
    }

    @Override
    protected void showPraise(int count) {
        //The audience does not show animations
    }

    @Override
    public void onChatRoomOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
        super.onChatRoomOwnerChanged(chatRoomId, newOwner, oldOwner);
        if (TextUtils.equals(liveRoom.getId(), chatRoomId) && TextUtils.equals(newOwner, ChatClient.getInstance().getCurrentUser())) {
            isSwitchOwner = true;
            if (liveListener != null) {
                liveListener.onRoomOwnerChangedToCurrentUser(chatRoomId, newOwner);
            }
        }
    }

    @Override
    public void onAdminChanged() {
        chatroom = ChatClient.getInstance().chatroomManager().getChatRoom(chatroomId);
    }

    private void showGiftDialog() {
        LiveGiftDialog dialog = (LiveGiftDialog) getChildFragmentManager().findFragmentByTag("live_gift");
        if (dialog == null) {
            dialog = LiveGiftDialog.getNewInstance();
        }
        if (dialog.isAdded()) {
            return;
        }
        dialog.show(getChildFragmentManager(), "live_gift");
        dialog.setOnConfirmClickListener(new OnConfirmClickListener() {
            @Override
            public void onConfirmClick(View view, Object bean) {
                if (bean instanceof GiftBean) {
                    presenter.sendGiftMsg((GiftBean) bean, new OnLiveMessageCallBack() {
                        @Override
                        public void onSuccess(ChatMessage message) {
                            ThreadManager.getInstance().runOnMainThread(() -> {
                                barrageLayout.showGift((GiftBean) bean);
                            });
                        }
                    });

                }
            }
        });
    }

    private void getLiveRoomDetail() {
        viewModel.getRoomDetailObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    //If the current user is the host, enter the host room
                    if (DemoHelper.isOwner(data.getOwner())) {
                        isSwitchOwner = true;
                        if (liveListener != null) {
                            liveListener.onRoomOwnerChangedToCurrentUser(data.getChatroomId(), data.getOwner());
                        }
                        return;
                    }
                    LiveAudienceFragment.this.liveRoom = data;
                    if (DemoHelper.isLiving(data.getStatus())) {
                        if (liveListener != null) {
                            liveListener.onLiveOngoing(data);
                        }
                        messageView.getInputView().requestFocus();
                        messageView.getInputView().requestFocusFromTouch();
                        joinChatRoom();

                    } else {
                        mContext.showLongToast("Live stream End");
                        if (liveListener != null) {
                            liveListener.onLiveClosed();
                        }
                    }
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    loadingLayout.setVisibility(View.INVISIBLE);
                }
            });
        });
        viewModel.getLiveRoomDetails(liveRoom.getId());
    }

    private void joinChatRoom() {
        ChatClient.getInstance()
                .chatroomManager()
                .joinChatRoom(chatroomId, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom emChatRoom) {
                        EMLog.d(TAG, "audience join chat room success");
                        chatroom = emChatRoom;
                        addChatRoomChangeListener();
                        onMessageListInit();
                        startCycleRefresh();
                    }

                    @Override
                    public void onError(int i, String s) {
                        EMLog.d(TAG, "audience join chat room fail message: " + s);
                        if (i == Error.GROUP_PERMISSION_DENIED || i == Error.CHATROOM_PERMISSION_DENIED) {
                            mContext.showLongToast("You do not have permission to join this room");
                            mContext.finish();
                        } else if (i == Error.CHATROOM_MEMBERS_FULL) {
                            mContext.showLongToast("Room is full");
                            mContext.finish();
                        } else {
                            mContext.showLongToast("Failed to join chat room: " + s);
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMessageListInited) messageView.refresh();
        // register the event listener when enter the foreground
        ChatClient.getInstance().chatManager().addMessageListener(presenter);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the
        // background
        ChatClient.getInstance().chatManager().removeMessageListener(presenter);

        if (mContext.isFinishing()) {
            LiveDataBus.get().with(DemoConstants.FRESH_LIVE_LIST).setValue(true);
            if (isMessageListInited && !isSwitchOwner) {
                leaveRoom();
                //postUserChangeEvent(StatisticsType.LEAVE, ChatClient.getInstance().getCurrentUser());
            }
        }
    }

    private void leaveRoom() {
        ChatClient.getInstance().chatroomManager().leaveChatRoom(chatroomId);
        isMessageListInited = false;
        EMLog.d(TAG, "audience leave chat room");
        mContext.finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void setOnLiveListener(OnLiveListener liveListener) {
        this.liveListener = liveListener;
    }

    public interface OnLiveListener {
        void onLiveOngoing(LiveRoom data);

        void onLiveClosed();

        void onRoomOwnerChangedToCurrentUser(String chatRoomId, String newOwner);
    }
}
