package io.agora.livedemo.ui.other.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.List;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.livedemo.BuildConfig;
import io.agora.livedemo.DemoApplication;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnConfirmClickListener;
import io.agora.livedemo.ui.base.BaseLiveFragment;
import io.agora.livedemo.ui.widget.ArrowItemView;

public class AboutMeFragment extends BaseLiveFragment implements View.OnClickListener, View.OnLongClickListener {
    private ArrowItemView itemVersion;
    private ArrowItemView itemView;
    private ImageView ivLogo;
    private Button btnOut;

    @Override
    protected int getLayoutId() {
        return R.layout.em_fragment_about_me;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        itemVersion = findViewById(R.id.item_version);
        itemView = findViewById(R.id.item_about);
        ivLogo = findViewById(R.id.iv_logo);
        btnOut = findViewById(R.id.btn_out);

        itemVersion.getTvContent().setText(BuildConfig.VERSION_NAME);

        boolean canRegister = DemoHelper.isCanRegister();
        btnOut.setVisibility(canRegister ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initListener() {
        super.initListener();
        itemView.setOnClickListener(this);
        btnOut.setOnClickListener(this);
        ivLogo.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_about:
                startToAbout();
                break;
            case R.id.btn_out:
                showOutDialog();
                break;
        }
    }

    private void startToAbout() {
        Uri uri = Uri.parse("http://www.easemob.com/about");
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it);
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.iv_logo:
                showSelectDialog();
                return true;
        }
        return false;
    }

    private void showSelectDialog() {
        boolean canRegister = DemoHelper.isCanRegister();
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(canRegister ? R.string.em_set_select_auto : R.string.em_set_select_login)
                .setConfirmButtonTxt(R.string.em_set_select_switch)
                .setOnConfirmClickListener(new OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, Object bean) {
                        DemoHelper.setCanRegister(!canRegister);
                        btnOut.setVisibility(!canRegister ? View.VISIBLE : View.GONE);
                    }
                })
                .build()
                .show(getChildFragmentManager(), "dialog");
    }

    private void showOutDialog() {
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(R.string.em_set_logout_confirm)
                .setOnConfirmClickListener(new OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, Object bean) {
                        logoutHx();
                    }
                })
                .build()
                .show(getChildFragmentManager(), "dialog");
    }

    private void logoutHx() {
        ChatClient.getInstance().logout(false, new CallBack() {
            @Override
            public void onSuccess() {
                skipToLogin();
            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
    }

    private void skipToLogin() {
        DemoHelper.clearUser();
    }
}
