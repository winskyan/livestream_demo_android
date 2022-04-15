package io.agora.livedemo.ui.live.fragment;

import static io.agora.livedemo.ui.live.fragment.RoomUserManagementFragment.ManagementType.BLACKLIST;
import static io.agora.livedemo.ui.live.fragment.RoomUserManagementFragment.ManagementType.MUTE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chat.ChatRoomManager;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.exceptions.ChatException;
import io.agora.livedemo.R;
import io.agora.livedemo.ThreadPoolManager;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.ui.base.BaseFragment;
import io.agora.livedemo.ui.live.viewmodels.LivingViewModel;
import io.agora.livedemo.ui.live.viewmodels.UserManageViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoomUserManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomUserManagementFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private ManagementType type;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    protected ChatRoomManager chatRoomManager;
    protected String chatroomId;
    protected ManagementAdapter adapter;
    protected UserManageViewModel viewModel;
    protected ChatRoom chatRoom;
    protected boolean isAllMuted;
    protected List<String> muteList;
    protected LivingViewModel livingViewModel;

    public RoomUserManagementFragment() {
        // Required empty public constructor
    }

    public static RoomUserManagementFragment newInstance(String chatroomId, ManagementType type) {
        RoomUserManagementFragment fragment;
        if (type == BLACKLIST) {
            fragment = new RoomWhiteManageFragment();
        } else if (type == MUTE) {
            fragment = new RoomMuteManageFragment();
        } else {
            fragment = new RoomMemberManageFragment();
        }
        Bundle args = new Bundle();
        args.putSerializable("ManagementType", type);
        args.putString("chatroomId", chatroomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = (ManagementType) getArguments().getSerializable("ManagementType");
            chatroomId = getArguments().getString("chatroomId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room_user_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView) getView().findViewById(R.id.recycleview);
        refreshLayout.setOnRefreshListener(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        chatRoomManager = ChatClient.getInstance().chatroomManager();
        chatRoom = chatRoomManager.getChatRoom(chatroomId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        adapter = new ManagementAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        initViewModel();
        fetchData();
    }

    protected void initViewModel() {
        viewModel = new ViewModelProvider(mContext).get(UserManageViewModel.class);
        livingViewModel = new ViewModelProvider(mContext).get(LivingViewModel.class);

        viewModel.getChatRoomObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<ChatRoom>() {
                @Override
                public void onSuccess(ChatRoom data) {
                    chatRoom = data;
                    isAllMuted = chatRoom.isAllMemberMuted();
                    executeFetchTask();
                }
            });
        });
    }

    private void fetchData() {
//        refreshLayout.setRefreshing(true);
//        executeFetchTask();
    }

    /**
     * 请求数据
     */
    protected void executeFetchTask() {
    }

    /**
     * 设置数据
     *
     * @param list
     */
    protected void setAdapter(List<String> list) {
        adapter.setData(list);
    }

    /**
     * 停止刷新
     */
    protected void finishRefresh() {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        executeFetchTask();
    }

    protected class ManagementAdapter extends RecyclerView.Adapter<ManagementAdapter.ManagementViewHolder> {
        private Context context;
        private List<String> userList;

        public ManagementAdapter(Context context) {
            this.context = context;
        }

        public ManagementAdapter(Context context, List<String> userList) {
            this.userList = userList;
            this.context = context;
        }

        @Override
        public ManagementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ManagementViewHolder(LayoutInflater.from(context).inflate(R.layout.em_layout_live_member_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ManagementViewHolder holder, final int position) {
            final String username = userList.get(position);
            holder.usernickView.setText(DemoHelper.getNickName(username));
            //holder.imgAvatar.setImageResource(DemoHelper.getAvatarResource(username, R.drawable.ease_default_avatar));
            showOtherInfo(holder, userList, position);
        }

        @Override
        public int getItemCount() {
            return userList == null ? 0 : userList.size();
        }

        public void setData(List<String> data) {
            this.userList = data;
            notifyDataSetChanged();
        }

        public class ManagementViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.img_avatar)
            public EaseImageView imgAvatar;
            @BindView(R.id.txt_usernick)
            public TextView usernickView;
            @BindView(R.id.btn_manager)
            public TextView managerButton;
            @BindView(R.id.tv_label)
            public TextView tvLabel;
            @BindView(R.id.switch_mute)
            public Switch switchMute;
            @BindView(R.id.tv_mute_hint)
            public TextView tvMuteHint;

            public ManagementViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }
    }

    protected void showOtherInfo(ManagementAdapter.ManagementViewHolder holder, List<String> userList, int position) {
        String username = userList.get(position);
        switch (type) {
            case MEMBER:
                ChatRoom chatRoom = chatRoomManager.getChatRoom(chatroomId);
                if (chatRoom.getAdminList().contains(ChatClient.getInstance().getCurrentUser())) {
                    holder.managerButton.setVisibility(View.INVISIBLE);
                } else {
                    holder.managerButton.setVisibility(View.VISIBLE);
                    holder.managerButton.setText("移除房管");
                }
                break;
            case MUTE:
                holder.managerButton.setText("解除禁言");
                break;
            case BLACKLIST:
                holder.managerButton.setText("移除黑名单");
                break;
        }

        holder.managerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> list = new ArrayList<>();
                list.add(username);
                ThreadPoolManager.getInstance().executeTask(new ThreadPoolManager.Task<Void>() {
                    @Override
                    public Void onRequest() throws ChatException {
                        if (type == ManagementType.MEMBER) {
                            chatRoomManager.removeChatRoomAdmin(chatroomId, username);
                        } else if (type == MUTE) {
                            chatRoomManager.unMuteChatRoomMembers(chatroomId, list);
                        } else {
                            chatRoomManager.unblockChatRoomMembers(chatroomId, list);
                        }
                        return null;
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        userList.remove(username);
                        adapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onError(ChatException exception) {
                        Toast.makeText(mContext, "操作失败：" + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public enum ManagementType {
        MEMBER,
        MUTE,
        BLACKLIST
    }
}
