package io.agora.livedemo.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.ThreadManager;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.data.model.User;

/**
 * 动画效果：每个动画展示最长时间为2000ms, 数字变动的时间为200ms。
 */
public class ShowGiveGiftView extends LinearLayout {
    private Context context;
    private NumberAnim giftNumberAnim;
    private Timer timer;
    private int duration = 200;
    private static final int maxExitTime = 2000;
    private static final int maxShowView = 2;

    public ShowGiveGiftView(Context context) {
        this(context, null);
    }

    public ShowGiveGiftView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowGiveGiftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initAttrs(context, attrs);
        init(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {

    }

    private void init(Context context) {
//        initAnimation(context);
    }

//    private void initAnimation(Context context) {
//    }

    public void showGift(GiftBean bean) {
        clearTiming();
        if (this.getChildCount() > maxShowView) {
            // 获取前2个元素的最后更新时间
            View giftView01 = getChildAt(0);
            ImageView iv_gift01 = giftView01.findViewById(R.id.iv_gift);
            long lastTime1 = (long) iv_gift01.getTag();
            int showTime1 = (int) giftView01.getTag();

            View giftView02 = getChildAt(1);
            ImageView iv_gift02 = giftView02.findViewById(R.id.iv_gift);
            long lastTime2 = (long) iv_gift02.getTag();
            int showTime2 = (int) giftView02.getTag();

            if (lastTime1 + showTime1 > lastTime2 + showTime2) { // 如果第二个View显示的时间比较长
                removeGiftView(giftView02);
            } else { // 如果第一个View显示的时间长
                removeGiftView(giftView01);
            }
        }

        // 添加礼物视图
        View newGiftView = getNewGiftView(bean);
        newGiftView.setTag(calculateDuration(bean));
        addView(newGiftView);

        // 播放动画
        TranslateAnimation inAnim = (TranslateAnimation) AnimationUtils.loadAnimation(context, R.anim.em_gift_in); // 礼物进入时动画
        newGiftView.startAnimation(inAnim);
        final MagicTextView mtv_giftNum = newGiftView.findViewById(R.id.tv_gift_num);
        inAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showGiftAnimation(mtv_giftNum, bean);
            }
        });

    }

    private void showGiftAnimation(MagicTextView tvGiftNum, GiftBean bean) {
        int num = bean.getNum();
        if (num > 0) {
            startGiftTimer(tvGiftNum, bean);
        }
    }

    private void startGiftTimer(MagicTextView tvGiftNum, GiftBean bean) {
        giftNumberAnim = new NumberAnim(); // 初始化数字动画
        int increment = calculateIncrease(bean);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ThreadManager.getInstance().runOnMainThread(() -> {
                    int tag = (int) tvGiftNum.getTag();
                    if (tag >= bean.getNum()) {
                        timer.cancel();
                        return;
                    }
                    if (tag + increment > bean.getNum()) {
                        tvGiftNum.setTag(bean.getNum());
                        tvGiftNum.setText(String.valueOf(bean.getNum()));
                    } else {
                        tvGiftNum.setTag(tag + increment);
                        tvGiftNum.setText(String.valueOf(tag + increment));
                    }
                    giftNumberAnim.showAnimator(tvGiftNum);

                });
            }

        }, 0, duration);
    }

    /**
     * 计算增长的数值
     *
     * @param bean
     * @return
     */
    private int calculateIncrease(GiftBean bean) {
        int increment = 1;
        if (bean.getNum() * duration > maxExitTime) {
            increment = (int) Math.ceil(bean.getNum() * 1f * duration / maxExitTime);
        }
        return increment;
    }

    private int calculateDuration(GiftBean bean) {
        if (bean.getNum() * duration > maxExitTime) {
            return maxExitTime;
        }
        return bean.getNum() * duration;
    }

    /**
     * 定时清理礼物列表信息
     */
    private void clearTiming() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    ThreadManager.getInstance().runOnMainThread(() -> {
                        int childCount = getChildCount();
                        long nowTime = System.currentTimeMillis();
                        for (int i = 0; i < childCount; i++) {

                            View childView = getChildAt(i);
                            ImageView iv_gift = (ImageView) childView.findViewById(R.id.iv_gift);
                            long lastUpdateTime = (long) iv_gift.getTag();

                            // 更新超过3秒就刷新
                            if (nowTime - lastUpdateTime >= maxExitTime) {
                                removeGiftView(childView);
                            }
                        }
                    });

                }
            }, 0, duration * 3);
        }
    }

    public void destroy() {
        if (timer != null) {
            timer.cancel();
        }
        if (giftNumberAnim != null && giftNumberAnim.lastAnimator != null && giftNumberAnim.lastAnimator.isRunning()) {
            giftNumberAnim.lastAnimator.cancel();
        }
    }

    /**
     * 移除礼物列表里的giftView
     */
    private void removeGiftView(final View removeGiftView) {
        Animation animation = removeGiftView.getAnimation();
        if (animation == null) {
            // 移除列表，外加退出动画
            TranslateAnimation outAnim = (TranslateAnimation) AnimationUtils.loadAnimation(context, R.anim.em_gift_out); // 礼物退出时动画
            outAnim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeView(removeGiftView);
                }
            });

            // 开启动画
            removeGiftView.startAnimation(outAnim);
        }
    }

    /**
     * 获取礼物
     */
    private View getNewGiftView(GiftBean gift) {

        // 添加标识, 该view若在layout中存在，就不在生成（用于findViewWithTag判断是否存在）
        View giftView = LayoutInflater.from(context).inflate(R.layout.em_layout_item_gift_show, null);
        // 添加标识, 记录生成时间，回收时用于判断是否是最新的，回收最老的
        ImageView iv_gift = giftView.findViewById(R.id.iv_gift);
        iv_gift.setTag(System.currentTimeMillis());

        iv_gift.setImageResource(gift.getResource());

        //显示赠送人
        TextView tvName = giftView.findViewById(R.id.tv_username);
        EaseImageView ivIcon = giftView.findViewById(R.id.iv_icon);
        User user = gift.getUser();
        if (user != null) {
            tvName.setText(DemoHelper.getNickName(user.getNickName()));
            //ivIcon.setImageResource(DemoHelper.getAvatarResource(user.getNickName()));
        }

        // 添加标识，记录礼物个数
        MagicTextView mtv_giftNum = giftView.findViewById(R.id.tv_gift_num);
        mtv_giftNum.setTag(1);
        mtv_giftNum.setText(String.valueOf(1));

        return giftView;
    }


    private class NumberAnim {
        private Animator lastAnimator;

        public void showAnimator(View v) {

//            if (lastAnimator != null) {
//                lastAnimator.removeAllListeners();
//                lastAnimator.cancel();
//                lastAnimator.end();
//            }
            ObjectAnimator animScaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f, 1.0f);
            ObjectAnimator animScaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f, 1.0f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(animScaleX, animScaleY);
            animSet.setDuration(duration);
            lastAnimator = animSet;
            animSet.start();
        }
    }
}
