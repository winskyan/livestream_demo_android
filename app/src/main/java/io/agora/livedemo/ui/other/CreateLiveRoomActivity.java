package io.agora.livedemo.ui.other;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.net.URI;

import io.agora.chat.ChatClient;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.OnResourceParseCallback;
import io.agora.livedemo.data.UserRepository;
import io.agora.livedemo.data.model.LiveRoom;
import io.agora.livedemo.databinding.ActivityCreateLiveRoomBinding;
import io.agora.livedemo.ui.base.BaseLiveActivity;
import io.agora.livedemo.ui.cdn.CdnLiveHostActivity;
import io.agora.livedemo.ui.live.fragment.ListDialogFragment;
import io.agora.livedemo.ui.live.viewmodels.CreateLiveViewModel;
import io.agora.livedemo.utils.Utils;
import io.agora.util.PathUtil;
import io.agora.util.VersionUtils;

public class CreateLiveRoomActivity extends BaseLiveActivity {

    private static final int REQUEST_CODE_PICK = 1;
    private static final int REQUEST_CODE_CUTTING = 2;
    private static final String[] calls = {"Take Photo", "Upload Photo"};
    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int LIVE_NAME_MAX_LENGTH = 50;

    private ActivityCreateLiveRoomBinding mBinding;
    private CreateLiveViewModel mViewModel;

    private String mCoverPath;

    protected File mCameraFile;
    private Uri mCacheUri;
    private CameraX.LensFacing mFacingType;


