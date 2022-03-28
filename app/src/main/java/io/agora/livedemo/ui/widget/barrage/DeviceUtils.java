package io.agora.livedemo.ui.widget.barrage;

import android.content.Context;

/**
 * Device Utils
 * <p>
 * Created by wangjie on 2019/3/16.
 * <p>
 * 项目地址：https://github.com/mCyp/Muti-Barrage
 */
class DeviceUtils {
    static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @SuppressWarnings("unused")
    static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
