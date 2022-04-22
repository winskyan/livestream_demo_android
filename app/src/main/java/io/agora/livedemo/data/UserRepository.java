package io.agora.livedemo.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.Error;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.livedemo.DemoApplication;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnUpdateUserInfoListener;
import io.agora.livedemo.common.db.DemoDbHelper;
import io.agora.livedemo.common.db.dao.UserDao;
import io.agora.livedemo.common.db.entity.UserEntity;
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
        EaseUser user = getUserInfo(agoraId);
        if (!TextUtils.isEmpty(agoraId) && !TextUtils.isEmpty(pwd)) {
            mCurrentUser = new User();
            mCurrentUser.setId(agoraId);
            mCurrentUser.setPwd(pwd);
            mCurrentUser.setNickName(user.getNickname());
            mCurrentUser.setAvatarDefaultResource(R.drawable.ease_default_avatar);
            mCurrentUser.setAvatarUrl(user.getAvatar());
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
            mCurrentUser.setAvatarDefaultResource(R.drawable.ease_default_avatar);
        }
        initCurrentUserInfo();
        return mCurrentUser;
    }

    private void initCurrentUserInfo() {
        EaseUser easeUser = new EaseUser(mCurrentUser.getId());
        easeUser.setNickname(mCurrentUser.getNickName());
        easeUser.setAvatar(mCurrentUser.getAvatarUrl());
        DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao().insert(UserEntity.parseParent(easeUser));
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }

    public void setHeadImageList(List<HeadImageInfo> headImageList) {
        this.mHeadImageList = headImageList;
    }

    public EaseUser getUserInfo(String username) {
        return getUserInfo(username, true);
    }

    public EaseUser getUserInfo(String username, boolean fetchFromServer) {
        if (TextUtils.isEmpty(username)) {
            return null;
        }
        // To get instance of EaseUser, here we get it from the user list in memory
        // You'd better cache it if you get it from your server
        EaseUser user = getUserInfoFromDb(username);
        if (user == null) {
            if (fetchFromServer) {
                List<String> userIdList = new ArrayList<>(1);
                userIdList.add(username);
                getUserInfoFromServer(userIdList, null);
            }
            user = new EaseUser(username);
        }
        return user;
    }

    public EaseUser getUserInfoFromDb(String username) {
        UserDao userDao = DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao();
        if (null != userDao) {
            List<EaseUser> list = userDao.loadUserByUserId(username);
            if (null != list && list.size() > 0) {
                return list.get(0);
            }
        }
        return new EaseUser(username);
    }

    public void fetchUserInfo(List<String> usernameList, OnUpdateUserInfoListener listener) {
        if (null == usernameList || usernameList.size() == 0) {
            if (null != listener) {
                listener.onError(Error.GENERAL_ERROR, "");
            }
            return;
        }
        //avoid fetch self info
        usernameList.remove(DemoHelper.getAgoraId());

        getUserInfoFromServer(usernameList, listener);
    }

    public void saveUserInfoToDb(EaseUser easeUser) {
        if (null == DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao()) {
            return;
        }
        DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao().insert(UserEntity.parseParent(easeUser));
    }

    private void getUserInfoFromServer(final List<String> usernameList,
                                       final OnUpdateUserInfoListener listener) {
        if (usernameList.size() == 0) {
            return;
        }
        ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(usernameList.toArray(new String[0]), new ValueCallBack<Map<String, UserInfo>>() {
            @Override
            public void onSuccess(Map<String, UserInfo> value) {
                Log.i("lives", "getUserInfoById success");
                if (null != listener) {
                    listener.onSuccess(value);
                }
                for (Map.Entry<String, UserInfo> entity : value.entrySet()) {
                    EaseUser easeUser = transformUserInfo(entity.getValue());
                    addDefaultAvatar(easeUser, null);
                    saveUserInfoToDb(easeUser);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                Log.e("lives", "getUserInfoById onError error msg=" + errorMsg);
                if (null != listener) {
                    listener.onError(error, errorMsg);
                }
            }
        });
    }

    private void addDefaultAvatar(EaseUser item, List<String> localUsers) {
        if (null == DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao()) {
            return;
        }
        if (localUsers == null) {
            localUsers = DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao().loadAllUsers();
        }
        if (TextUtils.isEmpty(item.getAvatar())) {
            if (localUsers.contains(item.getUsername())) {
                String avatar = DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao().loadUserByUserId(item.getUsername()).get(0).getAvatar();
                if (!TextUtils.isEmpty(avatar)) {
                    item.setAvatar(avatar);
                } else {
                    if (null != mHeadImageList && mHeadImageList.size() > 0) {
                        item.setAvatar(mHeadImageList.get(0).getUrl());
                    }
                }
            } else {
                if (null != mHeadImageList && mHeadImageList.size() > 0) {
                    item.setAvatar(mHeadImageList.get(0).getUrl());
                }
            }
        }
    }

    private EaseUser transformUserInfo(UserInfo info) {
        if (info != null) {
            EaseUser userEntity = new EaseUser();
            userEntity.setUsername(info.getUserId());
            userEntity.setNickname(info.getNickname());
            userEntity.setEmail(info.getEmail());
            userEntity.setAvatar(info.getAvatarUrl());
            userEntity.setBirth(info.getBirth());
            userEntity.setGender(info.getGender());
            userEntity.setExt(info.getExt());
            userEntity.setSign(info.getSignature());
            EaseUtils.setUserInitialLetter(userEntity);
            return userEntity;
        }
        return null;
    }
}
