package io.agora.livedemo.ui.live.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.common.OnUpdateUserInfoListener;
import io.agora.livedemo.data.UserRepository;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.ui.base.BaseFragment;
import io.agora.livedemo.ui.base.GridMarginDecoration;
import io.agora.livedemo.ui.live.adapter.LiveListAdapter;
import io.agora.livedemo.ui.live.viewmodels.LiveListViewModel;

public class LiveListFragment extends BaseFragment implements OnItemClickListener {
    protected SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar loadMorePB;

    protected static final int pageSize = 10;
    protected String cursor;
    protected boolean hasMoreData;
    private boolean isLoading;
    protected boolean isLoadMore;
    public LiveListAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private String status;
    protected LiveListViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initArgument();
        return inflater.inflate(R.layout.fragment_live_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initViewModel();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListener();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void initArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            status = bundle.getString("status");
        }
    }

    private void initView() {
        if (null == getView()) {
            return;
        }
        loadMorePB = getView().findViewById(R.id.pb_load_more);
        recyclerView = getView().findViewById(R.id.recycleview);
        gridLayoutManager = new GridLayoutManager(mContext, 2, RecyclerView.VERTICAL, false);
        linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        swipeRefreshLayout = getView().findViewById(R.id.refresh_layout);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridMarginDecoration(mContext, 10));
        adapter = new LiveListAdapter();
        adapter.setEmptyView(R.layout.live_list_empty);
        recyclerView.setAdapter(adapter);

        adapter.setStatus(status);
    }

    protected void initViewModel() {
        viewModel = new ViewModelProvider(this).get(LiveListViewModel.class);
/*        viewModel.getAllObservable().observe(getViewLifecycleOwner(), new Observer<Resource<ResponseModule<List<LiveRoom>>>>() {
            @Override
            public void onChanged(Resource<ResponseModule<List<LiveRoom>>> response) {
                LiveListFragment.this.parseResource(response, new OnResourceParseCallback<ResponseModule<List<LiveRoom>>>() {
                    @Override
                    public void onSuccess(ResponseModule<List<LiveRoom>> data) {
                        cursor = data.cursor;
                        hasMoreData = data.data.size() >= pageSize;
                        setAdapterData(data.data, isLoadMore);
                    }

                    @Override
                    public void hideLoading() {
                        super.hideLoading();
                        hideLoadingView(isLoadMore);
                    }
                });
            }
        });*/
    }

    private void initListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && hasMoreData
                        && !isLoading
                        && gridLayoutManager.findLastVisibleItemPosition() == gridLayoutManager.getItemCount() - 1) {
                    showLiveList(true);
                }
            }
        });
        adapter.setOnItemClickListener(this);

        LiveDataBus.get().with(DemoConstants.FRESH_LIVE_LIST, Boolean.class)
                .observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean != null && aBoolean) {
                            int limit = pageSize;
                            try {
                                limit = adapter.getData().size();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            isLoadMore = false;
                            loadLiveList(limit, null);
                        }
                    }
                });
    }

    protected void refreshList() {
        showLiveList(false);
    }

    protected void initData() {
        swipeRefreshLayout.setRefreshing(true);
        showLiveList(false);
    }

    protected void showLiveList(final boolean isLoadMore) {
        this.isLoadMore = isLoadMore;
        if (!isLoadMore) {
            cursor = null;
        }
        loadLiveList(pageSize, cursor);
    }

    protected void loadLiveList(int limit, String cursor) {
        viewModel.getLiveRoomList(limit, cursor);
    }

    protected void hideLoadingView(boolean isLoadMore) {
        isLoading = false;
        if (!isLoadMore)
            swipeRefreshLayout.setRefreshing(false);
        else
            loadMorePB.setVisibility(View.INVISIBLE);
    }

    private boolean isOngoingLive() {
        return !TextUtils.isEmpty(status) && TextUtils.equals(status, DemoConstants.LIVE_ONGOING);
    }

    @Override
    public void onItemClick(View view, int position) {
        LiveRoom liveRoom = adapter.getItem(position);
        String status = liveRoom.getStatus();
        boolean living = DemoHelper.isLiving(status);
        if (living) {
            if (TextUtils.equals(liveRoom.getOwner(), ChatClient.getInstance().getCurrentUser())) {
                // LiveAnchorActivity.actionStart(mContext, liveRoom);
            } else {
                showDialog();
            }
        } else {
            // LiveAnchorActivity.actionStart(mContext, liveRoom);
        }
    }

    private void showDialog() {
        Toast.makeText(mContext, R.string.live_list_warning, Toast.LENGTH_SHORT).show();
    }

    protected void setAdapterData(List<LiveRoom> data, boolean isAdd) {
        if (data == null) {
            return;
        }
        List<String> anchorList = new ArrayList<>(data.size());
        for (LiveRoom liveRoom : data) {
            if (!anchorList.contains(liveRoom.getOwner())) {
                anchorList.add(liveRoom.getOwner());
            }
        }

        if (anchorList.size() == 0) {
            recyclerView.setLayoutManager(linearLayoutManager);
            adapter.setData(data);
        } else {
            UserRepository.getInstance().fetchUserInfo(anchorList, new OnUpdateUserInfoListener() {
                @Override
                public void onSuccess(Map<String, UserInfo> userInfoMap) {
                    if (null != LiveListFragment.this.getActivity()) {
                        LiveListFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isAdd) {
                                    if (null == adapter.getData() || adapter.getData().size() == 0) {
                                        recyclerView.setLayoutManager(gridLayoutManager);
                                    }
                                    adapter.setData(data);
                                } else {
                                    if (0 == data.size()) {
                                        recyclerView.setLayoutManager(linearLayoutManager);
                                    } else {
                                        if (null == adapter.getData() || adapter.getData().size() == 0) {
                                            recyclerView.setLayoutManager(gridLayoutManager);
                                        }
                                        adapter.setData(data);
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(int error, String errorMsg) {

                }
            });
        }


    }
}
