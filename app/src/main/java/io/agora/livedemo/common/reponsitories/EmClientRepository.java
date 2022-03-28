package io.agora.livedemo.common.reponsitories;

import static io.agora.cloud.HttpClientManager.Method_POST;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.Error;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.cloud.EMCloudOperationCallback;
import io.agora.cloud.EMHttpClient;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;
import io.agora.exceptions.ChatException;
import io.agora.livedemo.BuildConfig;
import io.agora.livedemo.common.ThreadManager;
import io.agora.livedemo.data.model.LoginBean;
import io.agora.livedemo.data.model.User;

public class EmClientRepository extends BaseEMRepository {

    /**
     * 上传直播间url
     *
     * @param localPath
     * @return
     */
    public LiveData<Resource<String>> updateRoomCover(String localPath) {
        return new NetworkOnlyResource<String, String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                runOnIOThread(() -> {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", "Bearer " + ChatClient.getInstance().getAccessToken());
                    EMHttpClient.getInstance().uploadFile(localPath, "", headers, new EMCloudOperationCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                JSONObject jsonObj = new JSONObject(result);
                                JSONObject entitys = jsonObj.getJSONArray("entities").getJSONObject(0);
                                String uuid = entitys.getString("uuid");
                                String url = jsonObj.getString("uri");
                                callBack.onSuccess(createLiveData(url + "/" + uuid));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String msg) {
                            callBack.onError(ErrorCode.UNKNOWN_ERROR, msg);
                        }

                        @Override
                        public void onProgress(int i) {

                        }
                    });
                });
            }
        }.asLiveData();

    }

    /**
     * log in by id and nickname
     *
     * @param user
     * @return
     */
    public LiveData<Resource<User>> log(User user) {
        return new NetworkOnlyResource<User, User>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<User>> callBack) {
                ChatClient.getInstance().login(user.getId(), user.getNickName(), new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(user));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> loginByAppServer(String username, String nickname) {
        return new NetworkOnlyResource<Boolean, Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                loginToAppServer(username, nickname, new ResultCallBack<LoginBean>() {
                    @Override
                    public void onSuccess(LoginBean value) {
                        if (value != null && !TextUtils.isEmpty(value.getAccessToken())) {
                            ChatClient.getInstance().loginWithAgoraToken(username, value.getAccessToken(), new CallBack() {
                                @Override
                                public void onSuccess() {
                                    success(nickname, callBack);
                                }

                                @Override
                                public void onError(int code, String error) {
                                    callBack.onError(code, error);

                                }

                                @Override
                                public void onProgress(int progress, String status) {

                                }
                            });
                        } else {
                            callBack.onError(Error.GENERAL_ERROR, "AccessToken is null!");
                        }

                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    private void loginToAppServer(String username, String nickname, ResultCallBack<LoginBean> callBack) {
        runOnIOThread(() -> {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                JSONObject request = new JSONObject();
                request.putOpt("userAccount", username);
                request.putOpt("userNickname", nickname);

                String url = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_SERVER_URL;
                HttpResponse response = HttpClientManager.httpExecute(url, headers, request.toString(), Method_POST);
                int code = response.code;
                String responseInfo = response.content;
                if (code == 200) {
                    if (responseInfo != null && responseInfo.length() > 0) {
                        JSONObject object = new JSONObject(responseInfo);
                        String token = object.getString("accessToken");
                        LoginBean bean = new LoginBean();
                        bean.setAccessToken(token);
                        bean.setUserNickname(nickname);
                        if (callBack != null) {
                            callBack.onSuccess(bean);
                        }
                    } else {
                        callBack.onError(code, responseInfo);
                    }
                } else {
                    callBack.onError(code, responseInfo);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                callBack.onError(Error.NETWORK_ERROR, e.getMessage());
            }
        });
    }

    private void success(String nickname, @NonNull ResultCallBack<LiveData<Boolean>> callBack) {
        // ** manually load all local groups and conversation
        callBack.onSuccess(createLiveData(true));
    }

    /**
     * 获取白名单
     *
     * @param roomId
     * @return
     */
    public LiveData<Resource<List<String>>> getWhiteList(String roomId) {
        return new NetworkOnlyResource<List<String>, List<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                getChatRoomManager().fetchChatRoomWhiteList(roomId, new ValueCallBack<List<String>>() {
                    @Override
                    public void onSuccess(List<String> value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 将用户加入白名单
     *
     * @param chatRoomId
     * @param members
     * @return
     */
    public LiveData<Resource<ChatRoom>> addToChatRoomWhiteList(String chatRoomId, List<String> members) {
        return new NetworkOnlyResource<ChatRoom, ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                ThreadManager.getInstance().runOnIOThread(() -> {
                    getChatRoomManager().addToChatRoomWhiteList(chatRoomId, members, new ValueCallBack<ChatRoom>() {
                        @Override
                        public void onSuccess(ChatRoom value) {
                            callBack.onSuccess(createLiveData(value));
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            callBack.onError(error, errorMsg);
                        }
                    });
                });
            }
        }.asLiveData();
    }

    /**
     * 将用户从白名单中移除
     *
     * @param chatRoomId
     * @param members
     * @return
     */
    public LiveData<Resource<ChatRoom>> removeFromChatRoomWhiteList(String chatRoomId, List<String> members) {
        return new NetworkOnlyResource<ChatRoom, ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                ThreadManager.getInstance().runOnIOThread(() -> {
                    getChatRoomManager().removeFromChatRoomWhiteList(chatRoomId, members, new ValueCallBack<ChatRoom>() {
                        @Override
                        public void onSuccess(ChatRoom value) {
                            callBack.onSuccess(createLiveData(value));
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            callBack.onError(error, errorMsg);
                        }
                    });
                });
            }
        }.asLiveData();
    }

    /**
     * 检查是否在白名单中
     *
     * @param username
     * @return
     */
    public LiveData<Resource<Boolean>> checkIfInGroupWhiteList(String username) {
        return new NetworkOnlyResource<Boolean, Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getChatRoomManager().checkIfInChatRoomWhiteList(username, new ValueCallBack<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        callBack.onSuccess(createLiveData(aBoolean));
                    }

                    @Override
                    public void onError(int i, String s) {
                        callBack.onError(i, s);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 获取成员列表（不包含owner和admins）
     *
     * @param chatRoomId
     * @return
     */
    public LiveData<Resource<List<String>>> getOnlyMembers(String chatRoomId) {
        return new NetworkOnlyResource<List<String>, List<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                ThreadManager.getInstance().runOnIOThread(() -> {
                    try {
                        ChatRoom chatRoom = getChatRoomManager().fetchChatRoomFromServer(chatRoomId, true);
                        List<String> memberList = chatRoom.getMemberList();
                        callBack.onSuccess(createLiveData(memberList));
                    } catch (ChatException e) {
                        callBack.onError(e.getErrorCode(), e.getMessage());
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 获取成员列表
     *
     * @param roomId
     * @return
     */
    public LiveData<Resource<List<String>>> getMembers(String roomId) {
        return new NetworkOnlyResource<List<String>, List<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                ThreadManager.getInstance().runOnIOThread(() -> {
                    try {
                        ChatRoom chatRoom = getChatRoomManager().fetchChatRoomFromServer(roomId, true);
                        List<String> allMembers = new ArrayList<>();
                        List<String> memberList = chatRoom.getMemberList();
                        allMembers.add(chatRoom.getOwner());
                        if (chatRoom.getAdminList() != null) {
                            allMembers.addAll(chatRoom.getAdminList());
                        }
                        if (memberList != null) {
                            allMembers.addAll(memberList);
                        }
                        callBack.onSuccess(createLiveData(allMembers));
                    } catch (ChatException e) {
                        callBack.onError(e.getErrorCode(), e.getMessage());
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 获取禁言列表
     *
     * @param roomId
     * @return
     */
    public LiveData<Resource<List<String>>> getMuteList(String roomId) {
        return new NetworkOnlyResource<List<String>, List<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                ThreadManager.getInstance().runOnIOThread(() -> {
                    try {
                        Map<String, Long> map = getChatRoomManager().fetchChatRoomMuteList(roomId, 1, 50);
                        callBack.onSuccess(createLiveData(new ArrayList<String>(map.keySet())));
                    } catch (ChatException e) {
                        callBack.onError(e.getErrorCode(), e.getMessage());
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 禁止聊天室成员发言，需要聊天室拥有者或者管理员权限
     *
     * @param chatRoomId
     * @return
     */
    public LiveData<Resource<ChatRoom>> MuteChatRoomMembers(String chatRoomId, List<String> muteMembers, long duration) {
        return new NetworkOnlyResource<ChatRoom, ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncMuteChatRoomMembers(chatRoomId, muteMembers, duration, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 取消禁言，需要聊天室拥有者或者管理员权限
     *
     * @param chatRoomId
     * @return
     */
    public LiveData<Resource<ChatRoom>> unMuteChatRoomMembers(String chatRoomId, List<String> muteMembers) {
        return new NetworkOnlyResource<ChatRoom, ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncUnMuteChatRoomMembers(chatRoomId, muteMembers, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 一键禁言
     *
     * @param chatRoomId
     * @return
     */
    public LiveData<Resource<ChatRoom>> muteAllMembers(String chatRoomId) {
        return new NetworkOnlyResource<ChatRoom, ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().muteAllMembers(chatRoomId, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 一键解除禁言
     *
     * @param chatRoomId
     * @return
     */
    public LiveData<Resource<ChatRoom>> unmuteAllMembers(String chatRoomId) {
        return new NetworkOnlyResource<ChatRoom, ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().unmuteAllMembers(chatRoomId, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

}
