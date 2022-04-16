package io.agora.livedemo.ui.live.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.livedemo.R;
import io.agora.livedemo.common.OnConfirmClickListener;
import io.agora.livedemo.data.TestGiftRepository;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.ui.base.BaseLiveFragment;
import io.agora.livedemo.ui.live.adapter.GiftListAdapter;
import io.agora.util.EMLog;

public class LiveGiftListFragment extends BaseLiveFragment implements OnItemClickListener, View.OnClickListener, LiveGiftInputNumDialog.OnConfirmClickListener {
    private RecyclerView rvList;
    private ImageView ivGiftMinus;
    private ImageView ivGiftPlus;
    private TextView tvGiftNum;
    private Button btnSend;
    private TextView tvGiftTotalValues;
    private int giftNum = 1;

    private GiftListAdapter adapter;
    private GiftBean giftBean;
    private OnConfirmClickListener listener;
    private int giftPosition;
    private Map<Integer, Integer> selectGiftLeftTimeMap;

    private final static int MESSAGE_UPDATE_GIFT_LEFT_TIME = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live_gift_list;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rvList = findViewById(R.id.rv_list);

        rvList.setLayoutManager(new GridLayoutManager(mContext, 4));
        adapter = new GiftListAdapter();
        rvList.setAdapter(adapter);
        rvList.addItemDecoration(new SpacesItemDecoration((int) EaseUtils.dip2px(mContext, 5)));


        ivGiftMinus = findViewById(R.id.iv_gift_minus);
        tvGiftNum = findViewById(R.id.tv_gift_num);
        ivGiftPlus = findViewById(R.id.iv_gift_plus);
        tvGiftTotalValues = findViewById(R.id.gift_total_values);
        btnSend = findViewById(R.id.btn_send);

        tvGiftNum.setText(String.valueOf(giftNum));

        tvGiftTotalValues.setText(mContext.getString(R.string.gift_send_total_values, String.valueOf(0)));

        setBottomViewEnable(false);

    }

    @Override
    protected void initListener() {
        super.initListener();
        adapter.setOnItemClickListener(this);

        ivGiftMinus.setOnClickListener(this);
        ivGiftPlus.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        tvGiftNum.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        giftPosition = -1;
        selectGiftLeftTimeMap = new HashMap<>();
        adapter.setData(TestGiftRepository.getDefaultGifts());
        updateGiftLeftTime();
    }

    @Override
    public void onItemClick(View view, int position) {
        giftBean = adapter.getItem(position);
        if (giftBean.getLeftTime() != 0) {
            return;
        }
        boolean checked = giftBean.isChecked();
        giftBean.setChecked(!checked);
        giftNum = 1;
        updateNumAndValues(giftNum);
        if (giftBean.isChecked()) {
            giftPosition = position;
            setBottomViewEnable(true);
            adapter.setSelectedPosition(position);
        } else {
            giftPosition = -1;
            setBottomViewEnable(false);
            adapter.setSelectedPosition(-1);
            tvGiftTotalValues.setText(mContext.getString(R.string.gift_send_total_values, String.valueOf(0)));
        }

    }

    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_gift_minus:
                giftNum = getNum();
                if (giftNum > 1) {
                    giftNum--;
                }
                updateNumAndValues(giftNum);
                break;
            case R.id.iv_gift_plus:
                giftNum = getNum();
                if (giftNum < 99) {
                    giftNum++;
                }
                updateNumAndValues(giftNum);
                break;
            case R.id.btn_send:
                giftNum = getNum();
                onGiftNum(giftNum);
                break;
            case R.id.tv_gift_num:
                showInputNumDialog();
                break;
        }
    }

    private void setBottomViewEnable(boolean enable) {
        ivGiftMinus.setEnabled(enable);
        tvGiftNum.setEnabled(enable);
        ivGiftPlus.setEnabled(enable);
        tvGiftTotalValues.setEnabled(enable);
        btnSend.setEnabled(enable);
    }

    private void showInputNumDialog() {
        LiveGiftInputNumDialog dialog = (LiveGiftInputNumDialog) getChildFragmentManager().findFragmentByTag("gift_input_num");
        if (dialog == null) {
            dialog = LiveGiftInputNumDialog.getNewInstance(Integer.parseInt(tvGiftNum.getText().toString().trim()));
        }
        if (dialog.isAdded()) {
            return;
        }
        dialog.setOnConfirmClickListener(this);
        dialog.show(getChildFragmentManager(), "gift_input_num");
    }

    private int getNum() {
        try {
            String num = tvGiftNum.getText().toString().trim();
            return Integer.parseInt(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void onGiftNum(int num) {
        giftBean.setNum(num);
        LiveGiftSendDialog dialog = (LiveGiftSendDialog) getChildFragmentManager().findFragmentByTag("gift_send");
        if (dialog == null) {
            dialog = LiveGiftSendDialog.getNewInstance(giftBean);
        }
        if (dialog.isAdded()) {
            return;
        }
        dialog.setOnConfirmClickListener(new OnConfirmClickListener() {
            @Override
            public void onConfirmClick(View view, Object bean) {
                updateGiftList();
                if (listener != null) {
                    listener.onConfirmClick(view, bean);
                }
            }
        });
        dialog.show(getChildFragmentManager(), "gift_send");
    }

    private void updateGiftList() {
        adapter.setSelectedPosition(-1);
        adapter.getItem(giftPosition).setLeftTime(3);
        selectGiftLeftTimeMap.put(giftPosition, 3);
    }

    @Override
    public void onConfirmClick(View v, int num) {
        updateNumAndValues(num);
    }

    private void updateNumAndValues(int num) {
        tvGiftNum.setText(String.valueOf(num));
        tvGiftTotalValues.setText(mContext.getString(R.string.gift_send_total_values, String.valueOf(num * giftBean.getValue())));
    }

    private static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view,
                                   RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = 0;
            outRect.right = 0;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_GIFT_LEFT_TIME:
                    updateGiftLeftTime();
                    break;
            }
            return false;
        }
    });

    private void updateGiftLeftTime() {
        boolean needNotify = false;
        for (Map.Entry<Integer, Integer> entry : selectGiftLeftTimeMap.entrySet()) {
            EMLog.i("lives", entry.getKey() + "," + entry.getValue());
            if (entry.getValue() > 0) {
                adapter.getItem(entry.getKey()).setLeftTime(entry.getValue() - 1);
                entry.setValue(entry.getValue() - 1);
                needNotify = true;
            } else {
                selectGiftLeftTimeMap.remove(entry.getKey());
            }
        }
        if (needNotify) {
            adapter.notifyDataSetChanged();
        }
        mHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_GIFT_LEFT_TIME, 1000);
    }
}
