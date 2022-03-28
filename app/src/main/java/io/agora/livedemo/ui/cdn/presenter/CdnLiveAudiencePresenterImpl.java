package io.agora.livedemo.ui.cdn.presenter;

import io.agora.cdn.presenter.CdnAudiencePresenter;
import io.agora.livedemo.common.ThreadManager;
import io.agora.livedemo.data.model.AgoraTokenBean;
import io.agora.livedemo.data.model.CdnUrlBean;
import io.agora.livedemo.data.restapi.LiveException;
import io.agora.livedemo.data.restapi.LiveManager;
import retrofit2.Response;

public class CdnLiveAudiencePresenterImpl extends CdnAudiencePresenter {
    @Override
    public void onLiveClosed() {
        runOnUI(() -> {
            if (isActive()) {
                mView.onLiveClosed();
            }
        });
    }

    @Override
    public void leaveChannel() {
        runOnUI(() -> {
            if (isActive()) {
                mView.onLeaveChannel();
            }
        });
    }

    @Override
    public void getFastToken(String hxId, String channel, String hxAppkey, int uid, boolean isRenew) {
        ThreadManager.getInstance().runOnIOThread(() -> {
            try {
                Response<AgoraTokenBean> response = LiveManager.getInstance().getAgoraToken(hxId, channel, hxAppkey, uid);
                runOnUI(() -> {
                    if (isActive()) {
                        mView.onGetTokenSuccess(response.body().getAccessToken(), response.body().getAgoraUserId(), isRenew);
                    }
                });
            } catch (LiveException e) {
                e.printStackTrace();
                runOnUI(() -> {
                    if (isActive()) {
                        mView.onGetTokenFail(e.getDescription());
                    }
                });
            }
        });
    }

    @Override
    public void getCdnUrl(String channel) {
        ThreadManager.getInstance().runOnIOThread(() -> {
            try {
                Response<CdnUrlBean> response = LiveManager.getInstance().getCdnPullUrl(channel);
                runOnUI(() -> {
                    if (isActive()) {
                        mView.onGetCdnUrlSuccess(response.body().getData());
                    }
                });
            } catch (LiveException e) {
                e.printStackTrace();
                runOnUI(() -> {
                    if (isActive()) {
                        mView.onGetCdnUrlFail(e.getDescription());
                    }
                });
            }
        });
    }
}

