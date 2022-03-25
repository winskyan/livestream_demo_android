package io.agora.livedemo.ui.live.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import io.agora.livedemo.R;
import io.agora.livedemo.common.OnConfirmClickListener;
import io.agora.livedemo.common.OnItemClickListener;
import io.agora.livedemo.data.TestGiftRepository;
import io.agora.livedemo.data.model.GiftBean;
import io.agora.livedemo.ui.base.BaseLiveFragment;
import io.agora.livedemo.ui.live.adapter.GiftListAdapter;
import io.agora.livedemo.ui.widget.recyclerview.HorizontalPageLayoutManager;
import io.agora.livedemo.ui.widget.recyclerview.PagingScrollHelper;

import androidx.recyclerview.widget.RecyclerView;

public class LiveGiftListFragment extends BaseLiveFragment implements OnItemClickListener, LiveGiftNumDialog.OnGiftNumListener, LiveGiftNumDialog.OnDismissListener {
    private RecyclerView rvList;
    private GiftListAdapter adapter;
    private GiftBean giftBean;
    private OnConfirmClickListener listener;

    @Override
    protected int getLayoutId() {
        return R.layout.em_fragment_live_gift_list;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rvList = findViewById(R.id.rv_list);

        PagingScrollHelper snapHelper = new PagingScrollHelper();
        HorizontalPageLayoutManager manager = new HorizontalPageLayoutManager(2, 4);
        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(manager);
        adapter = new GiftListAdapter();
        rvList.setAdapter(adapter);

        snapHelper.setUpRecycleView(rvList);
        snapHelper.updateLayoutManger();
        snapHelper.scrollToPosition(0);
        rvList.setHorizontalScrollBarEnabled(true);
    }

    @Override
    protected void initListener() {
        super.initListener();
        adapter.setOnItemClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        adapter.setData(TestGiftRepository.getDefaultGifts());
    }

    @Override
    public void onItemClick(View view, int position) {
        giftBean = adapter.getItem(position);
        boolean checked = giftBean.isChecked();
        giftBean.setChecked(!checked);
        if(giftBean.isChecked()) {
            adapter.setSelectedPosition(position);
            showNumDialog(giftBean);
        }else {
            adapter.setSelectedPosition(-1);
        }
    }

    private void showNumDialog(GiftBean item) {
        LiveGiftNumDialog dialog = (LiveGiftNumDialog) getChildFragmentManager().findFragmentByTag("gift_num");
        if(dialog == null) {
            dialog = LiveGiftNumDialog.getNewInstance(item);
        }
        if(dialog.isAdded()) {
            return;
        }
        dialog.setOnGiftNumListener(this);
        dialog.setOnDismissListener(this);
        dialog.show(getChildFragmentManager(), "gift_num");
    }

    @Override
    public void onGiftNum(View view, int num) {
        giftBean.setNum(num);
        LiveGiftSendDialog dialog = (LiveGiftSendDialog) getChildFragmentManager().findFragmentByTag("gift_send");
        if(dialog == null) {
            dialog = LiveGiftSendDialog.getNewInstance(giftBean);
        }
        if(dialog.isAdded()) {
            return;
        }
        dialog.setOnConfirmClickListener(new OnConfirmClickListener() {
            @Override
            public void onConfirmClick(View view, Object bean) {
                if(listener != null) {
                    listener.onConfirmClick(view, bean);
                }
            }
        });
        dialog.show(getChildFragmentManager(), "gift_send");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        adapter.setSelectedPosition(-1);
    }

    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.listener = listener;
    }
}