    public static void actionStart(Context context) {
        Intent starter = new Intent(context, CreateLiveRoomActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected View getContentView() {
        mBinding = ActivityCreateLiveRoomBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView() {
        super.initView();
        mBinding.closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(v);
                finish();
            }
        });

        int resIndex = DemoHelper.getAvatarResourceIndex();
        if (-1 != resIndex) {
            mBinding.coverImage.setImageResource(UserRepository.getInstance().getResDrawable(resIndex));
        } else {
            mBinding.coverImage.setImageResource(R.drawable.avatar_0);
        }
        setEditTextEnable(false, mBinding.liveName);

        mBinding.liveName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LIVE_NAME_MAX_LENGTH)});

        mBinding.cameraView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                updateTransform();
            }
        });

        mBinding.cameraView.post(new Runnable() {
            @Override
            public void run() {
                startCamera(mFacingType);
            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditTextEnable(true, mBinding.liveName);
                if (null != mBinding.liveName.getText()) {
                    mBinding.liveName.setSelection(mBinding.liveName.getText().length());
                }
                mBinding.liveNameNumbersTip.setVisibility(View.VISIBLE);
                mBinding.liveNameNumbersTip.setText(mBinding.liveName.getText().toString().trim().length() + "/" + LIVE_NAME_MAX_LENGTH);
            }
        });

        mBinding.liveName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_SEND) {
                    setEditTextEnable(false, mBinding.liveName);
                    mBinding.liveNameNumbersTip.setVisibility(View.GONE);
                }
                return false;
            }
        });

        mBinding.liveName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBinding.liveNameNumbersTip.setText(s.toString().trim().length() + "/" + LIVE_NAME_MAX_LENGTH);
            }
        });

        mBinding.coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog();
            }
        });

        mBinding.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditTextEnable(false, mBinding.liveName);
                mBinding.liveNameNumbersTip.setVisibility(View.GONE);
            }
        });

        mBinding.goLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLive();
            }
        });

        mBinding.flip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraX.LensFacing.FRONT == mFacingType) {
                    startCamera(CameraX.LensFacing.BACK);
                } else {
                    startCamera(CameraX.LensFacing.FRONT);
                }
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        mFacingType = CameraX.LensFacing.FRONT;
        initViewModel();
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(CreateLiveViewModel.class);
        mViewModel.getCreateObservable().observe(mContext, response -> {
            parseResource(response, new OnResourceParseCallback<LiveRoom>(true) {
                @Override
                public void onSuccess(LiveRoom data) {
                    if (DemoHelper.isCdnLiveType(data.getVideo_type())) {
                        stopCamera();
                        CdnLiveHostActivity.actionStart(mContext, data);
                    }
                    finish();
                }

                @Override
                public void onLoading() {
                    super.onLoading();
                    CreateLiveRoomActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBinding.goLive.setText("");
                            mBinding.loading.bringToFront();
                            mBinding.loading.invalidate();
                            Animation rotateAnimation = AnimationUtils.loadAnimation(CreateLiveRoomActivity.this, R.anim.go_live_loading_anim);
                            LinearInterpolator lin = new LinearInterpolator();
                            rotateAnimation.setInterpolator(lin);
                            mBinding.loading.startAnimation(rotateAnimation);
                            mBinding.loading.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    dismissProgressDialog();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    showToast("go live fail: " + message);
                }
            });
        });
    }

    void startLive() {
        String name = "";
        if (mBinding.liveName.getText() != null) {
            name = mBinding.liveName.getText().toString();
        }
        if (TextUtils.isEmpty(name)) {
            showToast(getResources().getString(R.string.create_live_room_check_info));
            return;
        }
        mViewModel.createLiveRoom(name, "", mCoverPath, LiveRoom.Type.agora_cdn_live.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUEST_CODE_CUTTING:
                setPicToView();
                break;
            case REQUEST_CODE_CAMERA:
                if (mCameraFile != null && mCameraFile.exists()) {
                    startPhotoZoom(getUriForFile(mContext, mCameraFile));
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showSelectDialog() {
        new ListDialogFragment.Builder(mContext)
                .setTitle(R.string.create_live_change_cover)
                .setGravity(Gravity.START)
                .setData(calls)
                .setCancelColorRes(R.color.black)
                .setWindowAnimations(R.style.animate_dialog)
                .setOnItemClickListener(new ListDialogFragment.OnDialogItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        switch (position) {
                            case 0:
                                selectPicFromCamera();
                                break;
                            case 1:
                                selectImageFromLocal();
                                break;
                        }
                    }
                })
                .show();
    }

    private void selectImageFromLocal() {
        Intent intent = null;
        if (VersionUtils.isTargetQ(mContext)) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            if (Build.VERSION.SDK_INT < 19) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
            } else {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
        }
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK);
    }

    /**
     * select picture from camera
     */
    private void selectPicFromCamera() {
        if (!checkSdCardExist()) {
            return;
        }
        mCameraFile = new File(PathUtil.getInstance().getImagePath(), ChatClient.getInstance().getCurrentUser()
                + System.currentTimeMillis() + ".jpg");

        mCameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(mContext, mCameraFile)),
                REQUEST_CODE_CAMERA);
    }

    private static Uri getUriForFile(Context context, @NonNull File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    private boolean checkSdCardExist() {
        return EaseUtils.isSdcardExist();
    }

    private void startPhotoZoom(Uri uri) {
        mCacheUri = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 450);
        intent.putExtra("outputY", 450);
        intent.putExtra("output", mCacheUri);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CODE_CUTTING);
    }

    /**
     * save the picture data
     */
    private void setPicToView() {
        try {
            mCoverPath = new File(new URI(mCacheUri.toString())).getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(mCoverPath);
            mBinding.coverImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setEditTextEnable(boolean enable, EditText editText) {
        editText.setFocusable(enable);
        editText.setFocusableInTouchMode(enable);
        editText.setInputType(enable ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : InputType.TYPE_NULL);
        editText.setHorizontallyScrolling(false);
        editText.setMaxLines(Integer.MAX_VALUE);
        if (enable) {
            editText.requestFocus();
            Utils.showKeyboard(editText);
        } else {
            editText.clearFocus();
            Utils.hideKeyboard(editText);
        }
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        // Compute the center of the view finder
        float centerX = mBinding.cameraView.getWidth() / 2f;
        float centerY = mBinding.cameraView.getHeight() / 2f;

        float[] rotations = {0, 90, 180, 270};
        // Correct preview output to account for display rotation
        float rotationDegrees = rotations[mBinding.cameraView.getDisplay().getRotation()];

        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        mBinding.cameraView.setTransform(matrix);
    }

    private void startCamera(CameraX.LensFacing facing) {
        mFacingType = facing;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopCamera();
            PreviewConfig previewConfig = new PreviewConfig.Builder()
                    .setLensFacing(facing)
                    .build();

            Preview preview = new Preview(previewConfig);
            preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
                @Override
                public void onUpdated(Preview.PreviewOutput output) {
                    ViewGroup parent = (ViewGroup) mBinding.cameraView.getParent();
                    parent.removeView(mBinding.cameraView);
                    parent.addView(mBinding.cameraView, 0);

                    mBinding.cameraView.setSurfaceTexture(output.getSurfaceTexture());
                    updateTransform();
                }
            });
            CameraX.bindToLifecycle(this, preview);
        } else {

        }
    }

    private void stopCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraX.unbindAll();
        } else {

        }
    }
}