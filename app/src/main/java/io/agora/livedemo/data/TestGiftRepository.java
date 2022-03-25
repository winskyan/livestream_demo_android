package io.agora.livedemo.data;

import android.content.Context;
import android.text.TextUtils;

import io.agora.chat.ChatClient;
import io.agora.livedemo.DemoApplication;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.data.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于获取本地礼物信息
 */
public class TestGiftRepository {
    static int SIZE = 8;
    static String[] names = {};

    public static List<GiftBean> getDefaultGifts() {
        Context context = DemoApplication.getInstance().getApplicationContext();
        List<GiftBean> gifts = new ArrayList<>();
        GiftBean bean;
        User user;
        for(int i = 1; i <= SIZE; i++){
            bean = new GiftBean();
            String name = "gift_default_"+i;
            int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            int nameId = context.getResources().getIdentifier("em_gift_default_name_" + i, "string", context.getPackageName());
            bean.setResource(resId);
            bean.setName(context.getString(nameId));
            bean.setId("gift_"+i);
            user = new User();
            user.setUsername(ChatClient.getInstance().getCurrentUser());
            bean.setUser(user);
            gifts.add(bean);
        }
        return gifts;
    }

    /**
     * 获取GiftBean
     * @param giftId
     * @return
     */
    public static GiftBean getGiftById(String giftId) {
        List<GiftBean> gifts = getDefaultGifts();
        for (GiftBean bean : gifts) {
            if(TextUtils.equals(bean.getId(), giftId)) {
                return bean;
            }
        }
        return null;
    }
}
