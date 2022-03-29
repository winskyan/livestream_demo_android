package io.agora.livedemo.data;

import android.content.Context;
import android.text.TextUtils;

import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.data.model.User;
import io.agora.livedemo.utils.Utils;

public class UserRepository {
    private static volatile UserRepository mInstance;

    private Context mContext;
    private User mCurrentUser;

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        if (mInstance == null) {
            synchronized (UserRepository.class) {
                if (mInstance == null) {
                    mInstance = new UserRepository();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context;
        String agoraId = DemoHelper.getAgoraId();
        String nickname = DemoHelper.getNickName();
        int resIndex = DemoHelper.getAvatarResourceIndex();
        if (!TextUtils.isEmpty(agoraId) && !TextUtils.isEmpty(nickname) && -1 != resIndex) {
            mCurrentUser = new User();
            mCurrentUser.setId(agoraId);
            mCurrentUser.setNickName(nickname);
            mCurrentUser.setAvatarResourceIndex(resIndex);
            mCurrentUser.setAvatarResource(getResDrawable(resIndex));
        }
    }

    /**
     * get random user
     *
     * @return
     */
    public User getRandomUser() {
        mCurrentUser = new User();
        mCurrentUser.setId(Utils.getStringRandom(8));
        mCurrentUser.setNickName(Utils.getStringRandom(8));
        int index = (int) Math.round(Math.random() * 7 + 1);
        int drawable = getResDrawable(index);
        mCurrentUser.setAvatarResourceIndex(index);
        mCurrentUser.setAvatarResource(drawable);
        return mCurrentUser;
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }

    public int getResDrawable(int index) {
        return mContext.getResources().getIdentifier("avatar_" + index, "drawable", mContext.getPackageName());
    }

}
