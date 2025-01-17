package com.easemob.fastlive;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.SurfaceView;

import com.easemob.fastlive.rtc.RtcEventHandler;
import com.easemob.fastlive.stats.StatsManager;
import com.easemob.fastlive.widgets.VideoGridContainer;

import java.util.Locale;

import io.agora.mediaplayer.IMediaPlayer;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.mediaplayer.data.PlayerUpdatedInfo;
import io.agora.mediaplayer.data.SrcInfo;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.DirectCdnStreamingError;
import io.agora.rtc2.DirectCdnStreamingMediaOptions;
import io.agora.rtc2.DirectCdnStreamingState;
import io.agora.rtc2.DirectCdnStreamingStats;
import io.agora.rtc2.IDirectCdnStreamingEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

import static android.content.ContentValues.TAG;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_640x360;

/**
 * Agora极速直播的帮助类
 */
public class FastLiveHelper {
    private Context mContext;
    private AgoraEngine mAgoraEngine;
    private StatsManager mStatsManager;
    private EngineConfig mGlobalConfig;
    private boolean isPaused;//是否暂停了
    private boolean isLiving;//是否正在直播
    private int lastUid = -1;
    private final VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
            VD_640x360,
            FRAME_RATE_FPS_15,
            700,
            ORIENTATION_MODE_FIXED_PORTRAIT
    );
    private IMediaPlayer mMediaPlayer;
    private Runnable pendingDirectCDNStoppedRun = null;
    private final IDirectCdnStreamingEventHandler iDirectCdnStreamingEventHandler = new IDirectCdnStreamingEventHandler() {
        @Override
        public void onDirectCdnStreamingStateChanged(DirectCdnStreamingState directCdnStreamingState, DirectCdnStreamingError directCdnStreamingError, String s) {
            Log.d(TAG, String.format("Stream Publish(DirectCdnStreaming): onDirectCdnStreamingStateChanged directCdnStreamingState=%s directCdnStreamingError=%s", directCdnStreamingState.toString(), directCdnStreamingError.toString()));
            switch (directCdnStreamingState){
                case STOPPED:
                    if(pendingDirectCDNStoppedRun != null){
                        pendingDirectCDNStoppedRun.run();
                        pendingDirectCDNStoppedRun = null;
                    }
                    break;
            }
        }

        @Override
        public void onDirectCdnStreamingStats(DirectCdnStreamingStats directCdnStreamingStats) {

        }
    };

    private static class FastLiveHelperInstance {
        private static final FastLiveHelper instance = new FastLiveHelper();
    }
    private FastLiveHelper(){}

    public static FastLiveHelper getInstance() {
        return FastLiveHelperInstance.instance;
    }

    /**
     * 初始化
     * （1）初始化对象{@link AgoraEngine},并在其中将{@link RtcEngine#setChannelProfile(int)}设置为了直播模式{@link Constants#CHANNEL_PROFILE_LIVE_BROADCASTING}
     * （2）初始化对象{@link StatsManager},用于展示各状态信息
     *  (3) 初始化配置对象{@link EngineConfig}
     * @param context
     * @param appId
     */
    public void init(Context context, String appId) {
        this.mContext = context;
        mAgoraEngine = new AgoraEngine(context, appId);
        mStatsManager = new StatsManager();
        mGlobalConfig = new EngineConfig();
        mGlobalConfig.setAppId(appId);
    }

    /**
     * 是否在直播页面展示视频参数等状态
     * @param showStats
     */
    public void showVideoStats(boolean showStats) {
        mStatsManager.enableStats(showStats);
    }

    /**
     * 获取直播状态管理类
     * @return
     */
    public StatsManager getStatsManager() {
        return mStatsManager;
    }

    /**
     * 获取rtcEngine
     * @return
     */
    public RtcEngine rtcEngine() {
        return mAgoraEngine != null ? mAgoraEngine.rtcEngine() : null;
    }

    /**
     * 获取极速直播中的配置
     * @return
     */
    public EngineConfig getEngineConfig() {
        return mGlobalConfig;
    }

    /**
     * 获取极速直播中的SP
     * @return
     */
    public SharedPreferences getFastSPreferences() {
        return FastPrefManager.getPreferences(mContext);
    }

    /**
     * 注册rtc事件
     * @param handler
     */
    public void registerRtcHandler(RtcEventHandler handler) {
        mAgoraEngine.registerRtcHandler(handler);
    }

    /**
     * 移除rtc事件
     * @param handler
     */
    public void removeRtcHandler(RtcEventHandler handler) {
        mAgoraEngine.removeRtcHandler(handler);
    }

    /**
     * 生命周期可见时调用
     */
    public void onResume() {
        if(isPaused) {
            isPaused = false;
            isLiving = true;
            setVideoMuted(false);
            setAudioMuted(false);
        }
    }

    /**
     * 生命周期pause时调用
     */
    public void onPause() {
        if(isLiving) {
            isLiving = false;
            isPaused = true;
            setVideoMuted(true);
            setAudioMuted(true);
        }
    }

    /**
     * 结束直播的时候调用
     * @param handler
     */
    public void onDestroy(RtcEventHandler handler) {
        Log.i("fast", "removeRtcHandler and leave channel");
        removeRtcHandler(handler);
        rtcEngine().stopPreview();
        rtcEngine().leaveChannel();
    }

    /**
     * 设置用户角色
     * @param role
     */
    public void setClientRole(int role) {
        if(role == Constants.CLIENT_ROLE_AUDIENCE) {
            // ClientRoleOptions clientRoleOptions = new ClientRoleOptions();
            // clientRoleOptions.audienceLatencyLevel = getEngineConfig().isLowLatency() ? Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY : Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY;
            rtcEngine().setClientRole(role);
        }else {
            rtcEngine().setClientRole(role);
        }
    }

    /**
     * 加入channel
     * @param role 用户角色
     * @param token 传入用于鉴权的 Token。一般在你的服务器端生成的 Token。
     * @param uid uid为本地用户的 ID。数据类型为整型，且频道内每个用户的 uid 必须是唯一的。
     *            若将 uid 设为 0，则 SDK 会自动分配一个 uid，并在 {@link com.easemob.fastlive.rtc.AgoraRtcHandler#onJoinChannelSuccess(String, int, int)}回调中报告。
     */
    public void joinRtcChannel(int role, String token, int uid) {
        setClientRole(role);
        rtcEngine().enableVideo();
        configVideo();
        // rtcEngine().joinChannel(token, getEngineConfig().getChannelName(), null, uid);
        ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
        channelMediaOptions.publishAudioTrack = true;
        channelMediaOptions.publishCameraTrack = true;
        channelMediaOptions.clientRoleType = role;
        rtcEngine().joinChannel(token, getEngineConfig().getChannelName(), uid, channelMediaOptions);
    }

    /**
     * 更新token
     * @param token
     */
    public void renewRtcToken(String token) {
        rtcEngine().renewToken(token);
    }

    private void configVideo() {
        VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
                FastConstants.VIDEO_DIMENSIONS[getEngineConfig().getVideoDimenIndex()],
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        );
        // configuration.mirrorMode = FastConstants.VIDEO_MIRROR_MODES[getEngineConfig().getMirrorEncodeIndex()];
        rtcEngine().setVideoEncoderConfiguration(configuration);
    }

    /**
     * 主播开始直播
     * @param container
     */
    public void startBroadcast(VideoGridContainer container, int uid) {
        rtcEngine().enableAudio();
        rtcEngine().enableVideo();
        setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        SurfaceView surfaceView = prepareRtcVideo(uid, true);
        surfaceView.setZOrderMediaOverlay(true);
        container.addUserVideoSurface(uid, surfaceView, true);
        rtcEngine().startPreview();
        isLiving = true;
    }

    public void startCdnBroadcast(VideoGridContainer container, int uid, String url, IDirectCdnStreamingEventHandler handler) {
        rtcEngine().enableAudio();
        rtcEngine().enableVideo();
        setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        SurfaceView surfaceView = prepareRtcVideo(uid, true);
        surfaceView.setZOrderMediaOverlay(true);
        container.addUserVideoSurface(uid, surfaceView, true);
        rtcEngine().startPreview();

        rtcEngine().setDirectCdnStreamingVideoConfiguration(encoderConfiguration);
        DirectCdnStreamingMediaOptions directCdnStreamingMediaOptions = new DirectCdnStreamingMediaOptions();
        directCdnStreamingMediaOptions.publishCameraTrack = true;
        directCdnStreamingMediaOptions.publishMicrophoneTrack = true;
        rtcEngine().startDirectCdnStreaming(iDirectCdnStreamingEventHandler , url, directCdnStreamingMediaOptions);
        isLiving = true;
    }

    public void startPullCdn(VideoGridContainer container, int uid, String url) {
        setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        rtcEngine().enableAudio();
        rtcEngine().enableVideo();
        if(mMediaPlayer == null){
            mMediaPlayer = rtcEngine().createMediaPlayer();
            mMediaPlayer.registerPlayerObserver(new IMediaPlayerObserver() {
                @Override
                public void onPlayerStateChanged(io.agora.mediaplayer.Constants.MediaPlayerState mediaPlayerState, io.agora.mediaplayer.Constants.MediaPlayerError mediaPlayerError) {
                    // Log.d(TAG, "MediaPlayer onPlayerStateChanged -- url=" + mMediaPlayer.getPlaySrc() + "state=" + mediaPlayerState + ", error=" + mediaPlayerError);
                    if (mediaPlayerState == io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                        if (mMediaPlayer != null) {
                            mMediaPlayer.play();
                        }
                    }
                }

                @Override
                public void onPositionChanged(long l) {

                }

                @Override
                public void onPlayerEvent(io.agora.mediaplayer.Constants.MediaPlayerEvent mediaPlayerEvent, long l, String s) {

                }

                @Override
                public void onMetaData(io.agora.mediaplayer.Constants.MediaPlayerMetadataType mediaPlayerMetadataType, byte[] bytes) {

                }

                @Override
                public void onPlayBufferUpdated(long l) {

                }

                @Override
                public void onPreloadEvent(String s, io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent mediaPlayerPreloadEvent) {

                }

                @Override
                public void onCompleted() {

                }

                @Override
                public void onAgoraCDNTokenWillExpire() {

                }

                @Override
                public void onPlayerSrcInfoChanged(SrcInfo srcInfo, SrcInfo srcInfo1) {

                }

                @Override
                public void onPlayerInfoUpdated(PlayerUpdatedInfo playerUpdatedInfo) {

                }

            });
            rtcEngine().setDefaultAudioRoutetoSpeakerphone(true);
        }
        mMediaPlayer.stop();
        mMediaPlayer.openWithAgoraCDNSrc(url, uid);

        if(lastUid != -1 && lastUid != uid) {
            removeRemoteVideo(lastUid, container);
        }
        lastUid = uid;
        if(!container.containUid(uid)) {
            SurfaceView surface = RtcEngine.CreateRendererView(mContext);
            rtcEngine().setupLocalVideo(new VideoCanvas(surface, Constants.RENDER_MODE_HIDDEN,
                    Constants.VIDEO_MIRROR_MODE_AUTO,
                    Constants.VIDEO_SOURCE_MEDIA_PLAYER,
                    mMediaPlayer.getMediaPlayerId(),
                    uid
            ));
            container.addUserVideoSurface(uid, surface, false);
        }
    }

    public void stopPullCdn() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void stopDirectCDNStreaming(Runnable onDirectCDNStopped) {
        if (rtcEngine() == null) {
            return;
        }
        rtcEngine().disableAudio();
        rtcEngine().disableVideo();
        pendingDirectCDNStoppedRun = onDirectCDNStopped;
        rtcEngine().stopDirectCdnStreaming();
    }

    /**
     * 设置视频是否停止推流
     * @param muted
     */
    public void setVideoMuted(boolean muted) {
        rtcEngine().muteLocalVideoStream(muted);
        getEngineConfig().setVideoMuted(muted);
    }

    /**
     * 设置是否静音
     * @param muted
     */
    public void setAudioMuted(boolean muted) {
        rtcEngine().muteLocalAudioStream(muted);
        getEngineConfig().setAudioMuted(muted);
    }

    /**
     * 设置远端主播视图
     * @param uid  远端用户的 UID
     */
    public void setupRemoteVideo(int uid, VideoGridContainer container) {
        SurfaceView surface = prepareRtcVideo(uid, false);
        container.addUserVideoSurface(uid, surface, false);
    }

    /**
     * 只展示一个主播视图
     * @param uid
     * @param container
     * @param onlyOne
     */
    public void setupRemoteVideo(int uid, VideoGridContainer container, boolean onlyOne) {
        if(onlyOne) {
            if(lastUid != -1 && lastUid != uid) {
                removeRemoteVideo(lastUid, container);
            }
            if(!container.containUid(uid)) {
                setupRemoteVideo(uid, container);
            }
            lastUid = uid;
        }else {
            setupRemoteVideo(uid, container);
        }
    }

    /**
     * 移除远端主播视图
     * @param uid
     * @param container
     */
    public void removeRemoteVideo(int uid, VideoGridContainer container) {
        removeRtcVideo(uid, false);
        container.removeUserVideo(uid, false);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        rtcEngine().switchCamera();
    }

    /**
     * 准备rtc直播
     * @param uid
     * @param local
     * @return
     */
    public SurfaceView prepareRtcVideo(int uid, boolean local) {
        SurfaceView surface = RtcEngine.CreateRendererView(mContext);
        if (local) {
            rtcEngine().setupLocalVideo(
                    // new VideoCanvas(
                    //         surface,
                    //         VideoCanvas.RENDER_MODE_HIDDEN,
                    //         0,
                    //         FastConstants.VIDEO_MIRROR_MODES[getEngineConfig().getMirrorLocalIndex()]
                    // )
                    new VideoCanvas(surface, VideoCanvas.RENDER_MODE_HIDDEN, uid)
            );
        } else {
            rtcEngine().setupRemoteVideo(
                    // new VideoCanvas(
                    //         surface,
                    //         VideoCanvas.RENDER_MODE_HIDDEN,
                    //         uid,
                    //         FastConstants.VIDEO_MIRROR_MODES[getEngineConfig().getMirrorRemoteIndex()]
                    // )
                    new VideoCanvas(surface, VideoCanvas.RENDER_MODE_HIDDEN, uid)
            );
        }
        return surface;
    }

    /**
     * 移除rtc video
     * @param uid
     * @param local
     */
    public void removeRtcVideo(int uid, boolean local) {
        if (local) {
            rtcEngine().setupLocalVideo(null);
        } else {
            rtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        }
    }

    /**
     * 当进程结束，或者需要的时候调用此方法
     */
    public void release() {
        mAgoraEngine.release();
    }
}

