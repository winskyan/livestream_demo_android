package io.agora.livedemo.data;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.data.model.HeadImageInfo;
import io.agora.livedemo.data.model.User;
import io.agora.livedemo.utils.Utils;

public class UserRepository {
    private static volatile UserRepository mInstance;

    private Context mContext;
    private User mCurrentUser;

    private List<HeadImageInfo> mHeadImageList;

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
        String pwd = DemoHelper.getPwd();
        String nickname = DemoHelper.getNickName();
        int defaultRes = DemoHelper.getAvatarDefaultResource();
        String avatarUrl = DemoHelper.getAvatarUrl();
        if (!TextUtils.isEmpty(agoraId) && !TextUtils.isEmpty(pwd)) {
            mCurrentUser = new User();
            mCurrentUser.setId(agoraId);
            mCurrentUser.setPwd(pwd);
            mCurrentUser.setNickName(nickname);
            mCurrentUser.setAvatarDefaultResource(defaultRes);
            mCurrentUser.setAvatarUrl(avatarUrl);
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
        mCurrentUser.setPwd(Utils.getStringRandom(8));
        mCurrentUser.setNickName(mCurrentUser.getId());
        if (null != mHeadImageList) {
            int index = (int) Math.round(Math.random() * mHeadImageList.size() + 1);
            mCurrentUser.setAvatarUrl(mHeadImageList.get(index).getUrl());
        } else {
            mCurrentUser.setAvatarDefaultResource(R.drawable.avatar_default);
        }
        return mCurrentUser;
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }

    public void setHeadImageList(List<HeadImageInfo> headImageList) {
        this.mHeadImageList = headImageList;
    }
}
