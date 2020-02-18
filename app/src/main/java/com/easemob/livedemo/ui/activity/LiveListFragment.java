package com.easemob.livedemo.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.easemob.livedemo.DemoConstants;
import com.easemob.livedemo.R;
import com.easemob.livedemo.ThreadPoolManager;
import com.easemob.livedemo.common.LiveHelper;
import com.easemob.livedemo.common.OnItemClickListener;
import com.easemob.livedemo.data.model.LiveRoom;
import com.easemob.livedemo.data.restapi.LiveManager;
import com.easemob.livedemo.data.restapi.model.ResponseModule;
import com.easemob.livedemo.ui.GridMarginDecoration;
import com.easemob.livedemo.ui.LiveListAdapter;
import com.easemob.livedemo.ui.live.LiveAnchorActivity;
import com.hyphenate.exceptions.HyphenateException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveListFragment extends BaseFragment implements OnItemClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar loadmorePB;

    private static final int pageSize = 8;
    private String cursor;
    private boolean hasMoreData;
    private boolean isLoading;
    private final List<LiveRoom> liveRoomList = new ArrayList<>();
    public LiveListAdapter adapter;
    private GridLayoutManager layoutManager;
    private String status;

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListener();
        initData();
    }

    private void initArgument() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            status = bundle.getString("status");
        }
    }

    private void initView() {
        loadmorePB = (ProgressBar) getView().findViewById(R.id.pb_load_more);
        recyclerView = (RecyclerView) getView().findViewById(R.id.recycleview);
        layoutManager = new GridLayoutManager(mContext, 2, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridMarginDecoration(mContext,3));
        adapter = new LiveListAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setStatus(status);
    }

    private void initListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                showLiveList(false);
            }
        });
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if(newState == RecyclerView.SCROLL_STATE_IDLE
//                        && hasMoreData
//                        && !isLoading
//                        && layoutManager.findLastVisibleItemPosition() == layoutManager.getItemCount() -1){
//                    showLiveList(true);
//                }
//            }
//        });
        adapter.setOnItemClickListener(this);
    }

    private void initData() {
        showLiveList(false);
    }

    private void showLiveList(final boolean isLoadMore){
        if(!isLoadMore)
            swipeRefreshLayout.setRefreshing(true);
        else
            loadmorePB.setVisibility(View.VISIBLE);
        isLoading = true;
        ThreadPoolManager.getInstance().executeTask(new ThreadPoolManager.Task<ResponseModule<List<LiveRoom>>>() {
            @Override public ResponseModule<List<LiveRoom>> onRequest() throws HyphenateException {
                int num = pageSize;
                if(!isLoadMore){
                    cursor = null;
                    num = pageSize;
                }
                return isOngoingLive() ? LiveManager.getInstance().getLivingRoomList(pageSize, cursor) :
                        LiveManager.getInstance().getLiveRoomList(0, num);
            }

            @Override public void onSuccess(ResponseModule<List<LiveRoom>> listResponseModule) {
                hideLoadingView(isLoadMore);
                List<LiveRoom> returnList = listResponseModule.data;
                if(returnList.size() < pageSize){
                    hasMoreData = false;
                    cursor = null;
                }else{
                    hasMoreData = true;
                    cursor = listResponseModule.cursor;
                }

                if(!isLoadMore) {
                    liveRoomList.clear();
                }
                liveRoomList.addAll(returnList);
                adapter.setData(liveRoomList);
            }

            @Override public void onError(HyphenateException exception) {
                hideLoadingView(isLoadMore);
            }
        });
    }

    private void hideLoadingView(boolean isLoadMore){
        isLoading = false;
        if(!isLoadMore)
            swipeRefreshLayout.setRefreshing(false);
        else
            loadmorePB.setVisibility(View.INVISIBLE);
    }

    private boolean isOngoingLive() {
        return !TextUtils.isEmpty(status) && TextUtils.equals(status, DemoConstants.LIVE_ONGOING);
    }

    @Override
    public void onItemClick(View view, int position) {
        LiveRoom liveRoom = adapter.getItem(position);
        String status = liveRoom.getStatus();
        boolean living = LiveHelper.isLiving(status);
        if(living) {
            showDialog();
        }else {
            startActivity(new Intent(mContext, LiveAnchorActivity.class)
                    .putExtra("liveroom", liveRoom));
        }
    }

    private void showDialog() {
        Toast.makeText(mContext, "展示dialog", Toast.LENGTH_SHORT).show();
    }
}
