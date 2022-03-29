package io.agora.livedemo.ui.live.fragment;

import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.agora.livedemo.R;
import io.agora.livedemo.common.OnResourceParseCallback;

public class RoomMuteManageFragment extends RoomUserManagementFragment {

    @Override
    protected void initViewModel() {
        super.initViewModel();
        viewModel.getMuteObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> data) {
                    Log.e("TAG", "getMuteObservable = " + data.size());
                    setAdapter(data);
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
        viewModel.getMuteList(chatroomId);
    }

    @Override
    protected void showOtherInfo(ManagementAdapter.ManagementViewHolder holder, List<String> userList, int position) {
        holder.tvLabel.setVisibility(View.VISIBLE);
        holder.tvLabel.setText(getString(R.string.em_live_audience_muted));
        holder.tvLabel.setBackground(ContextCompat.getDrawable(mContext, R.drawable.live_user_item_muted_bg_shape));
        holder.managerButton.setVisibility(View.VISIBLE);
        holder.managerButton.setText(getString(R.string.em_live_anchor_remove_mute));
        holder.managerButton.setBackground(null);

        holder.managerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> list = new ArrayList<>();
                list.add(userList.get(position));
                viewModel.unMuteChatRoomMembers(chatroomId, list);
            }
        });
    }
}
