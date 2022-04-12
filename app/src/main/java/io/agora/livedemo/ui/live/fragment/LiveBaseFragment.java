package io.agora.livedemo.ui.live.fragment;

import android.animation.ObjectAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.custommessage.OnCustomMsgReceiveListener;
import io.agora.custommessage.OnMsgCallBack;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.DemoMsgHelper;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.common.ThreadManager;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.data.model.User;
import io.agora.livedemo.ui.base.BaseLiveFragment;
import io.agora.livedemo.ui.live.ChatRoomPresenter;
import io.agora.livedemo.ui.live.adapter.MemberAvatarAdapter;
import io.agora.livedemo.ui.live.viewmodels.LivingViewModel;
import io.agora.livedemo.ui.live.viewmodels.UserManageViewModel;
import io.agora.livedemo.ui.widget.PeriscopeLayout;
import io.agora.livedemo.ui.widget.RoomMessagesView;
import io.agora.livedemo.ui.widget.ShowGiveGiftView;
import io.agora.livedemo.ui.widget.SingleBarrageView;
import io.agora.livedemo.utils.NumberUtils;
import io.agora.livedemo.utils.Utils;
import io.agora.util.EMLog;

public abstract class LiveBaseFragment extends BaseLiveFragment implements View.OnClickListener, View.OnTouchListener, ChatRoomPresenter.OnChatRoomListener, OnCustomMsgReceiveListener {
    private static final int MAX_SIZE = 10;
    protected static final String TAG = "lives";
    protected static final int CYCLE_REFRESH = 100;
    protected static final int CYCLE_REFRESH_TIME = 30000;
    @BindView(R.id.iv_icon)
    EaseImageView ivIcon;
    @BindView(R.id.message_view)
    RoomMessagesView messageView;
    @BindView(R.id.periscope_layout)
    PeriscopeLayout periscopeLayout;
    @BindView(R.id.bottom_bar)
    View bottomBar;
    @BindView(R.id.show_gift_view)
    ShowGiveGiftView barrageLayout;
    @BindView(R.id.horizontal_recycle_view)
    RecyclerView horizontalRecyclerView;
    //@BindView(R.id.new_messages_warn) ImageView newMsgNotifyImage;

    @BindView(R.id.user_manager_image)
    ImageView userManagerView;
    @BindView(R.id.switch_camera_image)
    ImageView switchCameraView;
    @BindView(R.id.like_image)
    ImageView likeImageView;
    @BindView(R.id.tv_username)
    TextView usernameView;
    @BindView(R.id.tv_member_num)
    TextView tvMemberNum;
    @BindView(R.id.tv_attention)
    TextView tvAttention;
    @BindView(R.id.toolbar)
    ViewGroup toolbar;
    @BindView(R.id.live_receive_gift)
    ImageView liveReceiveGift;
    @BindView(R.id.barrageView)
    SingleBarrageView barrageView;
    @BindView(R.id.layout_sex)
    ConstraintLayout sexLayout;
    @BindView(R.id.sex_icon)
    ImageView sexIcon;
    @BindView(R.id.age_tv)
    TextView ageTv;
    @BindView(R.id.group_toolbar_info)
    Group toolbarGroupView;
    @BindView(R.id.close_iv)
    ImageView closeIv;
    @BindView(R.id.comment_image)
    ImageView commentIv;
    @BindView(R.id.layout_attention)
    ConstraintLayout layoutAttention;

