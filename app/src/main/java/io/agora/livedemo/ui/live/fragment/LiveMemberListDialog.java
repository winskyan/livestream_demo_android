package io.agora.livedemo.ui.live.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chat.ChatRoomManager;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.ui.base.BaseLiveDialogFragment;
import io.agora.livedemo.ui.live.viewmodels.UserManageViewModel;
import io.agora.livedemo.utils.StatusBarCompat;


public class LiveMemberListDialog extends BaseLiveDialogFragment {
    private RecyclerView mUserListView;
    private UserListAdapter mUserListAdapter;
    private String chatRoomId;
    private UserManageViewModel viewModel;

    protected ChatRoomManager mChatRoomManager;
    protected ChatRoom mChatRoom;

    private List<String> mUserListData;
    private List<String> mMuteListData;
    private List<String> mAdminListData;

    public static LiveMemberListDialog getNewInstance(String chatRoomId) {
        LiveMemberListDialog dialog = new LiveMemberListDialog();
        Bundle bundle = new Bundle();
        bundle.putString("chatRoomId", chatRoomId);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_live_member_list;
    }

    @Override
    public void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if (bundle != null) {
            chatRoomId = bundle.getString("chatRoomId");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            Window dialogWindow = getDialog().getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            final float screenHeight = EaseUtils.getScreenInfo(mContext)[1];
            final int navBarHeight = StatusBarCompat.getNavigationBarHeight(mContext);
            lp.height = (int) screenHeight * 2 / 5 + navBarHeight;
            dialogWindow.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);

        mUserListView = findViewById(R.id.rv_user_list);

        mUserListView.setLayoutManager(new LinearLayoutManager(mContext));

        mUserListAdapter = new UserListAdapter();
        mUserListAdapter.hideEmptyView(true);
        mUserListView.setAdapter(mUserListAdapter);
        mUserListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                LiveMemberListDialog.this.dismiss();
                LiveDataBus.get().with(DemoConstants.SHOW_USER_DETAIL).postValue(mUserListAdapter.getItem(position));
            }
        });

        mUserListView.addItemDecoration(new UserListSpacesItemDecoration((int) EaseUtils.dip2px(mContext, 20)));
    }

    @Override
    public void initViewModel() {
        super.initViewModel();
        viewModel = new ViewModelProvider(mContext).get(UserManageViewModel.class);
        viewModel.getChatRoomObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<ChatRoom>() {
                @Override
                public void onSuccess(ChatRoom data) {
                    mChatRoom = data;
                }
            });
        });

        LiveDataBus.get().with(DemoConstants.REFRESH_MEMBER, Boolean.class).observe(getViewLifecycleOwner(), event -> {
            if (event != null && event) {
                updateChatRoom();
            }
        });

        LiveDataBus.get().with(DemoConstants.REFRESH_MEMBER_STATE, Boolean.class).observe(getViewLifecycleOwner(), event -> {
            if (event != null && event) {
                updateChatRoom();
            }
        });
    }

    @Override
    public void initListener() {
        super.initListener();
    }

    @Override
    public void initData() {
        super.initData();
        mChatRoomManager = ChatClient.getInstance().chatroomManager();
        updateChatRoom();

        mUserListData = new ArrayList<>();
        mMuteListData = new ArrayList<>();
        mAdminListData = new ArrayList<>();

        updateUserList();
    }

    private void updateChatRoom() {
        mChatRoom = mChatRoomManager.getChatRoom(chatRoomId);
    }

    private void updateUserList() {
        if (null == mChatRoom) {
            return;
        }

        mAdminListData.clear();
        mAdminListData.addAll(mChatRoom.getAdminList());
        mUserListAdapter.setAdminList(mAdminListData);

        Map<String, Long> muteMap = mChatRoom.getMuteList();
        mMuteListData.clear();
        for (Map.Entry<String, Long> entry : muteMap.entrySet()) {
            mMuteListData.add(entry.getKey());
        }
        mUserListAdapter.setMuteList(mMuteListData);

        mUserListData.clear();

        mUserListData.addAll(mAdminListData);
        mUserListData.addAll(mChatRoom.getMemberList());


        if (mUserListData.size() == 0) {
            mUserListAdapter.setData(mUserListData);
        } else {
            ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(mUserListData.toArray(new String[0]), new ValueCallBack<Map<String, UserInfo>>() {
                @Override
                public void onSuccess(Map<String, UserInfo> stringUserInfoMap) {
                    if (null != LiveMemberListDialog.this.getActivity()) {
                        LiveMemberListDialog.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUserListAdapter.setStringUserInfoMap(stringUserInfoMap);
                                mUserListAdapter.setData(mUserListData);
                            }
                        });
                    }
                }

                @Override
                public void onError(int i, String s) {

                }
            });
        }

    }

    private static class UserListAdapter extends EaseBaseRecyclerViewAdapter<String> {
        private static List<String> adminList;
        private static List<String> muteList;
        private static Map<String, UserInfo> stringUserInfoMap;

        public UserListAdapter() {
        }

        public void setAdminList(List<String> adminList) {
            this.adminList = adminList;
        }

        public void setStringUserInfoMap(Map<String, UserInfo> stringUserInfoMap) {
            this.stringUserInfoMap = stringUserInfoMap;
        }

        public void setMuteList(List<String> muteList) {
            this.muteList = muteList;
        }

        public Map<String, UserInfo> getStringUserInfoMap() {
            return stringUserInfoMap;
        }

        @Override
        public UserListViewHolder getViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_user_list, parent, false);
            return new UserListViewHolder(view, mContext);
        }

        private static class UserListViewHolder extends ViewHolder<String> {
            private ImageView ivUserAvatar;
            private TextView tvUserName;
            private TextView tvRoleType;
            private ImageView roleState;
            private Context context;

            public UserListViewHolder(@NonNull View itemView, Context context) {
                super(itemView);
                this.context = context;
            }

            @Override
            public void initView(View itemView) {
                ivUserAvatar = findViewById(R.id.iv_user_avatar);
                tvUserName = findViewById(R.id.tv_user_name);
                tvRoleType = findViewById(R.id.tv_role_type);
                roleState = findViewById(R.id.iv_state_icon);
            }

            @Override
            public void setData(String item, int position) {
                tvUserName.setText(item);
                try {
                    Glide.with(context).load(stringUserInfoMap.get(item).getAvatarUrl()).placeholder(R.drawable.avatar_default).error(R.drawable.avatar_default).into(ivUserAvatar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (null != adminList && adminList.contains(item)) {
                    tvRoleType.setText(context.getResources().getString(R.string.role_type_moderator));
                    tvRoleType.setBackgroundResource(R.drawable.ease_live_moderator_bg);
                    tvRoleType.setVisibility(View.VISIBLE);
                } else {
                    tvRoleType.setVisibility(View.GONE);
                }

                if (null != muteList && muteList.contains(item)) {
                    roleState.setVisibility(View.VISIBLE);
                    roleState.setImageResource(R.drawable.live_mute_icon);
                } else {
                    roleState.setVisibility(View.GONE);
                }
            }
        }
    }

    private static class UserListSpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public UserListSpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view,
                                   RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = 0;
            outRect.right = 0;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = 0;
            } else {
                outRect.top = 0;
            }
        }
    }
}
