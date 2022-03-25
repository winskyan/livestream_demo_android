package io.agora.livedemo.ui.other;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.ChatClient;
import io.agora.livedemo.databinding.ActivitySplashBinding;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.common.PreferenceManager;
import io.agora.livedemo.data.UserRepository;
import io.agora.livedemo.data.model.User;
import io.agora.livedemo.ui.MainActivity;
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

        UserRepository.getInstance().init(mContext);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        AlphaAnimation animation = new AlphaAnimation(0, 1f);
        animation.setDuration(3 * 1000);//3s
        mBinding.tvWelcome.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                skipToTarget();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void skipToTarget() {
        if (ChatClient.getInstance().isLoggedInBefore()) {
            PreferenceManager.init(mContext, ChatClient.getInstance().getCurrentUser());
            DemoHelper.saveUserId();
            DemoHelper.initDb();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            if (DemoHelper.isCanRegister()) {
               startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                createRandomUser();
            }

        }
    }

    private void createRandomUser() {
        ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage("wait...");
        pd.setCanceledOnTouchOutside(false);

        viewModel.getLoginObservable().observe(mContext, response -> {
            parseResource(response, new OnResourceParseCallback<User>() {
                @Override
                public void onSuccess(User data) {
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
}
