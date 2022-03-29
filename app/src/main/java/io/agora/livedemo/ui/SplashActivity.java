package io.agora.livedemo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.ChatClient;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.common.PreferenceManager;
import io.agora.livedemo.data.UserRepository;
import io.agora.livedemo.databinding.ActivitySplashBinding;
import io.agora.livedemo.ui.base.BaseLiveActivity;
import io.agora.livedemo.ui.other.viewmodels.LoginViewModel;


public class SplashActivity extends BaseLiveActivity {
    private LoginViewModel viewModel;
    private ActivitySplashBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getContentView() {
        mBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void initData() {
        super.initData();
        PreferenceManager.init(mContext);
        UserRepository.getInstance().init(mContext);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        mHandler.sendEmptyMessageDelayed(0, 1000 * 3);//3s
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            skipToTarget();
            return false;
        }
    });

    private void skipToTarget() {
        if (ChatClient.getInstance().isLoggedInBefore()) {
            DemoHelper.saveCurrentUser();
            DemoHelper.initDb();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            login();
        }
    }

    private void login() {
        ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage("wait...");
        pd.setCanceledOnTouchOutside(false);

        viewModel.getLoginObservable().observe(mContext, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    skipToTarget();
                }

                @Override
                public void onLoading() {
                    super.onLoading();
                    pd.show();
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    pd.dismiss();
                }
            });
        });

        viewModel.login();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
