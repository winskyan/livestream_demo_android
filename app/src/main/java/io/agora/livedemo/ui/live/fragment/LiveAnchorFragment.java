package io.agora.livedemo.ui.live.fragment;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.DemoMsgHelper;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.OnConfirmClickListener;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.common.ThreadManager;
import io.agora.livedemo.common.db.dao.ReceiveGiftDao;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.ui.live.viewmodels.LivingViewModel;
import io.agora.livedemo.ui.other.fragment.SimpleDialogFragment;
import io.agora.livedemo.utils.NumberUtils;
import io.agora.livedemo.utils.Utils;
import io.agora.util.EMLog;

public class LiveAnchorFragment extends LiveBaseFragment {
    public static final int MSG_UPDATE_COUNTDOWN = 1;
    public static final int COUNTDOWN_DELAY = 1000;
    public static final int COUNTDOWN_START_INDEX = 3;
    public static final int COUNTDOWN_END_INDEX = 1;
    @BindView(R.id.view_group)
    Group viewGroup;
    @BindView(R.id.countdown_txtv)
    TextView countdownView;
    @BindView(R.id.group_gift_info)
    Group groupGiftInfo;
    @BindView(R.id.tv_gift_num)
    TextView tvGiftNum;
    @BindView(R.id.tv_like_num)
    TextView tvLikeNum;
    @BindView(R.id.img_bt_close)
    ImageView imgBtClose;
    @BindView(R.id.layout)
    ConstraintLayout layout;
    @BindView(R.id.end_live_stream_tip)
    TextView endLiveStreamTip;

    private Unbinder unbinder;
    protected boolean isShutDownCountdown = false;
    boolean isStarted;
    private OnCameraListener cameraListener;
    private LivingViewModel viewModel;
    private boolean isOnGoing;
    private boolean reChangeLiveStatus;
    //Whether to hand over the owner to someone else, in this scenario, the operation of exiting the chat room is not performed.
    private boolean isSwitchOwnerToOther;
    private OnConfirmClickListener mStopLiveClickListener;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live_anchor;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Glide.with(this).load(DemoHelper.getAvatarUrl()).apply(RequestOptions.placeholderOf(DemoHelper.getAvatarDefaultResource())).into(ivIcon);
        viewGroup.setVisibility(View.GONE);
        btEnd.setVisibility(View.VISIBLE);
        countdownView.setTypeface(Utils.getRobotoTypeface(getActivity().getApplicationContext()));

        ReceiveGiftDao giftDao = DemoHelper.getReceiveGiftDao();
        if (giftDao != null) {
            int totalNum = giftDao.loadGiftTotalNum(DemoMsgHelper.getInstance().getCurrentRoomId());
            tvGiftNum.setText(getString(R.string.live_anchor_receive_gift_info, NumberUtils.amountConversion(totalNum)));
        } else {
            tvGiftNum.setText(getString(R.string.live_anchor_receive_gift_info, NumberUtils.amountConversion(0)));
        }

