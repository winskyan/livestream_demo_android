package io.agora.livedemo.ui.live.fragment;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.common.reponsitories.Resource;

public class RoomWhiteManageFragment extends RoomUserManagementFragment {

    @Override
    protected void initViewModel() {
        super.initViewModel();
        viewModel.getWhitesObservable().observe(getViewLifecycleOwner(), new Observer<Resource<List<String>>>() {
            @Override
            public void onChanged(Resource<List<String>> response) {
                RoomWhiteManageFragment.this.parseResource(response, new OnResourceParseCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> data) {
                        Log.e("TAG", "getWhitesObservable = " + data.size());
                        setAdapter(data);
                    }

                    @Override
                    public void hideLoading() {
                        super.hideLoading();
                        finishRefresh();
                    }
                });
            }
        });
    }

    @Override
    protected void executeFetchTask() {
        viewModel.getWhiteList(chatroomId);
    }

    @Override
    protected void showOtherInfo(ManagementAdapter.ManagementViewHolder holder, List<String> userList, int position) {
        holder.tvLabel.setVisibility(View.GONE);
        holder.managerButton.setVisibility(View.VISIBLE);
        holder.managerButton.setText(getString(R.string.live_anchor_remove_white));
        holder.managerButton.setBackground(null);
        if (DemoHelper.isOwner(userList.get(position))) {
            holder.managerButton.setVisibility(View.GONE);
        }

        holder.managerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> list = new ArrayList<>();
                list.add(userList.get(position));
                viewModel.removeFromChatRoomWhiteList(chatroomId, list);
            }
        });
    }
}
