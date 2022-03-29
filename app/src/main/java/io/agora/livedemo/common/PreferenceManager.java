package io.agora.livedemo.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import io.agora.livedemo.DemoApplication;

public class PreferenceManager {
    private static SharedPreferences mSharedPreferences;
    private static PreferenceManager mPreferenceManager;
    private static SharedPreferences.Editor editor;
    private static SharedPreferences mDefaultSp;
    private static SharedPreferences.Editor mDefaultEditor;

    private static final String KEY_LIVING_ID = "key_living_id";
    private static final String KEY_CAN_REGISTER = "key_can_register";
    private static final String KEY_AGORA_ID = "key_agora_id";
    private static final String KEY_NICK_NAME = "key_nick_name";
    private static final String KEY_AVATAR_RESOURCE = "key_avatar_resource";
    private static final String KEY_AVATAR_RESOURCE_INDEX = "key_avatar_resource_index";
    private static final String KEY_LIKE_NUM = "key_like_num";

    @SuppressLint("CommitPrefEdits")
    private PreferenceManager(Context cxt) {
        mSharedPreferences = cxt.getSharedPreferences("live_stream", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }

    private static void getDefaultSp(Context context) {
        if (mDefaultSp == null) {
            mDefaultSp = context.getSharedPreferences("demo", Context.MODE_PRIVATE);
            mDefaultEditor = mDefaultSp.edit();
        }
    }

    /**
     * @param cxt
     * @param nickname
     */
    public static synchronized void init(Context cxt) {
        if (mPreferenceManager == null) {
            mPreferenceManager = new PreferenceManager(cxt);
        }
    }

    /**
     * get instance of PreferenceManager
     *
     * @param
     * @return
     */
    public synchronized static PreferenceManager getInstance() {
        if (mPreferenceManager == null) {
            init(DemoApplication.getInstance());
            if (mPreferenceManager == null) {
                throw new RuntimeException("please init first!");
            }
        }

        return mPreferenceManager;
    }

    /**
     * save the living of id
     *
     * @param liveId
     */
    public void saveLivingId(String liveId) {
        editor.putString(KEY_LIVING_ID, liveId);
        editor.apply();
    }

    public String getLivingId() {
        if (null != mSharedPreferences) {
            return mSharedPreferences.getString(KEY_LIVING_ID, null);
        } else {
            return "";
        }
    }

    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public int getInt(String key) {
        if (null != mSharedPreferences) {
            return mSharedPreferences.getInt(key, -1);
        } else {
            return -1;
        }
    }

    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * set whether can register page
     *
     * @param canRegister
     */
    public static void setCanRegister(boolean canRegister) {
        getDefaultSp(DemoApplication.getInstance());
        mDefaultEditor.putBoolean(KEY_CAN_REGISTER, canRegister);
        mDefaultEditor.apply();
    }

    public static boolean isCanRegister() {
        getDefaultSp(DemoApplication.getInstance());
        return mDefaultSp.getBoolean(KEY_CAN_REGISTER, false);
    }

    /**
     * save agora id
     *
     * @param id
     */
    public void saveAgoraId(String id) {
        editor.putString(KEY_AGORA_ID, id);
        editor.apply();
    }

    public String getAgoraId() {
        if (null != mSharedPreferences) {
            return mSharedPreferences.getString(KEY_AGORA_ID, null);
        } else {
            return "";
        }
    }

    public void saveNickname(String nickname) {
        editor.putString(KEY_NICK_NAME, nickname);
        editor.apply();
    }

    public String getNickname() {
        if (null != mSharedPreferences) {
            return mSharedPreferences.getString(KEY_NICK_NAME, "");
        } else {
            return "";
        }
    }

    public void saveAvatarResource(int res) {
        editor.putInt(KEY_AVATAR_RESOURCE, res);
        editor.apply();
    }

    public int getAvatarResource() {
        if (null != mSharedPreferences) {
            return mSharedPreferences.getInt(KEY_AVATAR_RESOURCE, -1);
        } else {
            return -1;
        }
    }

    public void saveAvatarResourceIndex(int res) {
        editor.putInt(KEY_AVATAR_RESOURCE_INDEX, res);
        editor.apply();
    }

    public int getAvatarResourceIndex() {
        if (null != mSharedPreferences) {
            return mSharedPreferences.getInt(KEY_AVATAR_RESOURCE_INDEX, -1);
        } else {
            return -1;
        }
    }

    public void saveLikeNum(String roomId, int num) {
        if (TextUtils.isEmpty(roomId)) {
            return;
        }
        editor.putInt(roomId, num);
        editor.apply();
    }

    /**
     * get the number of like
     *
     * @return
     */
    public int getLikeNum(String roomId) {
        return mSharedPreferences.getInt(roomId, 0);
    }

    public void removeLivingId() {
        remove(KEY_LIVING_ID);
    }
}
