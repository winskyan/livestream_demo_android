package io.agora.livedemo.ui.cdn.fragment;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.data.restapi.model.ResponseModule;
import io.agora.livedemo.ui.cdn.CdnLiveAudienceActivity;
import io.agora.livedemo.ui.live.fragment.LiveListFragment;

public class CdnLivingListFragment extends LiveListFragment {
    private List<LiveRoom> vodList;
    private static final int MAX_VOD_COUNT = 10;

    @Override
    public void onItemClick(View view, int position) {
        LiveRoom liveRoom = adapter.getItem(position);
        if (DemoHelper.isCdnLiveType(liveRoom.getVideo_type())) {
            CdnLiveAudienceActivity.actionStart(mContext, liveRoom);
        } else {
            // LiveAudienceActivity.actionStart(mContext, liveRoom);
        }
    }


    @Override
    protected void initViewModel() {
        super.initViewModel();
        viewModel.getFastVodRoomsObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<ResponseModule<List<LiveRoom>>>() {
                @Override
                public void onSuccess(ResponseModule<List<LiveRoom>> data) {
                    vodList = data.data;
                    showLiveList(false);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    showLiveList(false);
                }
            });
        });
        viewModel.getFastRoomsObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<ResponseModule<List<LiveRoom>>>() {
                @Override
                public void onSuccess(ResponseModule<List<LiveRoom>> data) {
                    cursor = data.cursor;
                    hasMoreData = true;
                    List<LiveRoom> livingRooms = data.data;
                    if (livingRooms.size() < pageSize) {
                        hasMoreData = false;
                    }
                    if (isLoadMore) {
                        setAdapterData(livingRooms, true);
                    } else {
                        if (vodList != null) {
                            livingRooms.addAll(0, vodList);
                        }
                        setAdapterData(livingRooms, false);
                    }
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    hideLoadingView(isLoadMore);
                }
            });
        });
    }

    @Override
    protected void initData() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.getFastCdnRoomList(MAX_VOD_COUNT, null);
    }

    @Override
    protected void refreshList() {
        viewModel.getFastCdnRoomList(MAX_VOD_COUNT, null);
    }

    @Override
    protected void loadLiveList(int limit, String cursor) {
        viewModel.getFastRoomList(limit, cursor);
    }

    public List<LiveRoom> getLiveRooms() {
        if (null != adapter) {
            return adapter.getData();
        }
        return new ArrayList<>(0);
    }
}
