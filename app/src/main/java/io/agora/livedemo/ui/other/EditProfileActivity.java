package io.agora.livedemo.ui.other;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.Map;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.livedemo.DemoConstants;
import io.agora.livedemo.R;
import io.agora.livedemo.common.DemoHelper;
import io.agora.livedemo.common.LiveDataBus;
import io.agora.livedemo.databinding.ActivityEditProfileBinding;
import io.agora.livedemo.ui.base.BaseLiveActivity;
import io.agora.livedemo.ui.live.fragment.ListDialogFragment;
import io.agora.livedemo.utils.Utils;
import io.agora.util.PathUtil;
import io.agora.util.VersionUtils;


public class EditProfileActivity extends BaseLiveActivity {
    private final static String TAG = EditProfileActivity.class.getSimpleName();
    private ActivityEditProfileBinding mBinding;
    private String[] mGenderArray;
    private final static int MAX_USERNAME_LENGTH = 24;
    private static final String[] calls = {"Take Photo", "Upload Photo"};
    private static final int REQUEST_CODE_PICK = 1;
    private static final int REQUEST_CODE_CUTTING = 2;
    private static final int REQUEST_CODE_CAMERA = 100;

    private String mAvatarPath;

    protected File mCameraFile;
    private Uri mCacheUri;

    @Override
    protected View getContentView() {
        mBinding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView() {
        super.initView();

        EaseUserUtils.showUserAvatar(mContext, String.valueOf(DemoHelper.getAvatarResource()), mBinding.userIcon);
        EaseUserUtils.setUserNick(DemoHelper.getAgoraId(), mBinding.itemUsername.getTvContent());
        mBinding.titlebarTitle.setTypeface(Utils.getRobotoTypeface(this.getApplicationContext()));
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.titleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBinding.titlebarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBinding.itemUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final View view = LayoutInflater.from(mContext).inflate(R.layout.modify_info_dialog, null);
                TextView title = view.findViewById(R.id.title);
                EditText editContent = view.findViewById(R.id.edit_content);
                TextView countTip = view.findViewById(R.id.count_tip);
                Button confirmBtn = view.findViewById(R.id.confirm);
                Button cancelBtn = view.findViewById(R.id.cancel);

                title.setText(mContext.getResources().getString(R.string.setting_username_title));
                editContent.setText(DemoHelper.getAgoraId());
                editContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_USERNAME_LENGTH)});
                editContent.setSelection(editContent.getText().toString().length());
                editContent.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        countTip.setText(s.toString().trim().length() + "/" + MAX_USERNAME_LENGTH);
                    }
                });


                countTip.setText(DemoHelper.getAgoraId().length() + "/" + MAX_USERNAME_LENGTH);

                final Dialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.getWindow().setContentView(view);

                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setUsername(editContent.getText().toString());
                        dialog.cancel();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
            }
        });

        mBinding.itemGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ListDialogFragment.Builder(mContext)
                        .setTitle(R.string.setting_gender_title)
                        .setGravity(Gravity.START)
                        .setData(mGenderArray)
                        .setCancelColorRes(R.color.black)
                        .setWindowAnimations(R.style.animate_dialog)
                        .setOnItemClickListener(new ListDialogFragment.OnDialogItemClickListener() {
                            @Override
                            public void OnItemClick(View view, int position) {
                                if (position != 2) {
                                    position = position + 1;
                                } else {
                                    position = 0;
                                }
                                setGender(position);
                            }
                        })
                        .show();
            }
        });

        mBinding.itemBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(EditProfileActivity.this, R.style.MyDatePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        setBirthday(year, monthOfYear, dayOfMonth);
                    }
                }
                        , calendar.get(Calendar.YEAR)
                        , calendar.get(Calendar.MONTH)
                        , calendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        mBinding.changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }

    @Override
    protected void initData() {
        super.initData();
        mGenderArray = getResources().getStringArray(R.array.gender_types);

        ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(new String[]{DemoHelper.getAgoraId()}, new ValueCallBack<Map<String, UserInfo>>() {
            @Override
            public void onSuccess(Map<String, UserInfo> stringUserInfoMap) {
                EditProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<String, UserInfo> user : stringUserInfoMap.entrySet()) {
                            Log.i(TAG, "user=" + user.getKey() + ",value=" + user.getValue().getGender() + "," + user.getValue().getBirth());
                            if (DemoHelper.getAgoraId().equals(user.getKey())) {
                                updateGenderView(user.getValue().getGender());
                                updateBirthdayView(user.getValue().getBirth());
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    private void setGender(final int gender) {
        ChatClient.getInstance().userInfoManager().updateOwnInfoByAttribute(UserInfo.UserInfoType.GENDER, String.valueOf(gender), new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EditProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateGenderView(gender);
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void updateGenderView(int gender) {
        if (gender < 0 || gender > mGenderArray.length) {
            Log.e(TAG, "gender value is incorrect,gender=" + gender);
            return;
        }
        if (0 == gender) {
            mBinding.itemGender.setContent(mGenderArray[2]);
        } else {
            mBinding.itemGender.setContent(mGenderArray[gender - 1]);
        }
    }

    private void setBirthday(int year, int month, int day) {
        final String birthday = month + "/" + day + "/" + year;
        ChatClient.getInstance().userInfoManager().updateOwnInfoByAttribute(UserInfo.UserInfoType.BIRTH, birthday, new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EditProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateBirthdayView(birthday);
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });


    }

    private void updateBirthdayView(String birthday) {
        if (!TextUtils.isEmpty(birthday)) {
            mBinding.itemBirthday.setContent(birthday);
        } else {
            mBinding.itemBirthday.setContent(this.getResources().getString(R.string.setting_unknown));
        }
    }

    private void setUsername(final String username) {
        if (TextUtils.isEmpty(username)) {
            return;
        }
        ChatClient.getInstance().userInfoManager().updateOwnInfoByAttribute(UserInfo.UserInfoType.NICKNAME, username, new ValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EditProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DemoHelper.getCurrentDemoUser().setId(username);
                        DemoHelper.saveCurrentUser();
                        DemoHelper.initDb();
                        EaseUserUtils.setUserNick(DemoHelper.getAgoraId(), mBinding.itemUsername.getTvContent());
                        LiveDataBus.get().with(DemoConstants.NICKNAME_CHANGE).postValue(username);
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });
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
            mAvatarPath = new File(new URI(mCacheUri.toString())).getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(mAvatarPath);
            mBinding.userIcon.setImageBitmap(bitmap);
            setAvatar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAvatar() {

    }
}