package io.agora.livedemo.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import io.agora.chat.ChatClient;
import io.agora.livedemo.R;
import io.agora.livedemo.runtimepermissions.PermissionsManager;
import io.agora.livedemo.runtimepermissions.PermissionsResultAction;
import io.hyphenate.easeui.widget.EaseTitleBar;
import io.agora.livedemo.ui.base.BaseLiveActivity;
import io.agora.livedemo.ui.cdn.fragment.CdnLivingListFragment;
import io.agora.livedemo.ui.other.CreateLiveRoomActivity;
import io.agora.livedemo.ui.other.LoginActivity;
import io.agora.livedemo.ui.other.fragment.AboutMeFragment;

public class MainActivity extends BaseLiveActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    private EaseTitleBar mTitleBar;
    private Fragment mCurrentFragment;
    private Fragment mHomeFragment, mLiveListFragment, mAboutMeFragment;
    private LinearLayout llHomeHome, llHomeSet;
    private ImageView ivHomeHome, ivHomeSet;
    private RelativeLayout rlHomeLive;
    private int position;

    @Override
    protected int getLayoutId() {
        return R.layout.em_activity_main;
    }

    @Override
    protected void initView() {
        super.initView();
        mTitleBar = findViewById(R.id.title_bar_main);
        llHomeHome = findViewById(R.id.ll_home_home);
        llHomeSet = findViewById(R.id.ll_home_set);
        rlHomeLive = findViewById(R.id.rl_home_live);
        ivHomeHome = findViewById(R.id.iv_home_home);
        ivHomeSet = findViewById(R.id.iv_home_set);
    }

    @Override
    protected void initListener() {
        super.initListener();
        llHomeHome.setOnClickListener(this);
        llHomeSet.setOnClickListener(this);
        rlHomeLive.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        skipToTarget(position);
        Log.e(TAG, "user = " + ChatClient.getInstance().getCurrentUser());
        requestPermissions();
    }

    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//				Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt("position");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processConflictIntent(intent);
    }

    private void processConflictIntent(Intent intent) {
        if (intent.getBooleanExtra("conflict", false)) {
            ChatClient.getInstance().logout(false, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.prompt)
                    .setMessage("账户已在别处登录！")
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
            builder.show();
        }
    }

    private void switchToHome() {
        ivHomeHome.setImageResource(R.drawable.em_live_home_selected);
        ivHomeSet.setImageResource(R.drawable.em_live_set_unselected);
        mHomeFragment = getSupportFragmentManager().findFragmentByTag("home");
        if (mHomeFragment == null) {
            CdnLivingListFragment fragment = new CdnLivingListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("status", "ongoing");
            fragment.setArguments(bundle);
            mHomeFragment = fragment;
        }
        replace(mHomeFragment, "home");
    }

    private void switchToAboutMe() {
        ivHomeHome.setImageResource(R.drawable.em_live_home_unselected);
        ivHomeSet.setImageResource(R.drawable.em_live_set_selected);
        mAboutMeFragment = getSupportFragmentManager().findFragmentByTag("about_me");
        if (mAboutMeFragment == null) {
            mAboutMeFragment = new AboutMeFragment();
        }
        replace(mAboutMeFragment, "about_me");
    }

    private void startAnimation(float fromX, float toX, float fromY, float toY) {
        ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);
        rlHomeLive.startAnimation(animation);
    }

    private void replace(Fragment fragment, String tag) {
        if (mCurrentFragment != fragment) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            if (mCurrentFragment != null) {
                t.hide(mCurrentFragment);
            }
            mCurrentFragment = fragment;
            if (!fragment.isAdded()) {
                t.add(R.id.fl_main_fragment, fragment, tag).show(fragment).commit();
            } else {
                t.show(fragment).commit();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_home_home:
                skipToTarget(0);
                break;
            case R.id.ll_home_set:
                skipToTarget(1);
                break;
            case R.id.rl_home_live:
                CreateLiveRoomActivity.actionStart(mContext);
                break;
        }
    }

    private void skipToTarget(int position) {
        this.position = position;
        mTitleBar.setVisibility(View.VISIBLE);
        switch (position) {
            case 0:
                switchToHome();
                mTitleBar.setTitle(getResources().getString(R.string.em_set_live_room));
                mTitleBar.setRightImageResource(R.drawable.home_search);
                break;
//            case 1 :
//                switchToLiveList();
//                mTitleBar.setTitle(getResources().getString(R.string.em_main_title_live));
//                break;
            case 1:
                switchToAboutMe();
                mTitleBar.setTitle(getResources().getString(R.string.em_set_title));
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", position);
    }
}
