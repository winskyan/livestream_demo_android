package io.agora.livedemo.common.utils;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.lives.EaseLiveMessageHelper;
import io.agora.chat.uikit.lives.OnSendLiveMessageCallBack;


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
     * It needs to be initialized at the beginning of the live page to prevent the chatroomId from being empty or incorrect
     *
     * @param chatroomId
     */
    public void init(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getCurrentRoomId() {
        return this.chatroomId;
    }

    /**
     * Send a text message
     *
     * @param content
     * @param isBarrageMsg
     * @param callBack
     */
    public void sendMsg(String content, boolean isBarrageMsg, OnSendLiveMessageCallBack callBack) {
        if (isBarrageMsg) {
            sendBarrageMsg(content, callBack);
        } else {
            sendTxtMsg(content, callBack);
        }
    }

    /**
     * Send a text message
     *
     * @param content
     * @param callBack
     */
    public void sendTxtMsg(String content, OnSendLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendTxtMsg(content, callBack);
    }

    /**
     * Send a gift message
     *
     * @param giftId
     * @param num
     * @param callBack
     */
    public void sendGiftMsg(String giftId, int num, OnSendLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendGiftMsg(chatroomId, giftId, num, new OnSendLiveMessageCallBack() {
            @Override
            public void onSuccess(ChatMessage message) {
                DemoHelper.saveGiftInfo(message);
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
     * Send a like message
     *
     * @param num
     * @param callBack
     */
    public void sendLikeMsg(int num, OnSendLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendPraiseMsg(chatroomId, num, new OnSendLiveMessageCallBack() {
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
     * Send a barrage message
     *
     * @param content
     * @param callBack
     */
    public void sendBarrageMsg(String content, OnSendLiveMessageCallBack callBack) {
        EaseLiveMessageHelper.getInstance().sendBarrageMsg(chatroomId, content, callBack);
    }

    /**
     * Gets the id of the gift in the gift message
     *
     * @param msg
     * @return
     */
    public String getMsgGiftId(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgGiftId(msg);
    }

    /**
     * Gets the number of gifts in the gift message
     *
     * @param msg
     * @return
     */
    public int getMsgGiftNum(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgGiftNum(msg);
    }

    /**
     * Gets the number of likes in the like message
     *
     * @param msg
     * @return
     */
    public int getMsgPraiseNum(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgPraiseNum(msg);
    }

    /**
     * Gets the text in the bullet screen message
     *
     * @param msg
     * @return
     */
    public String getMsgBarrageTxt(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().getMsgBarrageTxt(msg);
    }


    /**
     * Determine if it is a gift message
     *
     * @param msg
     * @return
     */
    public boolean isGiftMsg(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().isGiftMsg(msg);
    }

    /**
     * Determine if it is a like message
     *
     * @param msg
     * @return
     */
    public boolean isPraiseMsg(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().isPraiseMsg(msg);
    }

    /**
     * Determine if it is a bullet screen message
     *
     * @param msg
     * @return
     */
    public boolean isBarrageMsg(ChatMessage msg) {
        return EaseLiveMessageHelper.getInstance().isBarrageMsg(msg);
    }
}
