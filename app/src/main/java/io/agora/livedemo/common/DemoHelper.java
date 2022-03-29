package io.agora.livedemo.common;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.CustomMessageBody;
import io.agora.chat.MessageBody;
import io.agora.custommessage.EmCustomMsgType;
import io.agora.custommessage.MsgConstant;
import io.agora.fastlive.FastLiveHelper;
import io.agora.livedemo.DemoApplication;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.db.DemoDbHelper;
import io.agora.livedemo.common.db.dao.ReceiveGiftDao;
import io.agora.livedemo.common.db.entity.ReceiveGiftEntity;
import io.agora.livedemo.data.TestGiftRepository;
import io.agora.livedemo.data.UserRepository;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.data.model.User;

public class DemoHelper {

    /**
     * is living
     *
     * @param status
     * @return
     */
    public static boolean isLiving(String status) {
        return !TextUtils.isEmpty(status) && TextUtils.equals(status, DemoConstants.LIVE_ONGOING);
    }

    /**
     * is owner
     *
     * @param username
     * @return
     */
    public static boolean isOwner(String username) {
        if (TextUtils.isEmpty(username)) {
            return false;
        }
        return TextUtils.equals(username, ChatClient.getInstance().getCurrentUser());
    }

    public static void saveLivingId(String liveId) {
        PreferenceManager.getInstance().saveLivingId(liveId);
    }

    public static String getLivingId() {
        return PreferenceManager.getInstance().getLivingId();
    }

    public static void removeTarget(String key) {
        FastLiveHelper.getInstance().getFastSPreferences().edit().remove(key).commit();
    }

    public static void removeSaveLivingId() {
        PreferenceManager.getInstance().removeLivingId();
    }

    /**
     * is can register
     *
     * @return
     */
    public static boolean isCanRegister() {
        return PreferenceManager.isCanRegister();
    }

    public static void setCanRegister(boolean canRegister) {
        PreferenceManager.setCanRegister(canRegister);
    }

    /**
     * get nick name
     *
     * @param username
     * @return
     */
    public static String getNickName(String username) {
        /*User user = UserRepository.getInstance().getUserByUsername(username);
        if (user == null) {
            return username;
        }
        return user.getNickname();*/
        return "";
    }

    /**
     * get current user
     *
     * @return
     */
    public static User getCurrentDemoUser() {
        return UserRepository.getInstance().getCurrentUser();
    }

    /**
     * save current user
     */
    public static void saveCurrentUser() {
        PreferenceManager.getInstance().saveAgoraId(getCurrentDemoUser().getId());
        PreferenceManager.getInstance().saveNickname(getCurrentDemoUser().getNickName());
        PreferenceManager.getInstance().saveAvatarResourceIndex(getCurrentDemoUser().getAvatarResourceIndex());
        PreferenceManager.getInstance().saveAvatarResource(getCurrentDemoUser().getAvatarResource());
    }

    /**
     * clear agora id
     */
    public static void clearUser() {
        PreferenceManager.getInstance().saveAgoraId("");
        PreferenceManager.getInstance().saveNickname("");
        PreferenceManager.getInstance().saveAvatarResource(-1);
    }

    public static String getAgoraId() {
        return PreferenceManager.getInstance().getAgoraId();
    }

    public static String getNickName() {
        return PreferenceManager.getInstance().getNickname();
    }

    public static int getAvatarResource() {
        return PreferenceManager.getInstance().getAvatarResource();
    }

    public static int getAvatarResourceIndex() {
        return PreferenceManager.getInstance().getAvatarResourceIndex();
    }

    /**
     * get user avatar
     *
     * @param username
     * @return
     */
    public static int getAvatarResource(String username) {
        return getAvatarResource(username, 0);
    }

    /**
     * get user avatar
     *
     * @param username
     * @return
     */
    public static int getAvatarResource(String username, int defaultDrawable) {
        User user = UserRepository.getInstance().getCurrentUser();
        if (user == null) {
            return defaultDrawable == 0 ? R.drawable.live_logo : defaultDrawable;
        }
        DemoApplication context = DemoApplication.getInstance();
        int resId = context.getResources().getIdentifier("em_avatar_" + user.getAvatarResource(), "drawable", context.getPackageName());
        return resId == 0 ? R.drawable.live_logo : resId;
    }

