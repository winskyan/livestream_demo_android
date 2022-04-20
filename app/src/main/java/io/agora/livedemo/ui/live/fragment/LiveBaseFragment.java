package io.agora.livedemo.ui.live.fragment;

import android.animation.ObjectAnimator;
import android.graphics.Rect;
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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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
import io.agora.chat.uikit.lives.EaseChatRoomMessagesView;
import io.agora.chat.uikit.lives.OnLiveMessageCallBack;
import io.agora.chat.uikit.lives.OnLiveMessageReceiveListener;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseImageView;
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
import io.agora.livedemo.ui.widget.ShowGiveGiftView;
import io.agora.livedemo.ui.widget.SingleBarrageView;
import io.agora.livedemo.utils.NumberUtils;
import io.agora.livedemo.utils.Utils;
import io.agora.util.EMLog;

public abstract class LiveBaseFragment extends BaseLiveFragment implements View.OnClickListener, View.OnTouchListener, ChatRoomPresenter.OnChatRoomListener, OnLiveMessageReceiveListener {
    private static final int MAX_SIZE = 10;
    protected static final String TAG = "lives";
    protected static final int CYCLE_REFRESH = 100;
    protected static final int CYCLE_REFRESH_TIME = 30000;
    @BindView(R.id.iv_icon)
    EaseImageView ivIcon;
    @BindView(R.id.message_view)
    EaseChatRoomMessagesView messageView;
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
    @BindView(R.id.img_bt_close)
    ImageView btEnd;
    @BindView(R.id.layout_attention)
    ConstraintLayout layoutAttention;
    @BindView(R.id.layout_member_num)
    ConstraintLayout layoutMemberNum;


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