    protected LiveRoom liveRoom;
    protected ChatRoom chatroom;
    protected String chatroomId = "";
    protected String liveId = "";
    protected String anchorId;
    protected int watchedCount;
    protected LinkedList<String> memberList = new LinkedList<>();
    protected int membersCount;
    private LinearLayoutManager layoutManager;
    private MemberAvatarAdapter avatarAdapter;
    protected boolean isMessageListInited;
    protected ChatRoomPresenter presenter;
    protected LivingViewModel viewModel;
    private UserManageViewModel userManageViewModel;
    private long joinTime;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleHandlerMessage(msg);
        }
    };
    private boolean isStartCycleRefresh;


    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if (bundle != null) {
            liveRoom = (LiveRoom) bundle.getSerializable("liveroom");
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Log.i(TAG, "live room=" + liveRoom);
        liveId = liveRoom.getId();
        chatroomId = liveRoom.getId();
        anchorId = liveRoom.getOwner();
        DemoMsgHelper.getInstance().init(chatroomId);

        usernameView.setText(anchorId);

        ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(new String[]{anchorId}, new ValueCallBack<Map<String, UserInfo>>() {
            @Override
            public void onSuccess(Map<String, UserInfo> stringUserInfoMap) {
                for (Map.Entry<String, UserInfo> user : stringUserInfoMap.entrySet()) {
                    Log.i(TAG, "user=" + user.getKey() + ",value=" + user.getValue().getGender() + "," + user.getValue().getBirth());
                    if (anchorId.equals(user.getKey())) {
                        int gender = user.getValue().getGender();
                        if (1 == gender) {
                            sexLayout.setBackgroundResource(R.drawable.sex_male_bg_shape);
                            sexIcon.setImageResource(R.drawable.sex_male_icon);
                        } else if (2 == gender) {
                            sexLayout.setBackgroundResource(R.drawable.sex_female_bg_shape);
                            sexIcon.setImageResource(R.drawable.sex_female_icon);
                        } else {
                            sexLayout.setBackgroundResource(R.drawable.sex_other_bg_shape);
                            sexIcon.setImageResource(R.drawable.sex_other_icon);
                        }
                        String birth = user.getValue().getBirth();
                        if (!TextUtils.isEmpty(birth)) {
                            ageTv.setText(String.valueOf(user.getValue().getBirth()));
                        }

                    }
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });

        watchedCount = liveRoom.getAudienceNum();
        tvMemberNum.setText(NumberUtils.amountConversion(watchedCount));

        presenter = new ChatRoomPresenter(mContext, chatroomId);
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        viewModel = new ViewModelProvider(mContext).get(LivingViewModel.class);
        userManageViewModel = new ViewModelProvider(this).get(UserManageViewModel.class);
        LiveDataBus.get().with(DemoConstants.REFRESH_MEMBER_COUNT, Boolean.class).observe(getViewLifecycleOwner(), event -> {
            if (event != null && event) {
                viewModel.getRoomMemberNumber(chatroomId);
            }
        });
        viewModel.getMemberNumberObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    liveRoom = data;
                    handler.removeMessages(CYCLE_REFRESH);
                    handler.sendEmptyMessageDelayed(CYCLE_REFRESH, CYCLE_REFRESH_TIME);
                    LiveDataBus.get().with(DemoConstants.LIVING_STATUS).postValue(data.getStatus());
                    onRoomMemberChange(data);
                    checkLiveStatus(data);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    handler.removeMessages(CYCLE_REFRESH);
                    handler.sendEmptyMessageDelayed(CYCLE_REFRESH, CYCLE_REFRESH_TIME);
                }
            });
        });
    }


    protected void checkLiveStatus(LiveRoom data) {

    }

    @Override
    protected void initListener() {
        super.initListener();
        tvMemberNum.setOnClickListener(this);
        liveReceiveGift.setOnClickListener(this);
        getView().setOnTouchListener(this);
        presenter.setOnChatRoomListener(this);
        DemoMsgHelper.getInstance().setOnCustomMsgReceiveListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        joinTime = System.currentTimeMillis();
        barrageView.initBarrage();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_member_num:
                skipToListDialog();
                break;
            case R.id.live_receive_gift:
                onGiftClick();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isStartCycleRefresh) {
            startCycleRefresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isStartCycleRefresh) {
            stopCycleRefresh();
        }
    }

    protected void startCycleRefresh() {
        if (handler != null) {
            handler.removeMessages(CYCLE_REFRESH);
            handler.sendEmptyMessageDelayed(CYCLE_REFRESH, CYCLE_REFRESH_TIME);
        }
    }

    protected void stopCycleRefresh() {
        if (handler != null) {
            handler.removeMessages(CYCLE_REFRESH);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mContext != null && mContext.isFinishing()) {
            handler.removeCallbacksAndMessages(null);
            isStartCycleRefresh = false;
        }
    }


    public void handleHandlerMessage(Message msg) {
        switch (msg.what) {
            case CYCLE_REFRESH:
                if (!TextUtils.isEmpty(chatroomId)) {
                    isStartCycleRefresh = true;
                    viewModel.getRoomMemberNumber(chatroomId);
                }
                break;
        }
    }

    @OnClick(R.id.comment_image)
    void onCommentImageClick() {
        showInputView();
    }

    private void showInputView() {
        bottomBar.setVisibility(View.INVISIBLE);
        messageView.setShowInputView(true);
        messageView.getInputView().requestFocus();
        messageView.getInputView().requestFocusFromTouch();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.showKeyboard(messageView.getInputView());
            }
        }, 200);
    }


    protected void onGiftClick() {
    }

    @OnClick(R.id.layout_anchor)
    protected void anchorClick() {
    }

    protected void skipToListDialog() {
    }

    protected void showUserList() {
        LiveDataBus.get().with(DemoConstants.REFRESH_MEMBER_COUNT).postValue(true);
        RoomUserManagementDialog dialog = (RoomUserManagementDialog) getChildFragmentManager().findFragmentByTag("RoomUserManagementDialog");
        if (dialog == null) {
            dialog = new RoomUserManagementDialog(chatroomId);
        }
        if (dialog.isAdded()) {
            return;
        }
        dialog.show(getChildFragmentManager(), "RoomUserManagementDialog");
    }

    protected void showPraise(final int count) {
        requireActivity().runOnUiThread(() -> {
            for (int i = 0; i < count; i++) {
                if (!mContext.isFinishing())
                    periscopeLayout.addHeart();
            }
        });
    }

    /**
     * add chat room change listener
     */
    protected void addChatRoomChangeListener() {
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(presenter);
    }

    private synchronized void onRoomMemberChange(LiveRoom room) {
        watchedCount = room.getAudienceNum();
        memberList = room.getMemberList(MAX_SIZE);
        ThreadManager.getInstance().runOnMainThread(() -> {
            tvMemberNum.setText(NumberUtils.amountConversion(watchedCount));
            notifyDataSetChanged();
        });
    }

    private synchronized void onRoomMemberAdded(String name) {
        watchedCount++;
        if (!memberList.contains(name)) {
            membersCount++;
            if (memberList.size() >= MAX_SIZE)
                memberList.removeLast();
            memberList.addFirst(name);
            presenter.showMemberChangeEvent(name, getString(R.string.em_live_msg_member_add));
            EMLog.d(TAG, name + "added");
            ThreadManager.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    tvMemberNum.setText(String.valueOf(watchedCount));
                    if (name.equals(chatroom.getOwner())) {
                        LiveDataBus.get().with(DemoConstants.EVENT_ANCHOR_JOIN).setValue(true);
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }

    private void notifyDataSetChanged() {
        if (memberList.size() > 6) {
            layoutManager.setStackFromEnd(false);
        } else {
            layoutManager.setStackFromEnd(true);
        }
        avatarAdapter.setData(memberList);
    }


    private synchronized void onRoomMemberExited(final String name) {
        EMLog.e(TAG, name + "exited");
        if (memberList.contains(name)) {
            memberList.remove(name);
        }
        ThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (name.equals(chatroom.getOwner())) {
                    LiveDataBus.get().with(DemoConstants.EVENT_ANCHOR_FINISH_LIVE).setValue(true);
                    LiveDataBus.get().with(DemoConstants.FRESH_LIVE_LIST).setValue(true);
                }
            }
        });
    }

    protected void onMessageListInit() {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageView.init(chatroomId);
                messageView.setMessageViewListener(new RoomMessagesView.MessageViewListener() {
                    @Override
                    public void onMessageSend(String content, boolean isBarrageMsg) {
                        presenter.sendTxtMsg(content, isBarrageMsg, new OnMsgCallBack() {
                            @Override
                            public void onSuccess(ChatMessage message) {
                                messageView.refreshSelectLast();

                                if (isBarrageMsg) {
                                    barrageView.addData(message);
                                }
                            }
                        });
                    }

                    @Override
                    public void onItemClickListener(final ChatMessage message) {
                        //if(message.getFrom().equals(ChatClient.getInstance().getCurrentUser())){
                        //    return;
                        //}
                        String clickUsername = message.getFrom();
                        showUserDetailsDialog(clickUsername);
                    }

                    @Override
                    public void onHiderBottomBar() {
                        bottomBar.setVisibility(View.VISIBLE);
                    }
                });
                messageView.setVisibility(View.VISIBLE);
                bottomBar.setVisibility(View.VISIBLE);
                if (!chatroom.getAdminList().contains(ChatClient.getInstance().getCurrentUser())
                        && !chatroom.getOwner().equals(ChatClient.getInstance().getCurrentUser())) {
                    userManagerView.setVisibility(View.INVISIBLE);
                }
                isMessageListInited = true;
                updateUnreadMsgView();
                showMemberList();
            }
        });
    }

    protected void showUserDetailsDialog(String username) {
        RoomUserDetailsDialog dialog = (RoomUserDetailsDialog) getChildFragmentManager().findFragmentByTag("RoomUserDetailsDialog");
        if (dialog == null) {
            dialog = RoomUserDetailsDialog.newInstance(username, liveRoom);
        }
        if (dialog.isAdded()) {
            return;
        }
        dialog.show(getChildFragmentManager(), "RoomUserDetailsDialog");
        dialog.setManageEventListener(new RoomUserDetailsDialog.RoomManageEventListener() {
            @Override
            public void onKickMember(String username) {
                onRoomMemberExited(username);
            }

            @Override
            public void onAddBlacklist(String username) {
                onRoomMemberExited(username);
            }
        });
    }

    public void updateUnreadMsgView() {
        //if(isMessageListInited) {
        //  for (Conversation conversation : ChatClient.getInstance()
        //      .chatManager()
        //      .getAllConversations()
        //      .values()) {
        //    if (conversation.getType() == Conversation.ConversationType.Chat
        //        && conversation.getUnreadMsgCount() > 0) {
        //      newMsgNotifyImage.setVisibility(View.VISIBLE);
        //      return;
        //    }
        //  }
        //  newMsgNotifyImage.setVisibility(View.INVISIBLE);
        //}
    }

    private void showMemberList() {
        horizontalRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(layoutManager);
        avatarAdapter = new MemberAvatarAdapter();
        horizontalRecyclerView.setAdapter(avatarAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize((int) EaseUtils.dip2px(mContext, 5), 0);
        decoration.setDrawable(drawable);
        horizontalRecyclerView.addItemDecoration(decoration);
        avatarAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String item = avatarAdapter.getItem(position);
                skipToListDialog();
            }
        });

        userManageViewModel.getObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> data) {
                    boolean haveOwner = data.contains(chatroom.getOwner());
                    memberList.clear();
                    if (haveOwner) {
                        data.remove(chatroom.getOwner());
                    }
                    if (data.size() > MAX_SIZE) {
                        for (int i = 0; i < MAX_SIZE; i++) {
                            memberList.add(i, data.get(i));
                        }
                    } else {
                        memberList.addAll(data);
                    }
                    int size = chatroom.getMemberCount();
                    if (haveOwner) {
                        size--;
                    }
                    if (size < data.size()) {
                        size = data.size();
                    }
                    membersCount = size;
                    watchedCount = membersCount;
                    tvMemberNum.setText(NumberUtils.amountConversion(watchedCount));
                    notifyDataSetChanged();
                }
            });
        });
        userManageViewModel.getMembers(chatroomId);
    }

    private float preX, preY;

    protected void slideToLeft(int startX, float endX) {
//        startAnimation(getView(), startX, endX);
    }


    protected void slideToRight(float startX, float endX) {
//        startAnimation(getView(), startX, endX);
    }

    protected void startAnimation(View target, float startX, float endX) {
        if (target == null) {
            return;
        }
        if (target instanceof ViewGroup) {
            float x = ((ViewGroup) target).getChildAt(0).getX();
            if (x != startX) {
                return;
            }
            int childCount = ((ViewGroup) target).getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    View child = ((ViewGroup) target).getChildAt(i);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(child, "translationX", startX, endX);
                    animator.setDuration(500);
                    animator.start();
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = ev.getX();
                preY = ev.getY();
                hideSoftKeyBoard();
                break;
            case MotionEvent.ACTION_MOVE:
                float curX = ev.getX();
                float curY = ev.getY();
                float x = curX - preX;
                float y = curY - preY;
                if (Math.abs(x) > Math.abs(y) && Math.abs(x) > 20) {
                    float[] screenInfo = EaseUtils.getScreenInfo(mContext);
                    if (x > 0) {
                        slideToLeft(0, screenInfo[0]);
                    } else {
                        slideToRight(screenInfo[0], 0);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }

    private void hideSoftKeyBoard() {
        messageView.hideSoftKeyBoard();
    }

    @Override
    public void onChatRoomOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
        // owner changed
    }

    @Override
    public void onChatRoomMemberAdded(String participant) {
        LiveDataBus.get().with(DemoConstants.REFRESH_MEMBER).postValue(true);
        onRoomMemberAdded(participant);
    }

    @Override
    public void onChatRoomMemberExited(String participant) {
        LiveDataBus.get().with(DemoConstants.REFRESH_MEMBER).postValue(true);
        onRoomMemberExited(participant);
    }

    @Override
    public void onMessageReceived() {
        messageView.refreshSelectLast();
    }

    @Override
    public void onMessageSelectLast() {
        if (mContext != null && !mContext.isFinishing()) {
            mContext.runOnUiThread(() -> messageView.refreshSelectLast());
        }
    }

    @Override
    public void onMessageChanged() {
        if (isMessageListInited) {
            messageView.refresh();
        }
    }

    @Override
    public void onReceiveGiftMsg(ChatMessage message) {
        if (message.getMsgTime() >= joinTime) {
            DemoHelper.saveGiftInfo(message);
        }
        if (message.getMsgTime() < joinTime - 2000) {
            return;
        }
        String giftId = DemoMsgHelper.getInstance().getMsgGiftId(message);
        if (TextUtils.isEmpty(giftId)) {
            return;
        }
        GiftBean bean = DemoHelper.getGiftById(giftId);
        if (bean == null) {
            return;
        }
        User user = new User();
        user.setNickName(message.getFrom());
        bean.setUser(user);
        bean.setNum(DemoMsgHelper.getInstance().getMsgGiftNum(message));
        ThreadManager.getInstance().runOnMainThread(() -> {
            barrageLayout.showGift(bean);
        });
    }

    @Override
    public void onReceivePraiseMsg(ChatMessage message) {
        if (message.getMsgTime() >= joinTime) {
            DemoHelper.saveLikeInfo(message);
        }
        int likeNum = DemoMsgHelper.getInstance().getMsgPraiseNum(message);
        if (likeNum <= 0) {
            return;
        }
        showPraise(likeNum);
    }


    @Override
    public void onReceiveBarrageMsg(ChatMessage message) {
        ThreadManager.getInstance().runOnMainThread(() -> {
            barrageView.addData(message);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (barrageLayout != null) {
            barrageLayout.destroy();
        }
        RoomUserDetailsDialog.sAttentionClicked = false;
        DemoMsgHelper.getInstance().removeCustomMsgLisenter();
    }
}
