package io.agora.livedemo.ui.live.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.common.enums.LiveRoleType;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.ui.base.BaseLiveDialogFragment;
import io.agora.livedemo.ui.live.viewmodels.LivingViewModel;
import io.agora.livedemo.ui.live.viewmodels.UserManageViewModel;
import io.agora.livedemo.ui.widget.SwitchItemView;

public class RoomManageUserDialog extends BaseLiveDialogFragment implements View.OnClickListener {
    private EaseImageView userIcon;
    private TextView userNameTv;
    private ConstraintLayout sexLayout;
    private ImageView sexIcon;
    private TextView ageTv;
    private TextView roleType;
    private SwitchItemView banAll;

    private UserManageViewModel viewModel;
    protected LivingViewModel livingViewModel;
    private String username;
    private String roomId;
    private LiveRoleType liveRoleType;
    private boolean isMuted;
    private boolean inWhiteList;


    public static RoomManageUserDialog getNewInstance(String chatroomId, String username, LiveRoleType roleType) {
        RoomManageUserDialog dialog = new RoomManageUserDialog();
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        bundle.putString("chatroomid", chatroomId);
        bundle.putSerializable("liveroletype", roleType);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_dialog_live_manage_user;
    }

    @Override
    public void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if (bundle != null) {
            username = bundle.getString("username");
            roomId = bundle.getString("chatroomid");
            liveRoleType = (LiveRoleType) bundle.getSerializable("liveroletype");
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);


        userIcon = findViewById(R.id.user_icon);
        userNameTv = findViewById(R.id.tv_username);
        sexLayout = findViewById(R.id.layout_sex);
        sexIcon = findViewById(R.id.sex_icon);
        ageTv = findViewById(R.id.age_tv);
        roleType = findViewById(R.id.role_type);
        banAll = findViewById(R.id.ban_all);

        userIcon.setImageResource(DemoHelper.getAvatarResource());
        userNameTv.setText(username);

        ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(new String[]{username}, new ValueCallBack<Map<String, UserInfo>>() {
            @Override
            public void onSuccess(Map<String, UserInfo> stringUserInfoMap) {
                Objects.requireNonNull(RoomManageUserDialog.this.getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<String, UserInfo> user : stringUserInfoMap.entrySet()) {
                            if (username.equals(user.getKey())) {
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
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });

        if (liveRoleType == LiveRoleType.Streamer) {
            roleType.setText(this.getResources().getString(R.string.role_type_streamer));
            banAll.setVisibility(View.VISIBLE);
        } else if (liveRoleType == LiveRoleType.Moderator) {
            roleType.setText(this.getResources().getString(R.string.role_type_moderator));
            banAll.setVisibility(View.GONE);
        } else {
            banAll.setVisibility(View.GONE);
        }

    }

    @Override
    public void initViewModel() {
        super.initViewModel();
        viewModel = new ViewModelProvider(this).get(UserManageViewModel.class);
        livingViewModel = new ViewModelProvider(mContext).get(LivingViewModel.class);
        viewModel.getChatRoomObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<ChatRoom>() {
                @Override
                public void onSuccess(ChatRoom data) {
                    viewModel.getWhiteList(roomId);
                    viewModel.getMuteList(roomId);
                }
            });
        });
        viewModel.getWhitesObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> data) {
                    inWhiteList = data.contains(username);
                    /*tvWhite.setText(inWhiteList ?
                            getString(R.string.em_live_anchor_remove_from_white) :
                            getString(R.string.em_live_anchor_add_white));*/
                }
            });
        });
        viewModel.getMuteObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> data) {
                    isMuted = data.contains(username);
                    /*tvMute.setText(isMuted ?
                            getString(R.string.em_live_anchor_remove_mute) :
                            getString(R.string.em_live_anchor_mute));*/
                }
            });
        });

        livingViewModel.getMemberNumberObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    banAll.getSwitch().setChecked(data.isMute());
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();

                }
            });
        });
    }

    @Override
    public void initListener() {
        super.initListener();
        banAll.setOnCheckedChangeListener(new SwitchItemView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
                if (isChecked) {
                    viewModel.muteAllMembers(roomId);
                    LiveDataBus.get().with(DemoConstants.REFRESH_ATTENTION).postValue(RoomManageUserDialog.this.getResources().getString(R.string.attention_ban_all));
                } else {
                    viewModel.unMuteAllMembers(roomId);
                    LiveDataBus.get().with(DemoConstants.REFRESH_ATTENTION).postValue("");
                }
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getWhiteList(roomId);
        viewModel.getMuteList(roomId);

        livingViewModel.getRoomMemberNumber(roomId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.tv_mute:
                if (isMuted) {
                    viewModel.unMuteChatRoomMembers(roomId, getList(username));
                } else {
                    viewModel.muteChatRoomMembers(roomId, getList(username), -1);
                }
                break;
            case R.id.tv_white:
                if (inWhiteList) {
                    viewModel.removeFromChatRoomWhiteList(roomId, getList(username));
                } else {
                    viewModel.addToChatRoomWhiteList(roomId, getList(username));
                }
                break;
            case R.id.tv_cancel:
                dismiss();
                break;*/
        }
    }

    public List<String> getList(String username) {
        List<String> list = new ArrayList<>();
        list.add(username);
        return list;
    }
}
