package io.agora.livedemo.ui.live.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.ui.base.BaseActivity;
import io.agora.livedemo.ui.base.BaseLiveDialogFragment;
import io.agora.livedemo.ui.live.adapter.FragmentAdapter;
import io.agora.livedemo.ui.live.viewmodels.LivingViewModel;
import io.agora.livedemo.ui.live.viewmodels.UserManageViewModel;

/**
 * Created by wei on 2017/3/3.
 */

public class RoomUserManagementDialog extends BaseLiveDialogFragment {
    private BaseActivity mContext;
    private String chatroomId;
    TabLayout tabLayout;
    ViewPager viewPager;
    private FragmentAdapter adapter;
    private UserManageViewModel viewModel;

    public RoomUserManagementDialog() {
    }

    public RoomUserManagementDialog(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = (BaseActivity) context;
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_room_user_management;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
    }

    @Override
    public void initViewModel() {
        viewModel = new ViewModelProvider(mContext).get(UserManageViewModel.class);
        LivingViewModel livingViewModel = new ViewModelProvider(mContext).get(LivingViewModel.class);
        livingViewModel.getMemberNumberObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>() {
                @Override
                public void onSuccess(LiveRoom data) {
                    String title = getString(R.string.em_live_user_manage_users, data.getAudienceNum() + 1);
                    adapter.getTitles().remove(0);
                    adapter.getTitles().add(0, title);
                    adapter.notifyDataSetChanged();
                }
            });
        });
        viewModel.getWhitesObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> data) {
                    String title = getString(R.string.em_live_user_manage_white, data.size());
                    adapter.getTitles().remove(1);
                    adapter.getTitles().add(1, title);
                    adapter.notifyDataSetChanged();
                }
            });
        });
        viewModel.getMuteObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> data) {
                    String title = getString(R.string.em_live_user_manage_mute, data.size());
                    adapter.getTitles().remove(2);
                    adapter.getTitles().add(2, title);
                    adapter.notifyDataSetChanged();
                }
            });
        });
        LiveDataBus.get().with(DemoConstants.REFRESH_MEMBER, Boolean.class).observe(getViewLifecycleOwner(), event -> {
            if (event != null && event) {
                getDataFromServer();
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);

        getDataFromServer();
    }

    private void getDataFromServer() {
        viewModel.getMembers(chatroomId);
        viewModel.getWhiteList(chatroomId);
        viewModel.getMuteList(chatroomId);
    }

    private void setupViewPager() {
        adapter = new FragmentAdapter(getChildFragmentManager());
        adapter.addFragment(RoomUserManagementFragment.newInstance(chatroomId,
                RoomUserManagementFragment.ManagementType.MEMBER), "成员");
        adapter.addFragment(RoomUserManagementFragment.newInstance(chatroomId,
                RoomUserManagementFragment.ManagementType.BLACKLIST), "白名单");
        adapter.addFragment(RoomUserManagementFragment.newInstance(chatroomId,
                RoomUserManagementFragment.ManagementType.MUTE), "用户禁言");
        viewPager.setAdapter(adapter);
    }

}