        int likeNum = DemoHelper.getLikeNum(liveId);
        tvLikeNum.setText(getString(R.string.live_anchor_like_info, NumberUtils.amountConversion(likeNum)));
    }

    @Override
    protected void initListener() {
        super.initListener();
        imgBtClose.setOnClickListener(this);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.finish();
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(LivingViewModel.class);
        LiveDataBus.get().with(DemoConstants.REFRESH_GIFT_LIST, Boolean.class)
                .observe(getViewLifecycleOwner(), response -> {
                    if (response != null && response) {
                        int totalNum = DemoHelper.getReceiveGiftDao().loadGiftTotalNum(DemoMsgHelper.getInstance().getCurrentRoomId());
                        tvGiftNum.setText(getString(R.string.live_anchor_receive_gift_info, NumberUtils.amountConversion(totalNum)));
                    }
                });

        LiveDataBus.get().with(DemoConstants.REFRESH_LIKE_NUM, Boolean.class)
                .observe(getViewLifecycleOwner(), response -> {
                    if (response != null && response) {
                        int likeNum = DemoHelper.getLikeNum(liveId);
                        tvLikeNum.setText(getString(R.string.live_anchor_like_info, NumberUtils.amountConversion(likeNum)));
                    }
                });
        LiveDataBus.get().with(DemoConstants.FINISH_LIVE, Boolean.class)
                .observe(getViewLifecycleOwner(), response -> {
                    if (response != null && response) {
                        stopLiving();
                    }
                });

        LiveDataBus.get().with(DemoConstants.REFRESH_ATTENTION, String.class)
                .observe(getViewLifecycleOwner(), response -> {
                    if (TextUtils.isEmpty(response)) {
                        layoutAttention.setVisibility(View.GONE);
                    } else {
                        layoutAttention.setVisibility(View.VISIBLE);
                        tvAttention.setText(response);
                    }
                });


        startLive();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.img_bt_close:
                showDialog(this.mStopLiveClickListener);
                break;
        }
    }

    @OnClick(R.id.switch_camera_image)
    void switchCamera() {
        //mEasyStreaming.switchCamera();
        if (cameraListener != null) {
            cameraListener.switchCamera();
        }
    }

    @Override
    protected void anchorClick() {
        super.anchorClick();
        showUserDetailsDialog(chatroom.getOwner());
    }

    @Override
    protected void onGiftClick() {
        super.onGiftClick();
        showGiftDialog();
    }

    @Override
    protected void skipToListDialog() {
        super.skipToListDialog();
        try {
            showUserList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showGiftDialog() {
        LiveGiftStatisticsDialog dialog = (LiveGiftStatisticsDialog) getChildFragmentManager().findFragmentByTag("git_statistics");
        if (dialog == null) {
            dialog = LiveGiftStatisticsDialog.getNewInstance();
        }
        if (dialog.isAdded()) {
            return;
        }
        dialog.show(getChildFragmentManager(), "git_statistics");
    }


    @Override
    public void onChatRoomOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
        super.onChatRoomOwnerChanged(chatRoomId, newOwner, oldOwner);
        EMLog.d(TAG, "onChatRoomOwnerChanged oldOwner: " + oldOwner + " newOwner: " + newOwner + " current user: " + ChatClient.getInstance().getCurrentUser());
        if (TextUtils.equals(chatroomId, chatRoomId) && !TextUtils.equals(newOwner, ChatClient.getInstance().getCurrentUser())) {
            isSwitchOwnerToOther = true;
            DemoHelper.removeTarget(chatRoomId);
            DemoHelper.removeSaveLivingId();
            if (cameraListener != null) {
                cameraListener.onRoomOwnerChangedToOtherUser(chatRoomId, newOwner);
            }
        }
    }

    @Override
    public void onAdminChanged() {

    }

    @Override
    public void handleHandlerMessage(Message msg) {
        super.handleHandlerMessage(msg);
        switch (msg.what) {
            case MSG_UPDATE_COUNTDOWN:
                handleUpdateCountdown(msg.arg1);
                break;
        }
    }

    @Override
    protected void checkLiveStatus(LiveRoom data) {
        super.checkLiveStatus(data);
        //The page is not destroyed, the live broadcast has been going on, but the live broadcast status is not "ongoing"
        if (mContext != null && !mContext.isFinishing() && isOnGoing && DemoHelper.isOwner(data.getOwner()) && !data.isLiving()) {
            restartAnchorLive();
        }
    }

    private void startLive() {
        new Thread() {
            public void run() {
                int i = COUNTDOWN_START_INDEX;
                do {
                    Message msg = Message.obtain();
                    msg.what = MSG_UPDATE_COUNTDOWN;
                    msg.arg1 = i;
                    handler.sendMessage(msg);
                    i--;
                    try {
                        Thread.sleep(COUNTDOWN_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (i >= COUNTDOWN_END_INDEX);
            }
        }.start();
    }

    void handleUpdateCountdown(final int count) {
        if (countdownView != null) {
            countdownView.setVisibility(View.VISIBLE);
            countdownView.setText(String.format("%d", count));
            ScaleAnimation scaleAnimation =
                    new ScaleAnimation(1.0f, 0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(COUNTDOWN_DELAY);
            scaleAnimation.setFillAfter(false);
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    countdownView.setVisibility(View.GONE);

                    if (count == COUNTDOWN_END_INDEX
                            && !isShutDownCountdown && mContext != null && !mContext.isFinishing()) {
                        viewGroup.setVisibility(View.VISIBLE);
                        joinChatRoom();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if (!isShutDownCountdown) {
                countdownView.startAnimation(scaleAnimation);
            } else {
                countdownView.setVisibility(View.GONE);
            }
        }
    }

    private void joinChatRoom() {
        ChatClient.getInstance()
                .chatroomManager()
                .joinChatRoom(chatroomId, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom emChatRoom) {
                        EMLog.d(TAG, "joinChatRoom success room id: " + emChatRoom.getId() + " owner: " + emChatRoom.getOwner());
                        chatroom = emChatRoom;
                        ThreadManager.getInstance().runOnMainThread(LiveAnchorFragment.this::getLiveRoomDetail);
                    }

                    @Override
                    public void onError(int i, String s) {
                        EMLog.d(TAG, "joinChatRoom fail message: " + s);
                        mContext.showToast("Go live fail");
                    }
                });
    }

    private void getLiveRoomDetail() {
        viewModel.getRoomDetailObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    //需要保证聊天室和直播间的主播均不是当前用户
                    if (data.isLiving() && (!DemoHelper.isOwner(chatroom.getOwner()) && !DemoHelper.isOwner(data.getOwner()))) {
                        EMLog.d(TAG, "getLiveRoomDetails is living owner: " + chatroom.getOwner());
                        mContext.showToast(getString(R.string.live_list_warning));
                        exitRoom();
                    } else {
                        EMLog.d(TAG, "getLiveRoomDetails start livestream");
                        LiveAnchorFragment.this.liveRoom = data;
                        changeAnchorLive();
                    }

                }
            });
        });
        viewModel.getLiveRoomDetails(liveId);
    }

    private void exitRoom() {
        DemoHelper.removeTarget(liveId);
        DemoHelper.removeSaveLivingId();
        mContext.finish();
    }

    private void changeAnchorLive() {
        reChangeLiveStatus = false;
        changeAnchorLiveByServer();
    }

    private void restartAnchorLive() {
        EMLog.d(TAG, "restartAnchorLive");
        reChangeLiveStatus = true;
        changeAnchorLiveByServer();
    }

    private void changeAnchorLiveByServer() {
        viewModel.getChangeObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    EMLog.d(TAG, "changeLiveStatus success");
                    LiveDataBus.get().with(DemoConstants.FRESH_LIVE_LIST).setValue(true);
                    if (!reChangeLiveStatus) {
                        //开始直播，则开始统计点赞及礼物统计，实际开发中，应该由服务器进行统计，此处仅为展示用
                        DemoHelper.saveLikeNum(data.getId(), 0);
                        if (null != DemoHelper.getReceiveGiftDao()) {
                            DemoHelper.getReceiveGiftDao().clearData(DemoMsgHelper.getInstance().getCurrentRoomId());
                        }
                        startAnchorLive(liveRoom);
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    exitRoom();
                }
            });
        });
        if (liveRoom.isLiving() && !reChangeLiveStatus) {
            startAnchorLive(liveRoom);
        } else {
            viewModel.changeLiveStatus(liveId, ChatClient.getInstance().getCurrentUser(), "ongoing");
        }
    }

    private void startAnchorLive(LiveRoom liveRoom) {
        isOnGoing = true;
        DemoHelper.saveLivingId(liveRoom.getId());
        addChatRoomChangeListener();
        onMessageListInit();
        mContext.showToast("live stream begin");
        if (cameraListener != null) {
            cameraListener.onStartCamera();
        }
        startCycleRefresh();
    }

    private void showDialog(OnConfirmClickListener listener) {
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(R.string.live_dialog_quit_title)
                .setConfirmButtonTxt(R.string.live_dialog_quit_btn_title)
                .setConfirmColor(R.color.em_color_warning)
                .setOnConfirmClickListener(new OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, Object bean) {
                        stopLiving();
                        if (listener != null) {
                            listener.onConfirmClick(view, bean);
                        }
                    }
                })
                .build()
                .show(getChildFragmentManager(), "dialog");
    }

    private void stopLiving() {
        if (cameraListener != null) {
            cameraListener.onStopCamera();
        }
        if (isOnGoing) {
            isOnGoing = false;
            leaveRoom();
        }
    }

    private void leaveRoom() {
        viewModel.getCloseObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    try {
                        ChatClient.getInstance().chatroomManager().leaveChatRoom(chatroomId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    DemoHelper.removeTarget(data.getId());
                    DemoHelper.removeSaveLivingId();
                    if (DemoHelper.getReceiveGiftDao() != null) {
                        DemoHelper.getReceiveGiftDao().clearData(DemoMsgHelper.getInstance().getCurrentRoomId());
                    }
                    DemoHelper.saveLikeNum(data.getId(), 0);
                    endLiveStream();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    mContext.finish();
                }
            });
        });
        viewModel.closeLive(liveId, ChatClient.getInstance().getCurrentUser());
    }

    public void setOnCameraListener(OnCameraListener listener) {
        this.cameraListener = listener;
    }

    private void endLiveStream() {
        closeIv.setVisibility(View.VISIBLE);
        toolbarGroupView.setVisibility(View.INVISIBLE);
        messageView.setVisibility(View.GONE);

        imgBtClose.setVisibility(View.GONE);
        switchCameraView.setEnabled(false);
        commentIv.setEnabled(false);
        endLiveStreamTip.setVisibility(View.VISIBLE);
        layout.setBackgroundResource(R.color.translucent_bg);
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
            if (isMessageListInited && !isSwitchOwnerToOther) {
                ChatClient.getInstance().chatroomManager().leaveChatRoom(chatroomId);
                isMessageListInited = false;
                EMLog.d(TAG, "leave chat room id: " + chatroomId);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (presenter != null) {
            ChatClient.getInstance()
                    .chatroomManager()
                    .removeChatRoomListener(presenter);
        }

    }

    @Override
    public void onBackPressed() {
        if (isOnGoing) {
            showDialog(this.mStopLiveClickListener);
        } else {
            mContext.onBackPressed();
        }
    }

    public void setOnStopLiveClickListener(OnConfirmClickListener onStopLiveClickListener) {
        this.mStopLiveClickListener = onStopLiveClickListener;
    }

    public interface OnCameraListener {
        void onStartCamera();

        void switchCamera();

        void onStopCamera();

        void onRoomOwnerChangedToOtherUser(String chatRoomId, String newOwner);
    }
}