    /**
     * get gift
     *
     * @param giftId
     * @return
     */
    public static GiftBean getGiftById(String giftId) {
        return TestGiftRepository.getGiftById(giftId);
    }

    public static void initDb() {
        DemoDbHelper.getInstance(DemoApplication.getInstance()).initDb(ChatClient.getInstance().getCurrentUser());
    }

    /**
     * get receive gift
     *
     * @return
     */
    public static ReceiveGiftDao getReceiveGiftDao() {
        return DemoDbHelper.getInstance(DemoApplication.getInstance()).getReceiveGiftDao();
    }

    /**
     * save gift to local
     *
     * @param message
     */
    public static void saveGiftInfo(ChatMessage message) {
        if (message == null) {
            return;
        }
        MessageBody body = message.getBody();
        if (!(body instanceof CustomMessageBody)) {
            return;
        }
        String event = ((CustomMessageBody) body).event();
        if (!TextUtils.equals(event, EmCustomMsgType.CHATROOM_GIFT.getName())) {
            return;
        }
        Map<String, String> params = ((CustomMessageBody) body).getParams();
        Set<String> keySet = params.keySet();
        String gift_id = null;
        String gift_num = null;
        if (keySet.contains(MsgConstant.CUSTOM_GIFT_KEY_ID) && keySet.contains(MsgConstant.CUSTOM_GIFT_KEY_NUM)) {
            gift_id = params.get(MsgConstant.CUSTOM_GIFT_KEY_ID);
            gift_num = params.get(MsgConstant.CUSTOM_GIFT_KEY_NUM);
            ReceiveGiftEntity entity = new ReceiveGiftEntity();
            entity.setFrom(message.getFrom());
            entity.setTo(message.getTo());
            entity.setTimestamp(message.getMsgTime());
            entity.setGift_id(gift_id);
            entity.setGift_num(Integer.valueOf(gift_num));
            List<Long> list = getReceiveGiftDao().insert(entity);
            if (list.size() <= 0) {
            } else {
                LiveDataBus.get().with(DemoConstants.REFRESH_GIFT_LIST).postValue(true);
            }
        }
    }

    /**
     * 保存点赞数量
     *
     * @param message
     */
    public static void saveLikeInfo(ChatMessage message) {
        if (message == null) {
            return;
        }
        MessageBody body = message.getBody();
        if (!(body instanceof CustomMessageBody)) {
            return;
        }
        String event = ((CustomMessageBody) body).event();
        if (!TextUtils.equals(event, EmCustomMsgType.CHATROOM_PRAISE.getName())) {
            return;
        }
        Map<String, String> params = ((CustomMessageBody) body).getParams();
        Set<String> keySet = params.keySet();
        String num = null;
        if (keySet.contains(MsgConstant.CUSTOM_PRAISE_KEY_NUM)) {
            num = params.get(MsgConstant.CUSTOM_PRAISE_KEY_NUM);
        }
        if (!TextUtils.isEmpty(num)) {
            int like_num = Integer.parseInt(num);
            int total = getLikeNum(message.getTo()) + like_num;
            saveLikeNum(message.getTo(), total);
            LiveDataBus.get().with(DemoConstants.REFRESH_LIKE_NUM).postValue(true);
        }

    }

    public static void saveLikeNum(String roomId, int num) {
        PreferenceManager.getInstance().saveLikeNum(roomId, num);
    }

    public static int getLikeNum(String roomId) {
        return PreferenceManager.getInstance().getLikeNum(roomId);
    }

    public static String formatNum(double num) {
        if (num < 10000) {
            return String.valueOf((int) num);
        }
        return new DecimalFormat("#0.0").format(num / 10000) + "万";
    }


    public static boolean isFastLiveType(String videoType) {
        return TextUtils.equals(videoType, LiveRoom.Type.agora_speed_live.name());
    }


    public static boolean isInteractionLiveType(String videoType) {
        return TextUtils.equals(videoType, LiveRoom.Type.agora_interaction_live.name());
    }


    public static boolean isCdnLiveType(String videoType) {
        return TextUtils.equals(videoType, LiveRoom.Type.agora_cdn_live.name());
    }


    public static boolean isVod(String videoType) {
        return TextUtils.equals(videoType, LiveRoom.Type.vod.name()) || TextUtils.equals(videoType, LiveRoom.Type.agora_vod.name());
    }
}