    private List<String> mMemberIconList;
    protected String mAvatarUrl;
    private boolean mShowMessageListView;


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


        ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(new String[]{anchorId}, new ValueCallBack<Map<String, UserInfo>>() {
            @Override
            public void onSuccess(Map<String, UserInfo> stringUserInfoMap) {
                if (null != LiveBaseFragment.this.getActivity()) {
                    LiveBaseFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (Map.Entry<String, UserInfo> user : stringUserInfoMap.entrySet()) {
                                Log.i(TAG, "user=" + user.getKey() + ",value=" + user.getValue().getGender() + "," + user.getValue().getBirth() + "," + user.getValue().getNickname());
                                if (anchorId.equals(user.getKey())) {
                                    usernameView.setText(user.getValue().getNickname());
                                    int gender = user.getValue().getGender();
                                    if (1 == gender) {
                                        sexLayout.setBackgroundResource(R.drawable.sex_male_bg_shape);
                                        sexIcon.setImageResource(R.drawable.sex_male_icon);
                                    } else if (2 == gender) {
                                        sexLayout.setBackgroundResource(R.drawable.sex_female_bg_shape);
                                        sexIcon.setImageResource(R.drawable.sex_female_icon);
                                    } else if (3 == gender) {
                                        sexLayout.setBackgroundResource(R.drawable.sex_other_bg_shape);
                                        sexIcon.setImageResource(R.drawable.sex_other_icon);
                                    } else {
                                        sexLayout.setBackgroundResource(R.drawable.sex_secret_bg_shape);
                                        sexIcon.setImageResource(R.drawable.sex_secret_icon);
                                        ageTv.setVisibility(View.GONE);
                                    }
                                    String birth = user.getValue().getBirth();
                                    if (!TextUtils.isEmpty(birth)) {
                                        ageTv.setText(String.valueOf(Utils.getAgeByBirthday(user.getValue().getBirth())));
                                    }

                                    if (TextUtils.isEmpty(user.getValue().getNickname())) {
                                        usernameView.setText(anchorId);
                                        presenter.setOwnerNickname(anchorId);
                                    } else {
                                        usernameView.setText(user.getValue().getNickname());
                                        presenter.setOwnerNickname(user.getValue().getNickname());
                                    }

                                    mAvatarUrl = user.getValue().getAvatarUrl();
                                    updateAvatar();
                                    break;
                                }

                            }
                        }
                    });
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

    protected void updateAvatar() {

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
        layoutMemberNum.setOnClickListener(this);
        liveReceiveGift.setOnClickListener(this);
        presenter.setOnChatRoomListener(this);
        DemoMsgHelper.getInstance().setOnCustomMsgReceiveListener(this);

    }

    @Override
    protected void initData() {
        super.initData();
        mShowMessageListView = true;
        joinTime = System.currentTimeMillis();
        barrageView.initBarrage();
        mMemberIconList = new ArrayList<>(2);

        LiveDataBus.get().with(DemoConstants.SHOW_USER_DETAIL, String.class)
                .observe(getViewLifecycleOwner(), response -> {
                    if (!TextUtils.isEmpty(response)) {
                        showUserDetailsDialog(response);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_member_num:
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
        if (mShowMessageListView) {
            messageView.setShow(false);
            commentIv.setImageResource(R.drawable.live_comment);
        } else {
            messageView.setShow(true);
            commentIv.setImageResource(R.drawable.live_comment_close);
        }
        mShowMessageListView = messageView.isShowing();
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
        chatroom = ChatClient.getInstance().chatroomManager().getChatRoom(chatroomId);
    }

    private synchronized void onRoomMemberAdded(String name) {
        watchedCount++;
        if (!memberList.contains(name)) {
            membersCount++;
            if (memberList.size() >= MAX_SIZE)
                memberList.removeLast();
            memberList.add(name);
            presenter.showMemberChangeEvent(name, getString(R.string.live_msg_member_add));
            EMLog.d(TAG, name + " added");
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
        mMemberIconList.clear();
        if (memberList.size() > 2) {
            mMemberIconList.add(memberList.get(memberList.size() - 1));
            mMemberIconList.add(memberList.get(memberList.size() - 2));
        } else {
            mMemberIconList.addAll(memberList);
        }
        if (mMemberIconList.size() == 0) {
            avatarAdapter.setData(mMemberIconList);
            return;
        }
        ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(mMemberIconList.toArray(new String[0]), new ValueCallBack<Map<String, UserInfo>>() {
            @Override
            public void onSuccess(Map<String, UserInfo> stringUserInfoMap) {
                if (null != LiveBaseFragment.this.getActivity()) {
                    LiveBaseFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            avatarAdapter.setStringUserInfoMap(stringUserInfoMap);
                            avatarAdapter.setData(mMemberIconList);
                        }
                    });
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });


    }


    private synchronized void onRoomMemberExited(final String name) {
        EMLog.e(TAG, name + " exited");
        watchedCount--;
        memberList.remove(name);
        ThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                tvMemberNum.setText(String.valueOf(watchedCount));
                notifyDataSetChanged();
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
                messageView.setMessageViewListener(new EaseChatRoomMessagesView.MessageViewListener() {
                    @Override
                    public void onMessageSend(String content, boolean isBarrageMsg) {
                        presenter.sendTxtMsg(content, isBarrageMsg, new OnLiveMessageCallBack() {
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
                        String clickUsername = message.getFrom();
                        showUserDetailsDialog(clickUsername);
                    }

                    @Override
                    public void onHiderBottomBar(boolean hide) {
                        if (hide) {
                            bottomBar.setVisibility(View.GONE);
                        } else {
                            bottomBar.setVisibility(View.VISIBLE);
                        }
                    }
                });

                messageView.setShow(mShowMessageListView);
                bottomBar.setVisibility(View.VISIBLE);
                if (!chatroom.getAdminList().contains(ChatClient.getInstance().getCurrentUser())
                        && !chatroom.getOwner().equals(ChatClient.getInstance().getCurrentUser())) {
                    userManagerView.setVisibility(View.INVISIBLE);
                }
                isMessageListInited = true;
                showMemberList();
            }
        });
    }

    protected void showUserDetailsDialog(String username) {
        RoomUserDetailDialog fragment = (RoomUserDetailDialog) getChildFragmentManager().findFragmentByTag("RoomManageUserDialog");
        if (fragment == null) {
            fragment = RoomUserDetailDialog.getNewInstance(chatroomId, username);
        }
        if (fragment.isAdded()) {
            return;
        }
        fragment.show(getChildFragmentManager(), "RoomManageUserDialog");
    }

    private void showMemberList() {
        layoutManager = new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(layoutManager);
        avatarAdapter = new MemberAvatarAdapter();
        avatarAdapter.hideEmptyView(true);
        horizontalRecyclerView.setAdapter(avatarAdapter);
        horizontalRecyclerView.addItemDecoration(new MemberIconSpacesItemDecoration((int) EaseUtils.dip2px(mContext, -10)));

        avatarAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
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
        Utils.hideKeyboard(messageView);
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
        DemoMsgHelper.getInstance().removeCustomMsgListener();
    }

    private static class MemberIconSpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public MemberIconSpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view,
                                   RecyclerView parent, @NonNull RecyclerView.State state) {
            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 1) {
                outRect.left = space;
            }
        }
    }
}
