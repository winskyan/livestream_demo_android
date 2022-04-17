package io.agora.livedemo.common;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.lives.EaseLiveMessageHelper;
import io.agora.chat.uikit.lives.OnLiveMessageCallBack;
import io.agora.chat.uikit.lives.OnLiveMessageReceiveListener;
import io.agora.livedemo.ui.live.fragment.LiveMemberListDialog;


public class DemoMsgHelper {
    private static DemoMsgHelper instance;

    private DemoMsgHelper() {
    }

    private String chatroomId;

    public static DemoMsgHelper getInstance() {
        if (instance == null) {
            synchronized (DemoMsgHelper.class) {
                if (instance == null) {
                    instance = new DemoMsgHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 需要在直播页面开始的时候初始化，防止chatroomId为空或不正确
     *
     * @param chatroomId
     */
    public void init(String chatroomId) {
        this.chatroomId = chatroomId;
        setCustomMsgListener();
        //设置相关的直播间信息
        EaseLiveMessageHelper.getInstance().setChatRoomInfo(chatroomId);
    }

    public String getCurrentRoomId() {
        return this.chatroomId;
    }

    /**
     * 设置自定义消息监听
     */
    public void setCustomMsgListener() {
        EaseLiveMessageHelper.getInstance().init();
    }

    /**
     * 移除自定义消息监听
     */
    public void removeCustomMsgLisenter() {
        EaseLiveMessageHelper.getInstance().removeListener();
    }

    /**
     * 发送文本消息
     *
     * @param content
     * @param isBarrageMsg
     * @param callBack
     */
    public void sendMsg(String content, boolean isBarrageMsg, OnLiveMessageCallBack callBack) {
        if (isBarrageMsg) {
            sendBarrageMsg(content, callBack);
        } else {
            sendTxtMsg(content, callBack);
        }
    }

    /**
     * 发送文本消息
     *
     * @param content
     * @param callBack
     */
    public void sendTxtMsg(String content, OnLiveMessageCallBack callBack) {
        ChatMessage message = ChatMessage.createTxtSendMessage(content, chatroomId);
        message.setChatType(ChatMessage.ChatType.ChatRoom);
        message.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                callBack.onSuccess();
                callBack.onSuccess(message);
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(i, s);
                callBack.onError(message.getMsgId(), i, s);
            }

            @Override
            public void onProgress(int i, String s) {
                callBack.onProgress(i, s);
            }
        });
        ChatClient.getInstance().chatManager().sendMessage(message);
    }

    /**
     * 发送礼物消息
     *
     * @param giftId
     * @param num
     * @param callBack
     */
    public void sendGiftMsg(String giftId, int num, OnLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendGiftMsg(giftId, num, new OnLiveMessageCallBack() {
            @Override
            public void onSuccess(ChatMessage message) {
                DemoHelper.saveGiftInfo(message);
                if (callBack != null) {
                    callBack.onSuccess();
                    callBack.onSuccess(message);
                }
            }

            @Override
            public void onProgress(int i, String s) {
                super.onProgress(i, s);
                if (callBack != null) {
                    callBack.onProgress(i, s);
                }
            }

            @Override
            public void onError(String messageId, int code, String error) {
                if (callBack != null) {
                    callBack.onError(code, error);
                    callBack.onError(messageId, code, error);
                }
            }
        });
    }

    /**
     * 发送点赞消息
     *
     * @param num
     * @param callBack
     */
    public void sendLikeMsg(int num, OnLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendPraiseMsg(num, new OnLiveMessageCallBack() {
            @Override
            public void onSuccess(ChatMessage message) {
                DemoHelper.saveLikeInfo(message);
                if (callBack != null) {
                    callBack.onSuccess(message);
                }
            }

            @Override
            public void onProgress(int i, String s) {
                super.onProgress(i, s);
                if (callBack != null) {
                    callBack.onProgress(i, s);
                }
            }

            @Override
            public void onError(String messageId, int code, String error) {
                if (callBack != null) {
                    callBack.onError(code, error);
                    callBack.onError(messageId, code, error);
                }
            }
        });
    }

    /**
     * 发送弹幕消息
     *
     * @param content
     * @param callBack
     */
    public void sendBarrageMsg(String content, OnLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendBarrageMsg(content, callBack);
    }

    /**
     * 获取礼物消息中礼物的id
     *
     * @param msg
     * @return
     */
    public String getMsgGiftId(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgGiftId(msg);
    }

    /**
     * 获取礼物消息中礼物的数量
     *
     * @param msg
     * @return
     */
    public int getMsgGiftNum(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgGiftNum(msg);
    }

    /**
     * 获取点赞消息中点赞的数目
     *
     * @param msg
     * @return
     */
    public int getMsgPraiseNum(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgPraiseNum(msg);
    }

    /**
     * 获取弹幕消息中的文本
     *
     * @param msg
     * @return
     */
    public String getMsgBarrageTxt(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgBarrageTxt(msg);
    }


    /**
     * 判断是否是礼物消息
     *
     * @param msg
     * @return
     */
    public boolean isGiftMsg(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().isGiftMsg(msg);
    }

    /**
     * 判断是否是点赞消息
     *
     * @param msg
     * @return
     */
    public boolean isPraiseMsg(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().isPraiseMsg(msg);
    }

    /**
     * 判断是否是弹幕消息
     *
     * @param msg
     * @return
     */
    public boolean isBarrageMsg(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().isBarrageMsg(msg);
    }

    public void setOnCustomMsgReceiveListener(OnLiveMessageReceiveListener listener) {
        EaseLiveMessageHelper.getInstance().setOnCustomMsgReceiveListener(listener);
    }
}
