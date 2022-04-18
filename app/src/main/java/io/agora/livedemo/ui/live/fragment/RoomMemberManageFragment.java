package io.agora.livedemo.ui.live.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import androidx.core.content.ContextCompat;

import java.util.LinkedList;
import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.data.model.LiveRoom;


public class RoomMemberManageFragment extends RoomUserManagementFragment {

    @Override
    protected void initViewModel() {
        super.initViewModel();
        livingViewModel.getMemberNumberObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    isAllMuted = data.isMute();
                    LinkedList<String> memberList = data.getMemberList(DemoConstants.MAX_SHOW_MEMBERS_COUNT);
                    if (TextUtils.equals(data.getOwner(), ChatClient.getInstance().getCurrentUser())) {
                        if (memberList == null) {
                            memberList = new LinkedList<>();
                        }
                        memberList.add(0, ChatClient.getInstance().getCurrentUser());
                    }
                    setAdapter(memberList);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    finishRefresh();
                }
            });
        });
    }

    @Override
    protected void executeFetchTask() {
        viewModel.getMembers(chatroomId);
    }

    @Override
    protected void showOtherInfo(ManagementAdapter.ManagementViewHolder holder, List<String> userList, int position) {
        String username = userList.get(position);
        boolean isAnchor = DemoHelper.isOwner(username);
        boolean isMemberMuted = false;
        if (isAnchor) {
            holder.switchMute.setVisibility(View.VISIBLE);
            holder.switchMute.setChecked(isAllMuted);
            holder.tvLabel.setVisibility(View.VISIBLE);
            holder.tvLabel.setText(getString(R.string.live_anchor_self));
            holder.tvMuteHint.setVisibility(View.VISIBLE);
        } else {
            holder.managerButton.setVisibility(View.GONE);
            holder.tvLabel.setVisibility(View.GONE);
            holder.tvLabel.setBackground(ContextCompat.getDrawable(mContext, R.drawable.live_member_label_mute_shape));
            holder.tvLabel.setText(getString(R.string.live_anchor_muted));
            holder.tvMuteHint.setVisibility(View.GONE);
            holder.switchMute.setVisibility(View.GONE);
        }

        holder.switchMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    viewModel.muteAllMembers(chatroomId);
                } else {
                    viewModel.unMuteAllMembers(chatroomId);
                }
            }
        });

    }
}
