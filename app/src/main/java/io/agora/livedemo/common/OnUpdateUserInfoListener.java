package io.agora.livedemo.common;

import java.util.Map;

import io.agora.chat.UserInfo;


public interface OnUpdateUserInfoListener {
    void onSuccess(Map<String, UserInfo> userInfoMap);

    void onError(int error, String errorMsg);
}
